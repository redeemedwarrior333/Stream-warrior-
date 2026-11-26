package com.streamwarrior.app.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.streamwarrior.app.ai.FirebaseAiService
import com.streamwarrior.app.data.ChatTurn
import com.streamwarrior.app.stream.LiveOverlayBus
import com.streamwarrior.app.voice.StreamVoiceEngine
import com.streamwarrior.app.voice.VoiceChatMessage
import com.streamwarrior.app.voice.VoicePersona
import com.streamwarrior.app.voice.VoicePersonas
import com.streamwarrior.app.voice.VoiceTonePreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "VoiceAiViewModel"

sealed interface VoiceUiState {
    data object Idle : VoiceUiState
    data class Listening(val partialTranscript: String, val rmsDb: Float) : VoiceUiState
    data class Thinking(val userTranscript: String) : VoiceUiState
    data class Streaming(val userTranscript: String, val partialReply: String) : VoiceUiState
    data class Speaking(val userTranscript: String, val fullReply: String) : VoiceUiState
    data class Error(val message: String) : VoiceUiState
}

class VoiceAiViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedPersona = MutableStateFlow(VoicePersonas.all.first())
    val selectedPersona: StateFlow<VoicePersona> = _selectedPersona.asStateFlow()

    private val _tonePreset = MutableStateFlow(VoiceTonePreset.NORMAL)
    val tonePreset: StateFlow<VoiceTonePreset> = _tonePreset.asStateFlow()

    private val _speakAloud = MutableStateFlow(true)
    val speakAloud: StateFlow<Boolean> = _speakAloud.asStateFlow()

    private val _handsFreeLoop = MutableStateFlow(true)
    val handsFreeLoop: StateFlow<Boolean> = _handsFreeLoop.asStateFlow()

    private val _uiState = MutableStateFlow<VoiceUiState>(VoiceUiState.Idle)
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(
            VoiceChatMessage(
                id = 1L,
                isUser = false,
                text = VoicePersonas.all.first().greeting,
                personaName = VoicePersonas.all.first().name
            )
        )
    )
    val messages: StateFlow<List<VoiceChatMessage>> = _messages.asStateFlow()

    private var streamerName: String = "Stream Warrior"
    private var activeGenerationJob: Job? = null
    private var lastTurnUsedVoice: Boolean = false
    private var currentTranscriptForSpeech: String = ""
    private var currentReplyForSpeech: String = ""

    private val voiceEngine: StreamVoiceEngine = StreamVoiceEngine(
        context = application,
        onPartialTranscript = { partial ->
            val current = _uiState.value
            if (current is VoiceUiState.Listening) {
                _uiState.value = current.copy(partialTranscript = partial)
            }
        },
        onFinalTranscript = { finalText ->
            submitTurn(finalText, fromVoice = true)
        },
        onRmsChanged = { rms ->
            val current = _uiState.value
            if (current is VoiceUiState.Listening) {
                _uiState.value = current.copy(rmsDb = rms)
            }
        },
        onSpeechError = { msg, recoverableSilence ->
            val current = _uiState.value
            if (current is VoiceUiState.Listening && current.partialTranscript.isNotBlank() && recoverableSilence) {
                submitTurn(current.partialTranscript, fromVoice = true)
            } else {
                _uiState.value = VoiceUiState.Error(msg)
            }
        },
        onTtsStart = {
            if (_uiState.value !is VoiceUiState.Streaming && _uiState.value !is VoiceUiState.Listening) {
                if (currentReplyForSpeech.isNotBlank()) {
                    _uiState.value = VoiceUiState.Speaking(
                        userTranscript = currentTranscriptForSpeech,
                        fullReply = currentReplyForSpeech
                    )
                }
            }
        },
        onTtsAllDone = {
            if (_uiState.value is VoiceUiState.Speaking) {
                if (lastTurnUsedVoice && _handsFreeLoop.value && voiceEngine.isSpeechRecognitionAvailable) {
                    startListeningInternal()
                } else {
                    _uiState.value = VoiceUiState.Idle
                }
            }
        }
    )

    val isSpeechRecognitionAvailable: Boolean
        get() = voiceEngine.isSpeechRecognitionAvailable

    init {
        viewModelScope.launch {
            _uiState.collect { state ->
                val personaName = _selectedPersona.value.name
                when (state) {
                    is VoiceUiState.Listening -> LiveOverlayBus.updateVoiceCaptions(
                        persona = personaName,
                        status = "LISTENING",
                        userCaption = state.partialTranscript,
                        aiCaption = ""
                    )
                    is VoiceUiState.Thinking -> LiveOverlayBus.updateVoiceCaptions(
                        persona = personaName,
                        status = "THINKING",
                        userCaption = state.userTranscript,
                        aiCaption = ""
                    )
                    is VoiceUiState.Streaming -> LiveOverlayBus.updateVoiceCaptions(
                        persona = personaName,
                        status = "ON AIR",
                        userCaption = state.userTranscript,
                        aiCaption = state.partialReply
                    )
                    is VoiceUiState.Speaking -> LiveOverlayBus.updateVoiceCaptions(
                        persona = personaName,
                        status = "SPEAKING",
                        userCaption = state.userTranscript,
                        aiCaption = state.fullReply
                    )
                    VoiceUiState.Idle,
                    is VoiceUiState.Error -> Unit
                }
            }
        }
    }

    fun speakAudioCheck() {
        voiceEngine.beginSpeechStream()
        voiceEngine.enqueueSpeechChunk("Stream Warrior audio check complete. Ready to go live!")
        voiceEngine.finishSpeechStream()
    }

    fun updateStreamerName(name: String) {
        if (name.isNotBlank()) {
            streamerName = name.trim()
        }
    }

    fun selectPersona(persona: VoicePersona) {
        if (_selectedPersona.value.id == persona.id) return
        stopAll()
        _selectedPersona.value = persona
        _messages.value = listOf(
            VoiceChatMessage(
                id = System.currentTimeMillis(),
                isUser = false,
                text = persona.greeting,
                personaName = persona.name
            )
        )
        _uiState.value = VoiceUiState.Idle
    }

    fun cycleTonePreset() {
        val values = VoiceTonePreset.entries
        val next = values[(values.indexOf(_tonePreset.value) + 1) % values.size]
        _tonePreset.value = next
        voiceEngine.setTone(next)
    }

    fun toggleSpeakAloud() {
        val next = !_speakAloud.value
        _speakAloud.value = next
        if (!next) {
            voiceEngine.stopSpeaking()
            if (_uiState.value is VoiceUiState.Speaking) {
                _uiState.value = VoiceUiState.Idle
            }
        }
    }

    fun toggleHandsFreeLoop() {
        _handsFreeLoop.value = !_handsFreeLoop.value
    }

    fun startVoiceListening() {
        activeGenerationJob?.cancel()
        voiceEngine.stopSpeaking()
        startListeningInternal()
    }

    private fun startListeningInternal() {
        _uiState.value = VoiceUiState.Listening(partialTranscript = "", rmsDb = 0f)
        voiceEngine.startListening()
    }

    fun finishListeningNow() {
        val current = _uiState.value
        if (current is VoiceUiState.Listening) {
            if (current.partialTranscript.isNotBlank()) {
                voiceEngine.cancelListening()
                submitTurn(current.partialTranscript, fromVoice = true)
            } else {
                voiceEngine.stopListening()
            }
        }
    }

    fun stopAll() {
        lastTurnUsedVoice = false
        activeGenerationJob?.cancel()
        activeGenerationJob = null
        voiceEngine.cancelListening()
        voiceEngine.stopSpeaking()
        _uiState.value = VoiceUiState.Idle
    }

    fun resetConversation() {
        stopAll()
        val persona = _selectedPersona.value
        _messages.value = listOf(
            VoiceChatMessage(
                id = System.currentTimeMillis(),
                isUser = false,
                text = persona.greeting,
                personaName = persona.name
            )
        )
    }

    fun submitTextPrompt(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank()) return
        voiceEngine.cancelListening()
        voiceEngine.stopSpeaking()
        submitTurn(clean, fromVoice = false)
    }

    private fun submitTurn(userText: String, fromVoice: Boolean) {
        val clean = userText.trim()
        if (clean.isBlank()) {
            _uiState.value = VoiceUiState.Idle
            return
        }

        activeGenerationJob?.cancel()
        lastTurnUsedVoice = fromVoice
        currentTranscriptForSpeech = clean
        currentReplyForSpeech = ""

        val persona = _selectedPersona.value
        val priorHistory = _messages.value.map { msg ->
            ChatTurn(
                role = if (msg.isUser) "user" else "model",
                content = msg.text
            )
        }

        val userMsg = VoiceChatMessage(
            id = System.currentTimeMillis(),
            isUser = true,
            text = clean,
            spokenByVoice = fromVoice
        )
        _messages.value = (_messages.value + userMsg).takeLast(30)
        _uiState.value = VoiceUiState.Thinking(clean)

        val shouldSpeak = _speakAloud.value
        if (shouldSpeak) {
            voiceEngine.beginSpeechStream()
        }

        val enrichedSystem = "${persona.systemInstruction} Address or play off the streamer ($streamerName) naturally."

        activeGenerationJob = viewModelScope.launch {
            var latestReply = ""
            var spokenOffset = 0
            try {
                FirebaseAiService.streamConversation(
                    systemPrompt = enrichedSystem,
                    history = priorHistory,
                    userMessage = clean
                ).collect { accumulated ->
                    latestReply = accumulated
                    currentReplyForSpeech = accumulated
                    _uiState.value = VoiceUiState.Streaming(
                        userTranscript = clean,
                        partialReply = accumulated
                    )
                    if (shouldSpeak) {
                        val (chunks, nextOffset) = extractCompleteSentenceChunks(accumulated, spokenOffset)
                        chunks.forEach { sentence ->
                            voiceEngine.enqueueSpeechChunk(sentence)
                        }
                        spokenOffset = nextOffset
                    }
                }

                if (latestReply.isBlank()) {
                    _uiState.value = VoiceUiState.Error("Gemini returned an empty response. Please try again.")
                    return@launch
                }

                val aiMsg = VoiceChatMessage(
                    id = System.currentTimeMillis() + 1L,
                    isUser = false,
                    text = latestReply.trim(),
                    personaName = persona.name
                )
                _messages.value = (_messages.value + aiMsg).takeLast(30)

                if (shouldSpeak) {
                    val remaining = latestReply.substring(spokenOffset).trim()
                    if (remaining.isNotEmpty()) {
                        voiceEngine.enqueueSpeechChunk(remaining)
                    }
                    _uiState.value = VoiceUiState.Speaking(
                        userTranscript = clean,
                        fullReply = latestReply.trim()
                    )
                    voiceEngine.finishSpeechStream()
                } else {
                    _uiState.value = VoiceUiState.Idle
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Voice AI generation timed out", e)
                if (shouldSpeak) voiceEngine.stopSpeaking()
                _uiState.value = VoiceUiState.Error("AI response timed out after 60 seconds. Tap Mic or Retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Voice AI generation failed", e)
                if (shouldSpeak) voiceEngine.stopSpeaking()
                _uiState.value = VoiceUiState.Error(
                    e.localizedMessage ?: "Failed to reach Gemini AI."
                )
            }
        }
    }

    override fun onCleared() {
        activeGenerationJob?.cancel()
        voiceEngine.release()
        super.onCleared()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return VoiceAiViewModel(application) as T
                }
            }

        fun extractCompleteSentenceChunks(
            fullText: String,
            startOffset: Int
        ): Pair<List<String>, Int> {
            if (startOffset >= fullText.length) return emptyList<String>() to startOffset
            val chunks = mutableListOf<String>()
            var cursor = startOffset
            var idx = startOffset
            while (idx < fullText.length) {
                val ch = fullText[idx]
                val isTerminal = ch == '.' || ch == '!' || ch == '?' || ch == '\n'
                val followedByBoundary = idx == fullText.lastIndex || fullText[idx + 1].isWhitespace()
                if (isTerminal && followedByBoundary) {
                    val piece = fullText.substring(cursor, idx + 1).trim()
                    if (piece.length >= 6) {
                        chunks.add(piece)
                        cursor = idx + 1
                    }
                }
                idx++
            }
            return chunks to cursor
        }
    }
}

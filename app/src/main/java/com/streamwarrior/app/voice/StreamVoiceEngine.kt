package com.streamwarrior.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

private const val TAG = "StreamVoiceEngine"

class StreamVoiceEngine(
    context: Context,
    private val onPartialTranscript: (String) -> Unit,
    private val onFinalTranscript: (String) -> Unit,
    private val onRmsChanged: (Float) -> Unit,
    private val onSpeechError: (String, Boolean) -> Unit,
    private val onTtsStart: () -> Unit,
    private val onTtsAllDone: () -> Unit
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    @Volatile
    private var ttsReady = false
    @Volatile
    private var currentTone = VoiceTonePreset.NORMAL
    @Volatile
    private var activeUtteranceCount = 0
    @Volatile
    private var allChunksQueued = false

    val isSpeechRecognitionAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(appContext)

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = tts ?: return@TextToSpeech
                    val res = engine.setLanguage(Locale.US)
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        engine.setLanguage(Locale.getDefault())
                    }
                    engine.setSpeechRate(currentTone.speechRate)
                    engine.setPitch(currentTone.pitch)
                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            mainHandler.post { onTtsStart() }
                        }

                        override fun onDone(utteranceId: String?) {
                            mainHandler.post {
                                activeUtteranceCount = (activeUtteranceCount - 1).coerceAtLeast(0)
                                if (activeUtteranceCount == 0 && allChunksQueued) {
                                    onTtsAllDone()
                                }
                            }
                        }

                        @Deprecated("Deprecated in API 21")
                        @Suppress("OVERRIDE_DEPRECATION")
                        override fun onError(utteranceId: String?) {
                            mainHandler.post {
                                activeUtteranceCount = (activeUtteranceCount - 1).coerceAtLeast(0)
                                if (activeUtteranceCount == 0 && allChunksQueued) {
                                    onTtsAllDone()
                                }
                            }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            mainHandler.post {
                                activeUtteranceCount = (activeUtteranceCount - 1).coerceAtLeast(0)
                                if (activeUtteranceCount == 0 && allChunksQueued) {
                                    onTtsAllDone()
                                }
                            }
                        }
                    })
                    ttsReady = true
                } else {
                    Log.w(TAG, "TextToSpeech initialization returned status=$status")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create TextToSpeech", e)
        }
    }

    fun setTone(tone: VoiceTonePreset) {
        currentTone = tone
        mainHandler.post {
            tts?.setSpeechRate(tone.speechRate)
            tts?.setPitch(tone.pitch)
        }
    }

    fun startListening() {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                onSpeechError(
                    "On-device Speech Recognition service is unavailable on this device. You can still type prompts below and hear Gemini speak back!",
                    false
                )
                return@post
            }
            try {
                stopTtsInternal()
                speechRecognizer?.destroy()
                val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)
                speechRecognizer = recognizer
                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) {
                        onRmsChanged(rmsdB)
                    }

                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit

                    override fun onError(error: Int) {
                        val (msg, recoverableSilence) = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "Didn't catch anything — tap Mic or speak closer." to true
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "No speech detected." to true
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission is needed for voice interaction." to false
                            SpeechRecognizer.ERROR_AUDIO ->
                                "Microphone audio error. Check if another app holds the mic." to false
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Speech recognizer network error." to false
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                                "Speech recognizer is busy — try again." to true
                            else ->
                                "Voice capture stopped (code $error)." to false
                        }
                        onSpeechError(msg, recoverableSilence)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val best = matches?.firstOrNull()?.trim().orEmpty()
                        if (best.isBlank()) {
                            onSpeechError("Didn't catch clear words — try speaking again.", true)
                        } else {
                            onFinalTranscript(best)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()
                            .orEmpty()
                        if (partial.isNotEmpty()) {
                            onPartialTranscript(partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(
                        RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                        1400L
                    )
                }
                recognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start SpeechRecognizer", e)
                onSpeechError(
                    e.localizedMessage ?: "Could not start microphone listener.",
                    false
                )
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {
            }
        }
    }

    fun cancelListening() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }
        }
    }

    fun beginSpeechStream() {
        mainHandler.post {
            stopTtsInternal()
            allChunksQueued = false
        }
    }

    fun enqueueSpeechChunk(rawText: String) {
        val clean = sanitizeForSpeech(rawText)
        if (clean.isBlank()) return
        mainHandler.post {
            val engine = tts
            if (!ttsReady || engine == null) return@post
            activeUtteranceCount += 1
            val id = UUID.randomUUID().toString()
            engine.speak(clean, TextToSpeech.QUEUE_ADD, null, id)
        }
    }

    fun finishSpeechStream() {
        mainHandler.post {
            allChunksQueued = true
            if (activeUtteranceCount == 0) {
                onTtsAllDone()
            }
        }
    }

    fun stopSpeaking() {
        mainHandler.post {
            stopTtsInternal()
            onTtsAllDone()
        }
    }

    private fun stopTtsInternal() {
        activeUtteranceCount = 0
        allChunksQueued = true
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun release() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {
            }
            try {
                tts?.stop()
                tts?.shutdown()
                tts = null
            } catch (_: Exception) {
            }
        }
    }

    companion object {
        fun sanitizeForSpeech(text: String): String {
            return text
                .replace("**", "")
                .replace("__", "")
                .replace("`", "")
                .replace("#", "")
                .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
        }
    }
}

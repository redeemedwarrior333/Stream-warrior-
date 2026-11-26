package com.streamwarrior.app.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.ChatTurn
import com.streamwarrior.app.data.CustomSound
import com.streamwarrior.app.data.DenAlertEvent
import com.streamwarrior.app.data.DenConfig
import com.streamwarrior.app.data.ForgeTool
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.data.SettingsRepository
import com.streamwarrior.app.data.StreamConnectionConfig
import com.streamwarrior.app.data.WeirdLawScore
import com.streamwarrior.app.stream.LiveOverlayBus
import com.streamwarrior.app.stream.LocalOverlayServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StreamWarriorViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val streamerName: StateFlow<String> = repository.streamerNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Stream Warrior")

    val aiConfig: StateFlow<AiConfig> = repository.aiConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiConfig())

    val customSounds: StateFlow<List<CustomSound>> = repository.customSoundsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val forgeTools: StateFlow<List<ForgeTool>> = repository.forgeToolsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val denConfig: StateFlow<DenConfig> = repository.denConfigFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DenConfig())

    val weirdLawScore: StateFlow<WeirdLawScore> = repository.weirdLawScoreFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeirdLawScore())

    val streamConnectionConfig: StateFlow<StreamConnectionConfig> = repository.streamConnectionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreamConnectionConfig())

    private val overlayServer = LocalOverlayServer()

    private val _overlayRunning = MutableStateFlow(false)
    val overlayRunning: StateFlow<Boolean> = _overlayRunning.asStateFlow()

    private val _overlayPort = MutableStateFlow(8765)
    val overlayPort: StateFlow<Int> = _overlayPort.asStateFlow()

    private val _localIpAddress = MutableStateFlow("127.0.0.1")
    val localIpAddress: StateFlow<String> = _localIpAddress.asStateFlow()

    private val _overlayError = MutableStateFlow<String?>(null)
    val overlayError: StateFlow<String?> = _overlayError.asStateFlow()

    private val _barsHidden = MutableStateFlow(false)
    val barsHidden: StateFlow<Boolean> = _barsHidden.asStateFlow()

    private val _showHelpDialog = MutableStateFlow(false)
    val showHelpDialog: StateFlow<Boolean> = _showHelpDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _aiTestStatus = MutableStateFlow<String?>(null)
    val aiTestStatus: StateFlow<String?> = _aiTestStatus.asStateFlow()

    private val _soundUploadStatus = MutableStateFlow<String?>(null)
    val soundUploadStatus: StateFlow<String?> = _soundUploadStatus.asStateFlow()

    private val _activeDenAlert = MutableStateFlow<DenAlertEvent?>(null)
    val activeDenAlert: StateFlow<DenAlertEvent?> = _activeDenAlert.asStateFlow()

    init {
        viewModelScope.launch {
            denConfig.collect { LiveOverlayBus.updateDenConfig(it) }
        }
        viewModelScope.launch {
            streamConnectionConfig.collect { LiveOverlayBus.updateConnectionConfig(it) }
        }
        refreshNetworkInfo()
    }

    fun setBarsHidden(hidden: Boolean) {
        _barsHidden.value = hidden
    }

    fun openHelpDialog(open: Boolean) {
        _showHelpDialog.value = open
    }

    fun openSettingsDialog(open: Boolean) {
        _aiTestStatus.value = null
        _soundUploadStatus.value = null
        _showSettingsDialog.value = open
    }

    fun saveStreamerName(name: String) {
        viewModelScope.launch { repository.saveStreamerName(name) }
    }

    fun saveAiConfig(config: AiConfig) {
        viewModelScope.launch { repository.saveAiConfig(config) }
    }

    fun testAiConnection(config: AiConfig) {
        viewModelScope.launch {
            _aiTestStatus.value = "Testing connection…"
            val res = OpenAiClient.testConnection(config)
            _aiTestStatus.value = res.fold(
                onSuccess = { "Connected ✓ ($it)" },
                onFailure = { "Error: ${it.message ?: "Unable to connect"}" }
            )
        }
    }

    fun importCustomSound(uri: Uri, displayName: String) {
        viewModelScope.launch {
            val res = repository.importCustomSound(uri, displayName)
            _soundUploadStatus.value = res.fold(
                onSuccess = { "Added \"${it.name}\" to Soundboard!" },
                onFailure = { "Failed: ${it.message ?: "Could not read audio"}" }
            )
        }
    }

    fun deleteCustomSound(sound: CustomSound) {
        viewModelScope.launch { repository.deleteCustomSound(sound) }
    }

    fun recordWeirdLawAnswer(isCorrect: Boolean) {
        viewModelScope.launch {
            val cur = weirdLawScore.value
            val nextScore = if (isCorrect) cur.score + 1 else (cur.score - 1).coerceAtLeast(0)
            val nextStreak = if (isCorrect) cur.streak + 1 else 0
            val nextBest = maxOf(cur.bestStreak, nextStreak)
            repository.saveWeirdLawScore(
                WeirdLawScore(
                    score = nextScore,
                    streak = nextStreak,
                    bestStreak = nextBest,
                    playedCount = cur.playedCount + 1
                )
            )
        }
    }

    fun saveDenConfig(config: DenConfig) {
        viewModelScope.launch { repository.saveDenConfig(config) }
    }

    fun saveStreamConnectionConfig(config: StreamConnectionConfig) {
        LiveOverlayBus.updateConnectionConfig(config)
        viewModelScope.launch { repository.saveStreamConnectionConfig(config) }
    }

    fun refreshNetworkInfo() {
        viewModelScope.launch {
            val ip = withContext(Dispatchers.IO) { LocalOverlayServer.resolveLocalIpAddress() }
            _localIpAddress.value = ip
        }
    }

    fun setOverlayServerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _overlayError.value = null
            if (enabled) {
                val ip = withContext(Dispatchers.IO) { LocalOverlayServer.resolveLocalIpAddress() }
                _localIpAddress.value = ip
                val res = withContext(Dispatchers.IO) {
                    overlayServer.start(streamConnectionConfig.value.overlayPort)
                }
                res.onSuccess { port ->
                    _overlayPort.value = port
                    _overlayRunning.value = true
                }.onFailure { err ->
                    _overlayRunning.value = false
                    _overlayError.value = err.message ?: "Unable to start local overlay server."
                }
            } else {
                withContext(Dispatchers.IO) { overlayServer.stop() }
                _overlayRunning.value = false
            }
        }
    }

    fun fireDenAlert(kind: String, viewerName: String, message: String, amount: String) {
        val cleanName = viewerName.trim().ifBlank { "NightOwl_77" }
        val event = DenAlertEvent(
            id = System.currentTimeMillis(),
            kind = kind,
            viewerName = cleanName,
            message = message.trim(),
            amount = amount.trim()
        )
        SoundSynth.play("alert_$kind")
        _activeDenAlert.value = event
        LiveOverlayBus.updateActiveAlert(event)
        viewModelScope.launch {
            delay(4600)
            if (_activeDenAlert.value?.id == event.id) {
                _activeDenAlert.value = null
                LiveOverlayBus.updateActiveAlert(null)
            }
        }
    }

    fun dismissDenAlert() {
        _activeDenAlert.value = null
        LiveOverlayBus.updateActiveAlert(null)
    }

    fun upsertForgeTool(tool: ForgeTool) {
        viewModelScope.launch { repository.upsertForgeTool(tool) }
    }

    fun deleteForgeTool(id: String) {
        viewModelScope.launch { repository.deleteForgeTool(id) }
    }

    override fun onCleared() {
        overlayServer.stop()
        super.onCleared()
    }

    suspend fun askOptionalAi(
        systemPrompt: String,
        history: List<ChatTurn> = emptyList(),
        userMessage: String,
        temperature: Float = 0.75f
    ): Result<String> {
        return if (aiConfig.value.isEnabled) {
            OpenAiClient.chatCompletion(
                config = aiConfig.value,
                systemPrompt = systemPrompt,
                history = history,
                userMessage = userMessage,
                temperature = temperature
            )
        } else {
            com.streamwarrior.app.ai.FirebaseAiService.generateReply(
                systemPrompt = systemPrompt,
                history = history,
                userMessage = userMessage
            )
        }
    }

    companion object {
        fun factory(repository: SettingsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return StreamWarriorViewModel(repository) as T
                }
            }
    }
}

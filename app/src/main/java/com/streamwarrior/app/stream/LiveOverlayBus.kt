package com.streamwarrior.app.stream

import com.streamwarrior.app.data.DenAlertEvent
import com.streamwarrior.app.data.DenConfig
import com.streamwarrior.app.data.StreamConnectionConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

data class LiveOverlaySnapshot(
    val streamerName: String = "Redeemed Warrior",
    val tagline: String = "Walking in it. \uD83D\uDD25",
    val tickerMessages: List<String> = DenConfig().tickerMessages,
    val activeAlert: DenAlertEvent? = null,
    val voicePersona: String = "Hype Co-Host",
    val voiceStatus: String = "IDLE",
    val voiceUserCaption: String = "",
    val voiceAiCaption: String = "",
    val activityBanner: String = "",
    val connectionConfig: StreamConnectionConfig = StreamConnectionConfig()
) {
    fun toJsonString(): String {
        val alertJson = activeAlert?.let { ev ->
            buildJsonObject {
                put("id", ev.id)
                put("kind", ev.kind)
                put("viewerName", ev.viewerName)
                put("message", ev.message)
                put("amount", ev.amount)
            }
        }
        return buildJsonObject {
            put("streamerName", streamerName)
            put("tagline", tagline)
            put("tickerMessages", JsonArray(tickerMessages.map { JsonPrimitive(it) }))
            if (alertJson != null) {
                put("activeAlert", alertJson)
            }
            put("voicePersona", voicePersona)
            put("voiceStatus", voiceStatus)
            put("voiceUserCaption", voiceUserCaption)
            put("voiceAiCaption", voiceAiCaption)
            put("activityBanner", activityBanner)
            put("showCornerBadge", connectionConfig.showCornerBadge)
            put("showAlerts", connectionConfig.showAlerts)
            put("showVoiceCaptions", connectionConfig.showVoiceCaptions)
            put("showTicker", connectionConfig.showTicker)
        }.toString()
    }
}

object LiveOverlayBus {
    private val _snapshot = MutableStateFlow(LiveOverlaySnapshot())
    val snapshot: StateFlow<LiveOverlaySnapshot> = _snapshot.asStateFlow()

    fun updateDenConfig(config: DenConfig) {
        _snapshot.update {
            it.copy(
                streamerName = config.streamerName,
                tagline = config.tagline,
                tickerMessages = config.tickerMessages
            )
        }
    }

    fun updateConnectionConfig(config: StreamConnectionConfig) {
        _snapshot.update { it.copy(connectionConfig = config) }
    }

    fun updateActiveAlert(alert: DenAlertEvent?) {
        _snapshot.update { it.copy(activeAlert = alert) }
    }

    fun updateVoiceCaptions(
        persona: String,
        status: String,
        userCaption: String,
        aiCaption: String
    ) {
        _snapshot.update {
            it.copy(
                voicePersona = persona,
                voiceStatus = status,
                voiceUserCaption = userCaption,
                voiceAiCaption = aiCaption
            )
        }
    }

    fun updateActivityBanner(banner: String) {
        _snapshot.update { it.copy(activityBanner = banner) }
    }
}

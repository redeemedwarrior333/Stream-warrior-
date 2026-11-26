package com.streamwarrior.app.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomSound(
    val id: String,
    val name: String,
    val filePath: String
)

@Serializable
data class ForgeTool(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val prompt: String,
    val temperature: Float = 0.7f
)

@Serializable
data class TipLink(
    val id: String,
    val label: String,
    val url: String
)

@Serializable
data class DenConfig(
    val streamerName: String = "Redeemed Warrior",
    val tagline: String = "Walking in it. \uD83D\uDD25",
    val tickerMessages: List<String> = listOf(
        "Welcome to the Den \uD83D\uDD25",
        "Follow so you don't miss the next live",
        "New tool every week \u2014 Warrior Forge",
        "Stay blessed, stay dangerous \uD83D\uDE4F"
    ),
    val tipLinks: List<TipLink> = emptyList()
)

@Serializable
data class WeirdLawScore(
    val score: Int = 0,
    val streak: Int = 0,
    val bestStreak: Int = 0,
    val playedCount: Int = 0
)

@Serializable
data class AiConfig(
    val apiKey: String = "",
    val baseUrl: String = "https://api.mistral.ai/v1",
    val model: String = "mistral-small-latest"
) {
    val isEnabled: Boolean get() = apiKey.isNotBlank()
}

data class AiProviderPreset(
    val id: String,
    val label: String,
    val baseUrl: String,
    val defaultModel: String,
    val helperNote: String
)

val AI_PROVIDER_PRESETS = listOf(
    AiProviderPreset(
        id = "mistral",
        label = "Mistral AI",
        baseUrl = "https://api.mistral.ai/v1",
        defaultModel = "mistral-small-latest",
        helperNote = "Free tier available at console.mistral.ai"
    ),
    AiProviderPreset(
        id = "groq",
        label = "Groq",
        baseUrl = "https://api.groq.com/openai/v1",
        defaultModel = "llama-3.1-8b-instant",
        helperNote = "Fast inference with console.groq.com key"
    ),
    AiProviderPreset(
        id = "openai",
        label = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        defaultModel = "gpt-4o-mini",
        helperNote = "Standard OpenAI API key"
    ),
    AiProviderPreset(
        id = "openrouter",
        label = "OpenRouter",
        baseUrl = "https://openrouter.ai/api/v1",
        defaultModel = "meta-llama/llama-3.1-8b-instruct:free",
        helperNote = "Multi-model router key"
    )
)

data class DenAlertEvent(
    val id: Long,
    val kind: String,
    val viewerName: String,
    val message: String,
    val amount: String
)

@Serializable
data class StreamConnectionConfig(
    val platformId: String = "tiktok",
    val channelHandle: String = "",
    val overlayPort: Int = 8765,
    val showCornerBadge: Boolean = true,
    val showAlerts: Boolean = true,
    val showVoiceCaptions: Boolean = true,
    val showTicker: Boolean = true,
    val keepScreenAwake: Boolean = true,
    val launchSplitScreen: Boolean = false,
    val selectedStageId: String = "voice_ai"
)

data class StreamPlatformPreset(
    val id: String,
    val emoji: String,
    val label: String,
    val urlTemplate: String,
    val handleHint: String,
    val packageNames: List<String>,
    val screenCastSteps: String,
    val supportsMobileWebWidget: Boolean = false
) {
    fun buildChannelUrl(rawHandle: String): String {
        val clean = rawHandle.trim().removePrefix("@")
        if (clean.startsWith("http://") || clean.startsWith("https://")) return clean
        return if (clean.isBlank()) {
            urlTemplate.substringBefore("{handle}").trimEnd('/')
        } else {
            urlTemplate.replace("{handle}", clean)
        }
    }
}

val STREAM_PLATFORMS = listOf(
    StreamPlatformPreset(
        id = "tiktok",
        emoji = "\uD83C\uDFB5",
        label = "TikTok",
        urlTemplate = "https://www.tiktok.com/@{handle}/live",
        handleHint = "@yourhandle",
        packageNames = listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"),
        screenCastSteps = "In TikTok: tap + -> LIVE -> Mobile Gaming (Screen Cast) -> Go Live, then return here so viewers see Stream Warrior."
    ),
    StreamPlatformPreset(
        id = "twitch",
        emoji = "\uD83D\uDFE3",
        label = "Twitch",
        urlTemplate = "https://www.twitch.tv/{handle}",
        handleHint = "yourchannel",
        packageNames = listOf("tv.twitch.android.app"),
        screenCastSteps = "In Twitch: tap Create (+) -> Stream Games -> Start Stream, then switch back to Stream Warrior."
    ),
    StreamPlatformPreset(
        id = "kick",
        emoji = "\uD83D\uDFE2",
        label = "Kick",
        urlTemplate = "https://kick.com/{handle}",
        handleHint = "yourchannel",
        packageNames = listOf("com.kick.mobile"),
        screenCastSteps = "In Kick (or via PRISM RTMP to Kick): tap Go Live -> Screen Share, then switch back to Stream Warrior."
    ),
    StreamPlatformPreset(
        id = "prism",
        emoji = "\uD83C\uDF08",
        label = "PRISM Live",
        urlTemplate = "https://prismlive.com/",
        handleHint = "your PRISM / RTMP channel",
        packageNames = listOf("com.prism.live"),
        screenCastSteps = "In PRISM Live Studio: pick Screen Cast to show this app, OR add Web Widget http://127.0.0.1:8765/ right over your camera feed!",
        supportsMobileWebWidget = true
    ),
    StreamPlatformPreset(
        id = "meetme",
        emoji = "\uD83D\uDCAC",
        label = "MeetMe",
        urlTemplate = "https://www.meetme.com/",
        handleHint = "your MeetMe username",
        packageNames = listOf("com.myyearbook.m"),
        screenCastSteps = "In MeetMe: tap Go Live -> Screen Share / Broadcast Screen, then return here so your viewers see Stream Warrior."
    ),
    StreamPlatformPreset(
        id = "youtube",
        emoji = "\u25B6\uFE0F",
        label = "YouTube",
        urlTemplate = "https://www.youtube.com/@{handle}/live",
        handleHint = "@yourhandle",
        packageNames = listOf("com.google.android.youtube"),
        screenCastSteps = "In YouTube: tap + -> Go Live -> Screencast icon at top -> Start, then return to Stream Warrior."
    )
)

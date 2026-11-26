package com.streamwarrior.app.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.streamwarrior.app.R
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.STREAM_PLATFORMS
import com.streamwarrior.app.data.StreamConnectionConfig
import com.streamwarrior.app.data.StreamPlatformPreset
import com.streamwarrior.app.ui.AppRoute
import com.streamwarrior.app.ui.routeFromModuleId
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.CyanAccent
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary

private val BROADCAST_STAGES = listOf(
    "voice_ai" to "\uD83C\uDF99\uFE0F Voice AI",
    "den" to "\uD83D\uDD34 Den Stage",
    "wheel" to "\uD83C\uDFA1 Wheel",
    "tarot" to "\uD83D\uDD2E Tarot",
    "soundboard" to "\uD83D\uDD0A Soundboard"
)

@Composable
fun StreamConnectScreen(
    streamerName: String,
    connectionConfig: StreamConnectionConfig,
    overlayRunning: Boolean,
    overlayPort: Int,
    localIpAddress: String,
    overlayError: String?,
    onSaveConnectionConfig: (StreamConnectionConfig) -> Unit,
    onToggleOverlayServer: (Boolean) -> Unit,
    onRefreshNetwork: () -> Unit,
    onSendTestAlert: () -> Unit,
    onAudioCheck: () -> Unit,
    onStartCleanBroadcast: (AppRoute) -> Unit
) {
    val context = LocalContext.current
    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var statusBanner by remember { mutableStateOf<String?>(null) }
    var handleInput by remember(connectionConfig.channelHandle) {
        mutableStateOf(connectionConfig.channelHandle)
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted
        statusBanner = if (granted) {
            "Microphone enabled — Voice AI Co-Host is ready!"
        } else {
            "Mic not granted yet. You can still use typed prompts, overlays, and soundboard."
        }
    }

    val selectedPlatform = STREAM_PLATFORMS.firstOrNull { it.id == connectionConfig.platformId }
        ?: STREAM_PLATFORMS.first()

    val installedLaunchIntent = remember(selectedPlatform.id) {
        findInstalledPlatformIntent(context, selectedPlatform)
    }
    val selectedStageRoute = remember(connectionConfig.selectedStageId) {
        routeFromModuleId(connectionConfig.selectedStageId)
    }

    val overlayUrl = "http://$localIpAddress:$overlayPort/"
    val samePhoneUrl = "http://127.0.0.1:$overlayPort/"
    val captionsUrl = "http://$localIpAddress:$overlayPort/captions"

    fun launchLiveScreenShare(platform: StreamPlatformPreset) {
        if (platform.supportsMobileWebWidget && !overlayRunning) {
            onToggleOverlayServer(true)
        }
        onStartCleanBroadcast(selectedStageRoute)
        val appIntent = findInstalledPlatformIntent(context, platform)
        if (appIntent != null) {
            if (connectionConfig.launchSplitScreen) {
                appIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
            } else {
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val launched = runCatching { context.startActivity(appIntent) }.isSuccess
            statusBanner = if (launched) {
                "Opened ${platform.label}! Start Screen Share / Cast there, then return to Stream Warrior — Clean Screen & Keep Awake are active."
            } else {
                "Could not launch ${platform.label} app directly."
            }
        } else {
            val webUrl = platform.buildChannelUrl(handleInput)
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                if (connectionConfig.launchSplitScreen) {
                    addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            runCatching { context.startActivity(webIntent) }
            statusBanner = "${platform.label} app wasn't found locally — opened $webUrl. Clean Screen is ready once you start sharing!"
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 820.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: Direct Screen-Share Link to TikTok, Twitch, Kick, PRISM, MeetMe
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.55f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(VioletPrimary.copy(alpha = 0.28f), CardSurface)
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (overlayRunning) EmeraldGood else CrimsonAlert)
                        )
                        Text(
                            text = if (overlayRunning) "LIVE LINK ACTIVE • SCREEN READY" else "SHARE SCREEN TO LIVE STREAM",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (overlayRunning) EmeraldGood else GoldAccent
                        )
                    }

                    Text(
                        text = "Link to TikTok, Twitch, Kick, PRISM & MeetMe",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Pick your live app, choose what viewers see on your screen, and launch in 1 tap. Stream Warrior automatically hides bars and keeps your screen awake so your live stream sees your screen cleanly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    // Platform selector chips with installed detection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        STREAM_PLATFORMS.forEach { platform ->
                            val isInstalled = remember(platform.id) {
                                findInstalledPlatformIntent(context, platform) != null
                            }
                            FilterChip(
                                selected = connectionConfig.platformId == platform.id,
                                onClick = {
                                    onSaveConnectionConfig(connectionConfig.copy(platformId = platform.id))
                                },
                                label = {
                                    Text(
                                        if (isInstalled) "${platform.emoji} ${platform.label} \u2713"
                                        else "${platform.emoji} ${platform.label}"
                                    )
                                },
                                modifier = Modifier.testTag("connect_platform_${platform.id}")
                            )
                        }
                    }

                    // Dynamic Screen Cast Instructions for the selected platform
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${selectedPlatform.emoji} ${selectedPlatform.label} Screen-Share Setup" +
                                        if (installedLaunchIntent != null) " (App Installed)" else " (Web / Store Link)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyanAccent
                                )
                            }
                            Text(
                                text = selectedPlatform.screenCastSteps,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            if (selectedPlatform.supportsMobileWebWidget) {
                                OutlinedButton(
                                    onClick = {
                                        if (!overlayRunning) onToggleOverlayServer(true)
                                        copyText(context, "PRISM Web Widget URL", samePhoneUrl)
                                        statusBanner = "Overlay started & copied $samePhoneUrl for PRISM Web Widget!"
                                    },
                                    modifier = Modifier.testTag("connect_prism_widget_copy")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Copy PRISM Same-Phone URL ($samePhoneUrl)")
                                }
                            }
                        }
                    }

                    // Pick which stage viewers see when screen sharing
                    Text(
                        text = "WHAT YOUR LIVE STREAM SEES ON SCREEN",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BROADCAST_STAGES.forEach { (stageId, label) ->
                            FilterChip(
                                selected = connectionConfig.selectedStageId == stageId,
                                onClick = {
                                    onSaveConnectionConfig(connectionConfig.copy(selectedStageId = stageId))
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("connect_stage_$stageId")
                            )
                        }
                    }

                    // Screen Awake & Split-Screen options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Keep Awake", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                Switch(
                                    checked = connectionConfig.keepScreenAwake,
                                    onCheckedChange = {
                                        onSaveConnectionConfig(connectionConfig.copy(keepScreenAwake = it))
                                    },
                                    modifier = Modifier.testTag("connect_keep_awake_switch")
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Split-Screen", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                                Switch(
                                    checked = connectionConfig.launchSplitScreen,
                                    onCheckedChange = {
                                        onSaveConnectionConfig(connectionConfig.copy(launchSplitScreen = it))
                                    },
                                    modifier = Modifier.testTag("connect_split_screen_switch")
                                )
                            }
                        }
                    }

                    // Optional handle/URL field + Launch CTAs
                    OutlinedTextField(
                        value = handleInput,
                        onValueChange = {
                            handleInput = it
                            onSaveConnectionConfig(connectionConfig.copy(channelHandle = it))
                        },
                        label = { Text("${selectedPlatform.label} Handle or Channel Link (${selectedPlatform.handleHint})") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("connect_handle_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchLiveScreenShare(selectedPlatform) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("connect_open_platform_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Link & Open ${selectedPlatform.label}", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onStartCleanBroadcast(selectedStageRoute) },
                            modifier = Modifier.testTag("connect_clean_stage_now_button")
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Clean Screen")
                        }
                    }
                }
            }

            if (!statusBanner.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGood.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, EmeraldGood.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusBanner.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = EmeraldGood,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Card 2: Pre-Flight Readiness Checklist
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PRE-FLIGHT STREAM CHECK",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent
                        )
                        OutlinedButton(
                            onClick = onRefreshNetwork,
                            modifier = Modifier.testTag("connect_refresh_network")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Refresh IP")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadinessChip(
                            title = if (localIpAddress != "127.0.0.1") "Wi-Fi: $localIpAddress" else "Local Device",
                            ready = localIpAddress != "127.0.0.1",
                            modifier = Modifier.weight(1f)
                        )
                        ReadinessChip(
                            title = "Gemini AI Ready",
                            ready = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!micGranted) {
                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                } else {
                                    statusBanner = "Mic permission already granted!"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("connect_mic_check_button")
                        ) {
                            Icon(
                                imageVector = if (micGranted) Icons.Default.CheckCircle else Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (micGranted) EmeraldGood else GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (micGranted) "Mic Ready" else "Grant Mic")
                        }
                        OutlinedButton(
                            onClick = {
                                SoundSynth.play("tada")
                                onAudioCheck()
                                statusBanner = "Played stream audio check chime + voice test."
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("connect_audio_check_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Test Audio")
                        }
                    }
                }
            }

            // Card 3: PRISM / OBS / TikTok Live Studio Web Overlay Server
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, if (overlayRunning) EmeraldGood.copy(alpha = 0.65f) else CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Cast, contentDescription = null, tint = CyanAccent)
                                Text(
                                    text = "PRISM / OBS / Live Studio Overlay",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = "Streams live alerts, ticker, and Voice AI captions as a transparent Web Widget (PRISM) or Browser Source (OBS / TikTok Live Studio / Kick).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = overlayRunning,
                            onCheckedChange = onToggleOverlayServer,
                            modifier = Modifier.testTag("connect_overlay_switch")
                        )
                    }

                    if (!overlayError.isNullOrBlank()) {
                        Text(
                            text = overlayError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CrimsonAlert
                        )
                    }

                    if (overlayRunning) {
                        OverlayUrlBox(
                            label = "SAME-PHONE WEB WIDGET URL (PRISM Live Studio on Android)",
                            url = samePhoneUrl,
                            onCopy = {
                                copyText(context, "PRISM Same-Phone Widget URL", samePhoneUrl)
                                statusBanner = "Copied same-phone URL: $samePhoneUrl"
                            },
                            onShare = {
                                shareText(context, "Stream Warrior Mobile Overlay URL:\n$samePhoneUrl")
                            },
                            tagPrefix = "local"
                        )

                        OverlayUrlBox(
                            label = "WI-FI PC OVERLAY URL (OBS / Streamlabs / TikTok Live Studio)",
                            url = overlayUrl,
                            onCopy = {
                                copyText(context, "Stream Warrior Overlay URL", overlayUrl)
                                statusBanner = "Copied Wi-Fi overlay URL: $overlayUrl"
                            },
                            onShare = {
                                shareText(context, "Stream Warrior OBS Overlay URL:\n$overlayUrl")
                            },
                            tagPrefix = "full"
                        )

                        OverlayUrlBox(
                            label = "VOICE AI CAPTIONS-ONLY URL",
                            url = captionsUrl,
                            onCopy = {
                                copyText(context, "Stream Warrior Captions URL", captionsUrl)
                                statusBanner = "Copied captions URL: $captionsUrl"
                            },
                            onShare = {
                                shareText(context, "Stream Warrior Voice AI Captions URL:\n$captionsUrl")
                            },
                            tagPrefix = "captions"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(samePhoneUrl)))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("connect_preview_overlay_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Preview Browser")
                            }
                            Button(
                                onClick = {
                                    onSendTestAlert()
                                    statusBanner = "Sent live test alert to overlay & Den!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PinkAccent, contentColor = TextPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("connect_test_alert_button")
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Test Alert")
                            }
                        }
                    }

                    // Overlay Layer Toggles
                    Text(
                        text = "OVERLAY LAYERS",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = connectionConfig.showCornerBadge,
                            onClick = {
                                onSaveConnectionConfig(connectionConfig.copy(showCornerBadge = !connectionConfig.showCornerBadge))
                            },
                            label = { Text("Live Badge") },
                            modifier = Modifier.testTag("connect_layer_badge")
                        )
                        FilterChip(
                            selected = connectionConfig.showAlerts,
                            onClick = {
                                onSaveConnectionConfig(connectionConfig.copy(showAlerts = !connectionConfig.showAlerts))
                            },
                            label = { Text("Alerts") },
                            modifier = Modifier.testTag("connect_layer_alerts")
                        )
                        FilterChip(
                            selected = connectionConfig.showVoiceCaptions,
                            onClick = {
                                onSaveConnectionConfig(connectionConfig.copy(showVoiceCaptions = !connectionConfig.showVoiceCaptions))
                            },
                            label = { Text("AI Captions") },
                            modifier = Modifier.testTag("connect_layer_captions")
                        )
                        FilterChip(
                            selected = connectionConfig.showTicker,
                            onClick = {
                                onSaveConnectionConfig(connectionConfig.copy(showTicker = !connectionConfig.showTicker))
                            },
                            label = { Text("Ticker") },
                            modifier = Modifier.testTag("connect_layer_ticker")
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun findInstalledPlatformIntent(
    context: Context,
    platform: StreamPlatformPreset
): Intent? {
    val pm = context.packageManager
    for (pkg in platform.packageNames) {
        val intent = pm.getLaunchIntentForPackage(pkg)
        if (intent != null) return intent
    }
    return null
}

@Composable
private fun ReadinessChip(
    title: String,
    ready: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, if (ready) EmeraldGood.copy(alpha = 0.55f) else GoldAccent.copy(alpha = 0.45f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (ready) EmeraldGood else GoldAccent)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OverlayUrlBox(
    label: String,
    url: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    tagPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardSurfaceElevated,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = GoldAccent)
            Text(text = url, style = MaterialTheme.typography.titleMedium, color = CyanAccent)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.testTag("connect_copy_${tagPrefix}_url")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.cd_copy_text), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Copy URL")
                }
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("connect_share_${tagPrefix}_url")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Send to PC")
                }
            }
        }
    }
}

private fun copyText(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun shareText(context: Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Share Stream Overlay URL"))
    }
}

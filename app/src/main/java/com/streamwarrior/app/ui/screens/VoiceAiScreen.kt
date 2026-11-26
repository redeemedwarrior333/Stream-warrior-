package com.streamwarrior.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.streamwarrior.app.R
import com.streamwarrior.app.ui.VoiceAiViewModel
import com.streamwarrior.app.ui.VoiceUiState
import com.streamwarrior.app.ui.theme.BgDeep
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary
import com.streamwarrior.app.voice.VoiceChatMessage
import com.streamwarrior.app.voice.VoicePersonas
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VoiceAiScreen(
    streamerName: String,
    vm: VoiceAiViewModel
) {
    val context = LocalContext.current
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val persona by vm.selectedPersona.collectAsStateWithLifecycle()
    val tonePreset by vm.tonePreset.collectAsStateWithLifecycle()
    val speakAloud by vm.speakAloud.collectAsStateWithLifecycle()
    val handsFreeLoop by vm.handsFreeLoop.collectAsStateWithLifecycle()
    val messages by vm.messages.collectAsStateWithLifecycle()

    var typedPrompt by remember { mutableStateOf("") }
    var micPermissionDenied by remember { mutableStateOf(false) }

    LaunchedEffect(streamerName) {
        vm.updateStreamerName(streamerName)
    }

    DisposableEffect(Unit) {
        onDispose {
            vm.stopAll()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            vm.startVoiceListening()
        } else {
            micPermissionDenied = true
        }
    }

    fun requestMicAndListen() {
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) {
            micPermissionDenied = false
            vm.startVoiceListening()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun handleOrbClick() {
        when (uiState) {
            is VoiceUiState.Idle,
            is VoiceUiState.Error -> requestMicAndListen()
            is VoiceUiState.Listening -> vm.finishListeningNow()
            is VoiceUiState.Speaking -> requestMicAndListen() // Barge-in interrupt!
            is VoiceUiState.Thinking,
            is VoiceUiState.Streaming -> vm.stopAll()
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
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Broadcast Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, persona.accentColor.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (uiState) {
                                            is VoiceUiState.Listening -> Color(0xFF22D3EE)
                                            is VoiceUiState.Speaking -> PinkAccent
                                            is VoiceUiState.Thinking,
                                            is VoiceUiState.Streaming -> GoldAccent
                                            is VoiceUiState.Error -> CrimsonAlert
                                            VoiceUiState.Idle -> EmeraldGood
                                        }
                                    )
                            )
                            Text(
                                text = "ON AIR \u2022 VOICE AI CO-HOST",
                                style = MaterialTheme.typography.labelLarge,
                                color = persona.accentColor,
                                letterSpacing = 1.2.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "${persona.emoji} ${persona.name}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${persona.tagline} \u2022 Powered by Gemini AI Logic",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text(
                            text = if (vm.isSpeechRecognitionAvailable) "Mic Ready" else "Text + TTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (vm.isSpeechRecognitionAvailable) EmeraldGood else GoldAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Persona Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoicePersonas.all.forEach { item ->
                    val selected = item.id == persona.id
                    FilterChip(
                        selected = selected,
                        onClick = { vm.selectPersona(item) },
                        label = { Text("${item.emoji} ${item.name}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = item.accentColor.copy(alpha = 0.22f),
                            selectedLabelColor = TextPrimary
                        ),
                        modifier = Modifier.testTag("voice_persona_${item.id}")
                    )
                }
            }

            // Center Interactive Voice Stage + Waveform Orb
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    persona.accentColor.copy(alpha = 0.14f),
                                    CardSurface
                                )
                            )
                        )
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VoiceStageOrb(
                        uiState = uiState,
                        accent = persona.accentColor,
                        onClick = ::handleOrbClick
                    )

                    val statusLabel = when (val s = uiState) {
                        VoiceUiState.Idle ->
                            "Tap the Mic Orb and talk naturally on stream"
                        is VoiceUiState.Listening ->
                            if (s.partialTranscript.isBlank()) "Listening live\u2026 speak now (tap orb to send)" else "\"${s.partialTranscript}\""
                        is VoiceUiState.Thinking ->
                            "Hearing: \"${s.userTranscript}\" \u2014 Gemini is thinking\u2026"
                        is VoiceUiState.Streaming ->
                            "Streaming live reply\u2026"
                        is VoiceUiState.Speaking ->
                            "Speaking on air \u2014 tap orb anytime to interrupt"
                        is VoiceUiState.Error ->
                            s.message
                    }

                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (uiState is VoiceUiState.Error) CrimsonAlert else TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voice_status_text")
                    )

                    // Permission notice if denied
                    AnimatedVisibility(visible = micPermissionDenied) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CrimsonAlert.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.voice_mic_disclosure),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                OutlinedButton(
                                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                    modifier = Modifier.testTag("voice_retry_permission_button")
                                ) {
                                    Text("Enable Microphone")
                                }
                            }
                        }
                    }

                    // Error retry banner
                    if (uiState is VoiceUiState.Error) {
                        Button(
                            onClick = ::requestMicAndListen,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                            modifier = Modifier.testTag("voice_error_retry_button")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Try Mic Again", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Controls strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        FilterChip(
                            selected = speakAloud,
                            onClick = vm::toggleSpeakAloud,
                            leadingIcon = {
                                Icon(
                                    imageVector = if (speakAloud) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(if (speakAloud) "Voice ON" else "Voice Muted") },
                            modifier = Modifier.testTag("voice_toggle_tts")
                        )
                        FilterChip(
                            selected = handsFreeLoop,
                            onClick = vm::toggleHandsFreeLoop,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Autorenew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(if (handsFreeLoop) "Auto-Loop ON" else "Push-to-Talk") },
                            modifier = Modifier.testTag("voice_toggle_loop")
                        )
                        FilterChip(
                            selected = false,
                            onClick = vm::cycleTonePreset,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Tone: ${tonePreset.label}") },
                            modifier = Modifier.testTag("voice_cycle_tone")
                        )
                        if (uiState !is VoiceUiState.Idle) {
                            OutlinedButton(
                                onClick = vm::stopAll,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("voice_stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = stringResource(R.string.cd_stop_voice),
                                    tint = CrimsonAlert,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Stop", color = CrimsonAlert)
                            }
                        } else {
                            OutlinedButton(
                                onClick = vm::resetConversation,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("voice_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = stringResource(R.string.cd_clear_chat),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }
                    }
                }
            }

            // Broadcast Live Teleprompter Caption Card
            LiveStreamCaptionCard(
                uiState = uiState,
                personaName = "${persona.emoji} ${persona.name}",
                accent = persona.accentColor,
                latestMessages = messages
            )

            // Quick 1-Tap Stream Voice Prompts
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1-TAP STREAM TRIGGERS (AI SPEAKS BACK)",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoicePersonas.quickPrompts.forEachIndexed { index, prompt ->
                        Surface(
                            onClick = { vm.submitTextPrompt(prompt) },
                            shape = RoundedCornerShape(12.dp),
                            color = CardSurfaceElevated,
                            border = BorderStroke(1.dp, CardBorder),
                            modifier = Modifier.testTag("voice_quick_prompt_$index")
                        ) {
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Text fallback input row (for quiet rooms or typing a cue)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedPrompt,
                    onValueChange = { typedPrompt = it },
                    placeholder = { Text("Or type a cue for ${persona.name} to speak aloud\u2026") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (typedPrompt.isNotBlank()) {
                                vm.submitTextPrompt(typedPrompt)
                                typedPrompt = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_text_input")
                )
                Button(
                    onClick = {
                        if (typedPrompt.isNotBlank()) {
                            vm.submitTextPrompt(typedPrompt)
                            typedPrompt = ""
                        }
                    },
                    enabled = typedPrompt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("voice_text_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.cd_send_message)
                    )
                }
            }

            // Recent Session Transcript
            if (messages.size > 1) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LIVE SESSION LOG",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextMuted
                        )
                        messages.takeLast(8).forEach { msg ->
                            VoiceHistoryRow(message = msg, accent = persona.accentColor)
                        }
                    }
                }
            }

            Text(
                text = stringResource(R.string.voice_mic_disclosure),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun VoiceStageOrb(
    uiState: VoiceUiState,
    accent: Color,
    onClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "voiceOrb")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val activeColor = when (uiState) {
        is VoiceUiState.Listening -> Color(0xFF22D3EE)
        is VoiceUiState.Thinking,
        is VoiceUiState.Streaming -> GoldAccent
        is VoiceUiState.Speaking -> PinkAccent
        is VoiceUiState.Error -> CrimsonAlert
        VoiceUiState.Idle -> accent
    }

    val rmsBoost = when (uiState) {
        is VoiceUiState.Listening -> ((uiState.rmsDb + 2f) / 12f).coerceIn(0f, 1f)
        is VoiceUiState.Speaking -> 0.65f
        is VoiceUiState.Streaming -> 0.4f
        else -> 0.1f
    }

    Box(
        modifier = Modifier.size(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension * 0.30f
            val maxExtra = size.minDimension * 0.17f

            // Expanding wave rings
            drawCircle(
                color = activeColor.copy(alpha = (1f - pulse) * 0.35f),
                radius = baseRadius + maxExtra * (pulse + rmsBoost * 0.4f),
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = activeColor.copy(alpha = 0.28f + rmsBoost * 0.35f),
                radius = baseRadius + maxExtra * rmsBoost * 0.55f,
                center = center
            )

            // Radial audio spokes
            val spokes = 24
            for (i in 0 until spokes) {
                val angleRad = ((i * (360f / spokes)) + spin * 0.25f) * (PI / 180.0)
                val barWave = (0.35f + 0.65f * ((sin(angleRad * 3.0 + pulse * PI * 2.0).toFloat() + 1f) / 2f)) * rmsBoost
                val rStart = baseRadius + 6.dp.toPx()
                val rEnd = rStart + (12.dp.toPx() * barWave) + 2.dp.toPx()
                drawLine(
                    color = activeColor.copy(alpha = 0.75f),
                    start = Offset(
                        x = center.x + (cos(angleRad) * rStart).toFloat(),
                        y = center.y + (sin(angleRad) * rStart).toFloat()
                    ),
                    end = Offset(
                        x = center.x + (cos(angleRad) * rEnd).toFloat(),
                        y = center.y + (sin(angleRad) * rEnd).toFloat()
                    ),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = activeColor,
            border = BorderStroke(2.dp, TextPrimary.copy(alpha = 0.7f)),
            modifier = Modifier
                .size(96.dp)
                .testTag("voice_mic_orb_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (uiState) {
                        is VoiceUiState.Speaking,
                        is VoiceUiState.Streaming -> Icons.Default.GraphicEq
                        is VoiceUiState.Listening -> Icons.Default.Stop
                        else -> Icons.Default.Mic
                    },
                    contentDescription = stringResource(R.string.cd_voice_mic_button),
                    tint = if (activeColor == GoldAccent || activeColor == Color(0xFF22D3EE)) GoldInk else TextPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveStreamCaptionCard(
    uiState: VoiceUiState,
    personaName: String,
    accent: Color,
    latestMessages: List<VoiceChatMessage>
) {
    val lastUserText = when (uiState) {
        is VoiceUiState.Listening -> uiState.partialTranscript
        is VoiceUiState.Thinking -> uiState.userTranscript
        is VoiceUiState.Streaming -> uiState.userTranscript
        is VoiceUiState.Speaking -> uiState.userTranscript
        else -> latestMessages.lastOrNull { it.isUser }?.text.orEmpty()
    }
    val liveAiText = when (uiState) {
        is VoiceUiState.Thinking -> "Thinking\u2026"
        is VoiceUiState.Streaming -> uiState.partialReply
        is VoiceUiState.Speaking -> uiState.fullReply
        else -> latestMessages.lastOrNull { !it.isUser }?.text.orEmpty()
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BgDeep.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_caption_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (lastUserText.isNotBlank()) {
                Text(
                    text = "YOU SAID: \"$lastUserText\"",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent
                )
            }
            Text(
                text = personaName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                letterSpacing = 1.2.sp
            )
            Text(
                text = liveAiText.ifBlank { "Tap the mic orb above and say anything to start riffing live." },
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun VoiceHistoryRow(
    message: VoiceChatMessage,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (message.isUser) VioletPrimary.copy(alpha = 0.8f) else CardSurfaceElevated,
            border = BorderStroke(
                1.dp,
                if (message.isUser) GoldAccent.copy(alpha = 0.45f) else accent.copy(alpha = 0.35f)
            ),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = if (message.isUser) {
                        if (message.spokenByVoice) "\uD83C\uDF99\uFE0F You (Voice)" else "You"
                    } else {
                        message.personaName ?: "AI Co-Host"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isUser) GoldAccent else accent
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
            }
        }
    }
}

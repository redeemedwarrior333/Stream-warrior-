package com.streamwarrior.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.streamwarrior.app.R
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.BiffReplyPlay
import com.streamwarrior.app.data.ChatTurn
import com.streamwarrior.app.data.CoachesRepository
import com.streamwarrior.app.data.ForgeTool
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.data.WarriorCoach
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

@Composable
fun CoachDetailScreen(
    coachId: String,
    customTools: List<ForgeTool>,
    aiConfig: AiConfig,
    onBack: () -> Unit
) {
    val coach = remember(coachId, customTools) {
        if (coachId.startsWith("custom:")) {
            val id = coachId.removePrefix("custom:")
            val tool = customTools.firstOrNull { it.id == id }
            WarriorCoach(
                id = coachId,
                name = tool?.name ?: "Custom Forge Tool",
                emoji = tool?.icon ?: "\uD83D\uDD25",
                tagline = tool?.description ?: "Forged persona",
                category = "Custom",
                systemPrompt = tool?.prompt ?: "Helpful Warrior coach.",
                quickTopics = emptyList()
            )
        } else {
            CoachesRepository.coaches.firstOrNull { it.id == coachId } ?: CoachesRepository.coaches.first()
        }
    }

    val scope = rememberCoroutineScope()
    var subTab by remember(coach.id) { mutableStateOf("chat") }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var messages by remember(coach.id) {
        mutableStateOf(
            listOf(ChatTurn("assistant", "${coach.emoji} ${coach.name} is ready. ${coach.tagline}."))
        )
    }

    // BIFF Reply Drafter state
    var theirText by remember { mutableStateOf("") }
    var biffResult by remember { mutableStateOf<BiffReplyPlay?>(null) }

    // 15s Video Script state
    var scriptTopic by remember { mutableStateOf("") }
    var scriptOutput by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("coach_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = GoldAccent
                    )
                }
                Text(
                    text = "${coach.emoji} ${coach.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GoldAccent
                )
            }

            if (coach.hasCrisisBanner) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, CrimsonAlert),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.crisis_banner_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (coach.hasWealthDisclaimer) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.wealth_disclaimer),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GoldAccent,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            if (coach.hasBiffReplyTool || coach.hasCalmMeter || coach.hasVideoScriptTool) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = subTab == "chat",
                        onClick = { subTab = "chat" },
                        label = { Text("Strategy Chat") },
                        modifier = Modifier.testTag("coach_subtab_chat")
                    )
                    if (coach.hasBiffReplyTool) {
                        FilterChip(
                            selected = subTab == "reply",
                            onClick = { subTab = "reply" },
                            label = { Text("BIFF Reply Drafter") },
                            modifier = Modifier.testTag("coach_subtab_reply")
                        )
                    }
                    if (coach.hasCalmMeter) {
                        FilterChip(
                            selected = subTab == "mic",
                            onClick = { subTab = "mic" },
                            label = { Text("Calm Meter") },
                            modifier = Modifier.testTag("coach_subtab_mic")
                        )
                    }
                    if (coach.hasVideoScriptTool) {
                        FilterChip(
                            selected = subTab == "script",
                            onClick = { subTab = "script" },
                            label = { Text("15s Script") },
                            modifier = Modifier.testTag("coach_subtab_script")
                        )
                    }
                }
            }

            when (subTab) {
                "reply" -> BiffReplySection(
                    theirText = theirText,
                    result = biffResult,
                    onTextChange = { theirText = it },
                    onDraft = {
                        if (theirText.isNotBlank()) {
                            biffResult = CoachesRepository.draftBiffReply(theirText)
                        }
                    }
                )

                "mic" -> CalmMeterSection()

                "script" -> {
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
                            Text("🎬 15-Second Promo Script Builder", style = MaterialTheme.typography.titleMedium, color = GoldAccent)
                            OutlinedTextField(
                                value = scriptTopic,
                                onValueChange = { scriptTopic = it },
                                label = { Text("What are you promoting? (TikTok, Twitch, drop…)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ads_script_input")
                            )
                            Button(
                                onClick = { scriptOutput = CoachesRepository.build15sVideoScript(scriptTopic) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ads_script_generate")
                            ) {
                                Text("Build 15s Script", fontWeight = FontWeight.Bold)
                            }
                            scriptOutput?.let { out ->
                                Text(
                                    text = out,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                else -> {
                    if (coach.quickTopics.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            coach.quickTopics.forEachIndexed { idx, topic ->
                                OutlinedButton(
                                    onClick = { input = topic },
                                    modifier = Modifier.testTag("coach_topic_$idx")
                                ) {
                                    Text(topic)
                                }
                            }
                        }
                    }

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
                            messages.forEach { turn ->
                                val isUser = turn.role == "user"
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isUser) VioletPrimary else CardSurfaceElevated
                                    ) {
                                        Text(
                                            text = turn.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text("Describe what you need help with…") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("coach_chat_input")
                        )
                        IconButton(
                            onClick = {
                                val q = input.trim()
                                if (q.isBlank() || busy) return@IconButton
                                input = ""
                                messages = messages + ChatTurn("user", q)

                                // Crisis safety guardrail runs BEFORE any AI call
                                if (CoachesRepository.containsCrisisTrigger(q)) {
                                    messages = messages + ChatTurn(
                                        "assistant",
                                        CoachesRepository.offlineCoachAnswer(coach, q)
                                    )
                                    return@IconButton
                                }

                                if (aiConfig.isEnabled) {
                                    busy = true
                                    scope.launch {
                                        val res = OpenAiClient.chatCompletion(
                                            config = aiConfig,
                                            systemPrompt = coach.systemPrompt,
                                            history = messages.dropLast(1),
                                            userMessage = q
                                        )
                                        val reply = res.getOrElse { CoachesRepository.offlineCoachAnswer(coach, q) }
                                        messages = messages + ChatTurn("assistant", reply)
                                        busy = false
                                    }
                                } else {
                                    messages = messages + ChatTurn(
                                        "assistant",
                                        CoachesRepository.offlineCoachAnswer(coach, q)
                                    )
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("coach_chat_send")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.cd_send_message),
                                tint = GoldAccent
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BiffReplySection(
    theirText: String,
    result: BiffReplyPlay?,
    onTextChange: (String) -> Unit,
    onDraft: () -> Unit
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

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
            Text("✍️ BIFF Reply Drafter", style = MaterialTheme.typography.titleMedium, color = GoldAccent)
            OutlinedTextField(
                value = theirText,
                onValueChange = onTextChange,
                label = { Text("Paste what they sent…") },
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biff_their_message_input")
            )
            Button(
                onClick = {
                    copied = false
                    onDraft()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biff_draft_button")
            ) {
                Text("Draft BIFF Reply", fontWeight = FontWeight.Bold)
            }

            result?.let { play ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.2.dp, if (play.isWarning) CrimsonAlert else GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = play.tactic,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (play.isWarning) CrimsonAlert else GoldAccent
                        )
                        Text(
                            text = play.reply,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = play.note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                        if (!play.isWarning) {
                            OutlinedButton(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText("BIFF Reply", play.reply))
                                    copied = true
                                },
                                modifier = Modifier.testTag("biff_copy_button")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.cd_copy_text), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(if (copied) "Copied ✓" else "Copy Reply")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalmMeterSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var listening by remember { mutableStateOf(false) }
    var level by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Tap Start Calm Meter — checks volume level only.") }
    var monitorJob by remember { mutableStateOf<Job?>(null) }

    fun stopMeter() {
        monitorJob?.cancel()
        monitorJob = null
        listening = false
        level = 0f
        statusText = "Stopped. Nothing was saved or recorded."
    }

    @SuppressLint("MissingPermission")
    fun startMeter() {
        if (listening) return
        val sampleRate = 8000
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(1024)
        val recorder = runCatching {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBuf
            )
        }.getOrNull()

        if (recorder == null || recorder.state != AudioRecord.STATE_INITIALIZED) {
            statusText = "Microphone unavailable on this device."
            recorder?.release()
            return
        }

        listening = true
        statusText = "Monitoring live room loudness (memory only)…"
        monitorJob = scope.launch(Dispatchers.Default) {
            val buf = ShortArray(minBuf / 2)
            try {
                recorder.startRecording()
                while (isActive) {
                    val read = recorder.read(buf, 0, buf.size)
                    if (read > 0) {
                        var sumSq = 0.0
                        for (i in 0 until read) {
                            val norm = buf[i] / 32768.0
                            sumSq += norm * norm
                        }
                        val rms = sqrt(sumSq / read).toFloat()
                        level = (rms * 4.0f).coerceIn(0f, 1f)
                    }
                }
            } catch (_: Exception) {
            } finally {
                runCatching { recorder.stop() }
                runCatching { recorder.release() }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startMeter()
        } else {
            statusText = "Microphone permission declined — nothing accessed."
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopMeter() }
    }

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
            Text("\uD83C\uDF99\uFE0F Volume-Only Calm Meter", style = MaterialTheme.typography.titleMedium, color = GoldAccent)
            Text(
                text = stringResource(R.string.mic_disclosure),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            LinearProgressIndicator(
                progress = { level },
                color = if (level > 0.6f) CrimsonAlert else EmeraldGood,
                trackColor = CardSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            )
            Text(text = statusText, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (level > 0.6f) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CrimsonAlert.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, CrimsonAlert)
                ) {
                    Text(
                        text = "Loud spike detected. Pause, breathe, step into another room if needed. Safety first: 911 • 1-800-799-7233.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
            Button(
                onClick = {
                    if (listening) {
                        stopMeter()
                    } else {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) startMeter() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (listening) CrimsonAlert else GoldAccent,
                    contentColor = if (listening) TextPrimary else GoldInk
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calm_meter_toggle_button")
            ) {
                Text(if (listening) "Stop Meter" else "Start Calm Meter", fontWeight = FontWeight.Bold)
            }
        }
    }
}

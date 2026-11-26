package com.streamwarrior.app.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streamwarrior.app.R
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.ChatTurn
import com.streamwarrior.app.data.GamesRepository
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary
import kotlinx.coroutines.launch

@Composable
fun TwentyQuestionsScreen(
    aiConfig: AiConfig
) {
    val scope = rememberCoroutineScope()
    var target by remember { mutableStateOf(GamesRepository.twentyQEntities.random()) }
    var questionsUsed by remember { mutableIntStateOf(0) }
    var solved by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var useAiHost by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var log by remember {
        mutableStateOf(
            listOf(ChatTurn("assistant", "\u2753 I'm thinking of a secret person, place, or thing! Ask yes/no questions or guess it."))
        )
    }

    fun startNewRound() {
        target = GamesRepository.twentyQEntities.random()
        questionsUsed = 0
        solved = false
        revealed = false
        log = listOf(ChatTurn("assistant", "\u2753 Fresh secret picked! You have 20 questions."))
        SoundSynth.play("whoosh")
    }

    fun submitQuestion(rawQuestion: String) {
        val q = rawQuestion.trim()
        if (q.isBlank() || busy || solved || questionsUsed >= 20) return
        input = ""
        questionsUsed++
        log = log + ChatTurn("user", q)

        if (useAiHost && aiConfig.isEnabled) {
            busy = true
            scope.launch {
                val res = OpenAiClient.chatCompletion(
                    config = aiConfig,
                    systemPrompt = "You are hosting 20 Questions. Secret answer is \"${target.name}\". Reply concisely with YES, NOPE, or a tiny playful hint. If they guessed the secret, celebrate with 🎉 YES!",
                    history = log.dropLast(1),
                    userMessage = q,
                    maxTokens = 80
                )
                val reply = res.getOrElse {
                    val eval = GamesRepository.evaluateTwentyQ(target, q)
                    if (eval.won) solved = true
                    eval.response
                }
                if ("\uD83C\uDF89" in reply || target.name.removePrefix("a ").removePrefix("an ") in q.lowercase()) {
                    solved = true
                    SoundSynth.play("tada")
                } else {
                    SoundSynth.play(if (reply.startsWith("YES", ignoreCase = true)) "ding" else "drumhit")
                }
                log = log + ChatTurn("assistant", reply)
                busy = false
            }
        } else {
            val eval = GamesRepository.evaluateTwentyQ(target, q)
            if (eval.won) {
                solved = true
                SoundSynth.play("tada")
            } else {
                SoundSynth.play(if (eval.response.startsWith("YES")) "ding" else "drumhit")
            }
            val finalReply = if (!eval.won && questionsUsed >= 20) {
                revealed = true
                "${eval.response}\n\nOut of 20 questions! It was ${target.symbol} ${target.name}."
            } else {
                eval.response
            }
            log = log + ChatTurn("assistant", finalReply)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 700.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "\u2753 20 Questions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldAccent
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (solved) EmeraldGood.copy(alpha = 0.2f) else CardSurfaceElevated,
                    border = BorderStroke(1.dp, if (solved) EmeraldGood else CardBorder)
                ) {
                    Text(
                        text = "$questionsUsed / 20",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (solved) EmeraldGood else GoldAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            if (aiConfig.isEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !useAiHost,
                        onClick = { useAiHost = false },
                        label = { Text("Classic Host") },
                        modifier = Modifier.testTag("twentyq_classic_chip")
                    )
                    FilterChip(
                        selected = useAiHost,
                        onClick = { useAiHost = true },
                        label = { Text("\u2728 AI Host") },
                        modifier = Modifier.testTag("twentyq_ai_chip")
                    )
                }
            }

            // Quick attribute hint chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GamesRepository.twentyQAttrs.entries.forEach { (key, prompt) ->
                    Surface(
                        onClick = { submitQuestion(prompt) },
                        shape = RoundedCornerShape(999.dp),
                        color = CardSurfaceElevated,
                        border = BorderStroke(1.dp, CardBorder),
                        modifier = Modifier.testTag("twentyq_hint_$key")
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // Chat log
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
                    log.forEach { turn ->
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (revealed && !solved) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\uD83D\uDD13 Secret Answer: ${target.symbol} ${target.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldAccent,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask a yes/no question or guess…") },
                    singleLine = true,
                    enabled = !solved && questionsUsed < 20,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("twentyq_input")
                )
                IconButton(
                    onClick = { submitQuestion(input) },
                    enabled = !busy && !solved && questionsUsed < 20,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("twentyq_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(R.string.cd_send_message),
                        tint = GoldAccent
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        revealed = true
                        SoundSynth.play("trombone")
                    },
                    enabled = !revealed && !solved,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("twentyq_reveal_button")
                ) {
                    Text("Reveal Answer")
                }
                Button(
                    onClick = ::startNewRound,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("twentyq_new_round_button")
                ) {
                    Text("New Round", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

package com.streamwarrior.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.CardDeckKind
import com.streamwarrior.app.data.CardsRepository
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary
import kotlinx.coroutines.launch

@Composable
fun CardDeckScreen(
    kind: CardDeckKind,
    aiConfig: AiConfig
) {
    val scope = rememberCoroutineScope()
    val items = remember(kind) { CardsRepository.itemsFor(kind).shuffled() }
    var index by remember(kind) { mutableIntStateOf(0) }
    var customAiText by remember(kind) { mutableStateOf<String?>(null) }
    var chosenSide by remember(kind, index) { mutableStateOf<Int?>(null) }
    var aiLoading by remember(kind) { mutableStateOf(false) }

    val displayed = customAiText ?: items[index]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${kind.emoji} ${kind.title}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldAccent
                )
                Text(
                    text = "${index + 1} / ${items.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardSurface,
                border = BorderStroke(1.5.dp, VioletPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = kind.emoji, fontSize = 44.sp)
                    Text(
                        text = displayed,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    if (kind == CardDeckKind.WYR && " — or " in displayed) {
                        val parts = displayed.split(" — or ", limit = 2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            parts.forEachIndexed { sideIdx, option ->
                                val picked = chosenSide == sideIdx
                                Surface(
                                    onClick = {
                                        chosenSide = sideIdx
                                        SoundSynth.play("ding")
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (picked) PinkAccent.copy(alpha = 0.25f) else CardSurfaceElevated,
                                    border = BorderStroke(1.2.dp, if (picked) PinkAccent else CardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("wyr_choice_$sideIdx")
                                ) {
                                    Text(
                                        text = if (picked) "✓ $option" else option.trimEnd('?'),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        customAiText = null
                        index = if (index > 0) index - 1 else items.lastIndex
                        SoundSynth.play("whoosh")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("deck_prev_button")
                ) {
                    Text("◀ Back")
                }
                Button(
                    onClick = {
                        customAiText = null
                        index = (index + 1) % items.size
                        SoundSynth.play(kind.sfxId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("deck_next_button")
                ) {
                    Text("Next ${kind.title.dropLastWhile { it == 's' }} ▶", fontWeight = FontWeight.Bold)
                }
            }

            if (aiConfig.isEnabled) {
                OutlinedButton(
                    onClick = {
                        if (aiLoading) return@OutlinedButton
                        aiLoading = true
                        scope.launch {
                            val prompt = when (kind) {
                                CardDeckKind.JOKES -> "Tell one original clean PG-13 one-liner joke for a livestream. Just the joke."
                                CardDeckKind.LINES -> "Write one playful, wholesome PG-13 pickup line for livestream banter. Just the line."
                                CardDeckKind.FACTS -> "Share one surprising true fun fact in 1-2 short sentences."
                                CardDeckKind.WYR -> "Write one hilarious clean Would You Rather dilemma formatted strictly as: Option A — or Option B?"
                            }
                            val res = OpenAiClient.chatCompletion(
                                config = aiConfig,
                                systemPrompt = "You are a witty livestream co-host.",
                                userMessage = prompt,
                                maxTokens = 90
                            )
                            res.getOrNull()?.let {
                                customAiText = it
                                SoundSynth.play(kind.sfxId)
                            }
                            aiLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deck_ai_button")
                ) {
                    Text(if (aiLoading) "Generating…" else "✨ Fresh AI Card")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

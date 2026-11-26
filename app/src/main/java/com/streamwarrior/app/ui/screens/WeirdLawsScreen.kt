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
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.LawsRepository
import com.streamwarrior.app.data.StateWeirdLaws
import com.streamwarrior.app.data.WeirdLawScore
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

@Composable
fun WeirdLawsScreen(
    quizScore: WeirdLawScore,
    onRecordQuizAnswer: (Boolean) -> Unit
) {
    var quizMode by remember { mutableStateOf(true) }

    // Quiz state
    var quizIndex by remember { mutableIntStateOf(0) }
    val shuffledQuiz = remember { LawsRepository.quizItems.shuffled() }
    val quizCard = shuffledQuiz[quizIndex % shuffledQuiz.size]
    var quizAnsweredCorrect by remember(quizIndex) { mutableStateOf<Boolean?>(null) }

    // State browser state
    var selectedState by remember { mutableStateOf<StateWeirdLaws?>(null) }
    var stateLawIndex by remember { mutableIntStateOf(0) }
    var stateQuery by remember { mutableStateOf("") }

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
            Text(
                text = "\u2696\uFE0F Weird Laws & Real or Fake",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldAccent
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = quizMode,
                    onClick = { quizMode = true },
                    label = { Text("\u2696\uFE0F Weird Law or Fake?") },
                    modifier = Modifier.testTag("laws_tab_quiz")
                )
                FilterChip(
                    selected = !quizMode,
                    onClick = { quizMode = false },
                    label = { Text("\uD83C\uDDFA\uD83C\uDDF8 50-State Browser") },
                    modifier = Modifier.testTag("laws_tab_browser")
                )
            }

            if (quizMode) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: ${quizScore.score}", color = GoldAccent, fontWeight = FontWeight.Bold)
                        Text("Streak: ${quizScore.streak}", color = EmeraldGood, fontWeight = FontWeight.Bold)
                        Text("Best Streak: ${quizScore.bestStreak}", color = TextSecondary)
                    }
                }

                val cardBorder = when (quizAnsweredCorrect) {
                    true -> EmeraldGood
                    false -> CrimsonAlert
                    null -> VioletPrimary
                }
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = quizCard.state.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent
                        )
                        Text(
                            text = quizCard.text,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )

                        quizAnsweredCorrect?.let { correct ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (correct) EmeraldGood.copy(alpha = 0.16f) else CrimsonAlert.copy(alpha = 0.16f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = (if (correct) "✅ Spot on! " else "❌ Not this time. ") +
                                            "It was ${if (quizCard.isReal) "REAL" else "FAKE"}.",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (correct) EmeraldGood else CrimsonAlert
                                    )
                                    Text(
                                        text = quizCard.why,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                if (quizAnsweredCorrect == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val right = quizCard.isReal
                                quizAnsweredCorrect = right
                                onRecordQuizAnswer(right)
                                SoundSynth.play(if (right) "tada" else "buzzer")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("weirdlaw_real_button")
                        ) {
                            Text("REAL", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                val right = !quizCard.isReal
                                quizAnsweredCorrect = right
                                onRecordQuizAnswer(right)
                                SoundSynth.play(if (right) "tada" else "buzzer")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("weirdlaw_fake_button")
                        ) {
                            Text("FAKE", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            quizIndex++
                            SoundSynth.play("whoosh")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("weirdlaw_next_button")
                    ) {
                        Text("Next Law ▶", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val activeState = selectedState
                if (activeState != null) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.5.dp, GoldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = activeState.state.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldAccent
                            )
                            Text(
                                text = activeState.laws[stateLawIndex % activeState.laws.size],
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                stateLawIndex = (stateLawIndex + 1) % activeState.laws.size
                                SoundSynth.play("boing")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("laws_another_button")
                        ) {
                            Text("Another Law ▶", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                selectedState = LawsRepository.stateLaws.random()
                                stateLawIndex = 0
                                SoundSynth.play("whoosh")
                            },
                            modifier = Modifier.testTag("laws_random_state_button")
                        ) {
                            Text("🎲 Random")
                        }
                        OutlinedButton(
                            onClick = { selectedState = null },
                            modifier = Modifier.testTag("laws_all_states_button")
                        ) {
                            Text("States")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = stateQuery,
                            onValueChange = { stateQuery = it },
                            placeholder = { Text("Filter 50 states…") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("laws_state_search_input")
                        )
                        Button(
                            onClick = {
                                selectedState = LawsRepository.stateLaws.random()
                                stateLawIndex = 0
                                SoundSynth.play("whoosh")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                            modifier = Modifier.testTag("laws_pick_random_button")
                        ) {
                            Text("🎲 Random", fontWeight = FontWeight.Bold)
                        }
                    }

                    val filtered = LawsRepository.stateLaws.filter {
                        stateQuery.isBlank() || it.state.contains(stateQuery.trim(), ignoreCase = true)
                    }
                    filtered.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { item ->
                                Surface(
                                    onClick = {
                                        selectedState = item
                                        stateLawIndex = 0
                                        SoundSynth.play("ding")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardSurfaceElevated,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("state_btn_${item.state}")
                                ) {
                                    Text(
                                        text = item.state,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                    )
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

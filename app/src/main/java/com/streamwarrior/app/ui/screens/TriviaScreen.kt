package com.streamwarrior.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.dp
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.GamesRepository
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.data.TriviaQuestion
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary
import kotlinx.coroutines.launch

@Composable
fun TriviaScreen(
    aiConfig: AiConfig
) {
    val scope = rememberCoroutineScope()
    val categories = remember { listOf("All") + GamesRepository.trivia.map { it.category }.distinct() }
    var selectedCategory by remember { mutableStateOf("All") }
    var usedIndices by remember { mutableStateOf(emptySet<Int>()) }
    var currentQuestion by remember { mutableStateOf(GamesRepository.trivia.random()) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var revealed by remember { mutableStateOf(false) }
    var streak by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var aiLoading by remember { mutableStateOf(false) }

    fun loadNext(cat: String = selectedCategory) {
        val pool = GamesRepository.trivia.mapIndexedNotNull { index, q ->
            if (cat == "All" || q.category == cat) index to q else null
        }
        val unused = pool.filter { it.first !in usedIndices }
        val chosen = if (unused.isNotEmpty()) unused.random() else {
            usedIndices = emptySet()
            pool.random()
        }
        usedIndices = usedIndices + chosen.first
        currentQuestion = chosen.second
        selectedOption = null
        revealed = false
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
                    text = "\uD83E\uDDE0 Trivia Showdown",
                    style = MaterialTheme.typography.headlineMedium,
                    color = GoldAccent
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = CardSurfaceElevated,
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Text(
                        text = "Score $score • Streak $streak",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = cat
                            usedIndices = emptySet()
                            loadNext(cat)
                        },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("trivia_cat_$cat")
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentQuestion.category.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = VioletPrimary
                    )
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                }
            }

            val letters = listOf("A", "B", "C", "D")
            currentQuestion.options.forEachIndexed { idx, opt ->
                val isCorrect = idx == currentQuestion.correctIndex
                val isSelected = selectedOption == idx
                val borderColor = when {
                    !revealed -> CardBorder
                    isCorrect -> EmeraldGood
                    isSelected -> CrimsonAlert
                    else -> CardBorder
                }
                val bgColor = when {
                    !revealed -> CardSurfaceElevated
                    isCorrect -> EmeraldGood.copy(alpha = 0.20f)
                    isSelected -> CrimsonAlert.copy(alpha = 0.20f)
                    else -> CardSurface
                }

                Surface(
                    onClick = {
                        if (!revealed) {
                            selectedOption = idx
                            revealed = true
                            if (isCorrect) {
                                score++
                                streak++
                                SoundSynth.play("tada")
                            } else {
                                streak = 0
                                SoundSynth.play("buzzer")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = bgColor,
                    border = BorderStroke(1.2.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trivia_option_$idx")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = letters.getOrElse(idx) { "${idx + 1}" },
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent
                        )
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!revealed) {
                            revealed = true
                            SoundSynth.play("drumroll")
                        }
                    },
                    enabled = !revealed,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("trivia_reveal_button")
                ) {
                    Text("Reveal")
                }
                Button(
                    onClick = { loadNext() },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("trivia_next_button")
                ) {
                    Text("Next Question ▶", fontWeight = FontWeight.Bold)
                }
            }

            if (aiConfig.isEnabled) {
                OutlinedButton(
                    onClick = {
                        if (aiLoading) return@OutlinedButton
                        aiLoading = true
                        scope.launch {
                            val res = OpenAiClient.chatCompletion(
                                config = aiConfig,
                                systemPrompt = "Generate 1 fun multiple-choice trivia question for a livestream audience. Format strictly as 6 lines:\nCategory\nQuestion\nOption A\nOption B\nOption C\nOption D\nCorrectIndex(0-3)",
                                userMessage = "Category preference: $selectedCategory"
                            )
                            res.getOrNull()?.let { raw ->
                                val lines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
                                if (lines.size >= 7) {
                                    val cIdx = lines[6].filter { it.isDigit() }.toIntOrNull()?.coerceIn(0, 3) ?: 0
                                    currentQuestion = TriviaQuestion(
                                        category = lines[0],
                                        question = lines[1],
                                        options = lines.subList(2, 6),
                                        correctIndex = cIdx
                                    )
                                    selectedOption = null
                                    revealed = false
                                    SoundSynth.play("ding")
                                }
                            }
                            aiLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trivia_ai_generate_button")
                ) {
                    Text(if (aiLoading) "Generating AI Trivia…" else "✨ Generate Fresh AI Question")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

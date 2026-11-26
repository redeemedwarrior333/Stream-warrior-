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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.GamesRepository
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary

@Composable
fun MadLibsScreen() {
    var templateIdx by remember { mutableIntStateOf(0) }
    val template = GamesRepository.madLibs[templateIdx]
    var slotIndex by remember { mutableIntStateOf(0) }
    var currentInput by remember { mutableStateOf("") }
    var filledWords by remember(templateIdx) { mutableStateOf(List(template.slots.size) { "" }) }
    var showStory by remember(templateIdx) { mutableStateOf(false) }

    fun advanceWord(value: String) {
        val word = value.trim().ifBlank { template.slots[slotIndex] }
        val next = filledWords.toMutableList().also { it[slotIndex] = word }
        filledWords = next
        currentInput = ""
        if (slotIndex + 1 < template.slots.size) {
            slotIndex++
            SoundSynth.play("ding")
        } else {
            showStory = true
            SoundSynth.play("tada")
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
            Text(
                text = "\uD83D\uDCDD Mad Libs Live",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldAccent
            )
            Text(
                text = "Take funny word suggestions from chat and read the chaos out loud.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GamesRepository.madLibs.forEachIndexed { idx, item ->
                    FilterChip(
                        selected = templateIdx == idx,
                        onClick = {
                            templateIdx = idx
                            slotIndex = 0
                            currentInput = ""
                            filledWords = List(item.slots.size) { "" }
                            showStory = false
                        },
                        label = { Text(item.title) },
                        modifier = Modifier.testTag("madlibs_template_$idx")
                    )
                }
            }

            if (showStory) {
                val annotated = remember(template, filledWords) {
                    buildAnnotatedString {
                        val raw = template.storyTemplate
                        var cursor = 0
                        val regex = Regex("\\{(\\d+)\\}")
                        regex.findAll(raw).forEach { match ->
                            append(raw.substring(cursor, match.range.first))
                            val i = match.groupValues[1].toIntOrNull() ?: 0
                            withStyle(
                                SpanStyle(
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append(filledWords.getOrElse(i) { "___" })
                            }
                            cursor = match.range.last + 1
                        }
                        append(raw.substring(cursor))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = PinkAccent
                        )
                        Text(
                            text = annotated,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showStory = false
                            slotIndex = 0
                            currentInput = filledWords.firstOrNull().orEmpty()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("madlibs_edit_button")
                    ) {
                        Text("Edit Words")
                    }
                    Button(
                        onClick = {
                            templateIdx = (templateIdx + 1) % GamesRepository.madLibs.size
                            slotIndex = 0
                            currentInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("madlibs_next_story_button")
                    ) {
                        Text("Next Story ▶", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Slot progress chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    template.slots.forEachIndexed { idx, _ ->
                        val filled = filledWords[idx].isNotBlank()
                        FilterChip(
                            selected = idx == slotIndex,
                            onClick = {
                                slotIndex = idx
                                currentInput = filledWords[idx]
                            },
                            label = {
                                Text(
                                    if (filled) "${idx + 1}. ${filledWords[idx]}" else "Word ${idx + 1}"
                                )
                            },
                            modifier = Modifier.testTag("madlibs_slot_chip_$idx")
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardSurface,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Word ${slotIndex + 1} of ${template.slots.size}",
                            style = MaterialTheme.typography.labelLarge,
                            color = VioletPrimary
                        )
                        Text(
                            text = "Give me ${template.slots[slotIndex]}",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldAccent
                        )
                        OutlinedTextField(
                            value = currentInput,
                            onValueChange = { currentInput = it },
                            placeholder = { Text("Type word from chat…") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("madlibs_word_input")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { advanceWord(template.slots[slotIndex]) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("madlibs_skip_button")
                            ) {
                                Text("Use Placeholder")
                            }
                            Button(
                                onClick = { advanceWord(currentInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("madlibs_submit_word_button")
                            ) {
                                Text(
                                    if (slotIndex + 1 == template.slots.size) "Read Story!" else "Next ▶",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.R
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.ChatTurn
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.data.ProphetRepository
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary
import kotlinx.coroutines.launch

private enum class ProphetTab(val label: String) {
    DAILY("\u2728 Daily"),
    DECKS("\uD83D\uDD6F\uFE0F Decks"),
    ANGELS("\uD83D\uDC7C Angels"),
    ASK("\uD83D\uDE4F Ask")
}

@Composable
fun ProphetScreen(
    aiConfig: AiConfig
) {
    val scope = rememberCoroutineScope()
    var activeTab by remember { mutableStateOf(ProphetTab.DAILY) }
    val today = remember { ProphetRepository.dailyBlessing() }

    // Decks state
    var selectedDeckIdx by remember { mutableIntStateOf(0) }
    val currentDeck = ProphetRepository.decks[selectedDeckIdx]
    var drawnCardIdx by remember(selectedDeckIdx) { mutableStateOf<Int?>(null) }

    // Angels state
    var angelInput by remember { mutableStateOf("777") }
    val angelResult = remember(angelInput) { ProphetRepository.lookupAngelNumber(angelInput) }

    // Ask chat state
    var askInput by remember { mutableStateOf("") }
    var askBusy by remember { mutableStateOf(false) }
    var askMessages by remember {
        mutableStateOf(
            listOf(
                ChatTurn("assistant", "\uD83D\uDE4F Bring a question, burden, or prayer request — receive scripture and steady encouragement.")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 720.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "\uD83D\uDE4F Warrior Prophet",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldAccent
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProphetTab.entries.forEach { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("prophet_tab_${tab.name.lowercase()}")
                    )
                }
            }

            when (activeTab) {
                ProphetTab.DAILY -> {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = CardSurface,
                        border = BorderStroke(1.5.dp, GoldAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = today.datePretty.uppercase(),
                                style = MaterialTheme.typography.labelLarge,
                                color = PinkAccent
                            )
                            Text(
                                text = today.message,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CardSurfaceElevated,
                                border = BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "\"${today.verse}\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontStyle = FontStyle.Italic,
                                        color = GoldAccent
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "— ${today.reference}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = TextMuted
                                    )
                                }
                            }
                            Text(
                                text = "Today's Prayer: ${today.prayer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                ProphetTab.DECKS -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProphetRepository.decks.forEachIndexed { idx, d ->
                            FilterChip(
                                selected = selectedDeckIdx == idx,
                                onClick = { selectedDeckIdx = idx },
                                label = { Text("${d.symbol} ${d.name}") },
                                modifier = Modifier.testTag("prophet_deck_${d.id}")
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CardSurface,
                        border = BorderStroke(1.2.dp, VioletPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = currentDeck.symbol, fontSize = 38.sp)
                            Text(
                                text = currentDeck.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = GoldAccent
                            )
                            Text(
                                text = currentDeck.voice,
                                style = MaterialTheme.typography.labelLarge,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            val card = drawnCardIdx?.let { currentDeck.cards[it] }
                            if (card != null) {
                                Text(
                                    text = card.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "— ${card.reference}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GoldAccent
                                )
                            } else {
                                Text(
                                    text = currentDeck.intro,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            drawnCardIdx = (currentDeck.cards.indices).random()
                            SoundSynth.play("ding")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prophet_draw_card_button")
                    ) {
                        Text("Draw a Card ✨", fontWeight = FontWeight.Bold)
                    }
                }

                ProphetTab.ANGELS -> {
                    OutlinedTextField(
                        value = angelInput,
                        onValueChange = { angelInput = it.filter { ch -> ch.isDigit() }.take(6) },
                        label = { Text("Enter Angel Number (e.g. 111, 444, 777, 1111)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("angel_number_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("111", "222", "333", "444", "555", "777", "888", "1111", "1212").forEach { n ->
                            OutlinedButton(
                                onClick = {
                                    angelInput = n
                                    SoundSynth.play("ding")
                                },
                                modifier = Modifier.testTag("angel_chip_$n")
                            ) {
                                Text(n)
                            }
                        }
                    }

                    angelResult?.let { entry ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CardSurface,
                            border = BorderStroke(1.2.dp, GoldAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${entry.number} — ${entry.title}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = GoldAccent
                                )
                                Text(
                                    text = entry.message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "\"${entry.verse}\" — ${entry.reference}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                ProphetTab.ASK -> {
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
                            askMessages.forEach { turn ->
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
                            value = askInput,
                            onValueChange = { askInput = it },
                            placeholder = { Text("Ask for scripture or prayer guidance…") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prophet_ask_input")
                        )
                        IconButton(
                            onClick = {
                                val q = askInput.trim()
                                if (q.isBlank() || askBusy) return@IconButton
                                askInput = ""
                                askMessages = askMessages + ChatTurn("user", q)
                                if (aiConfig.isEnabled) {
                                    askBusy = true
                                    scope.launch {
                                        val res = OpenAiClient.chatCompletion(
                                            config = aiConfig,
                                            systemPrompt = "You are Warrior Prophet, a warm faith-centered spiritual encourager. Give 1-2 relevant Bible verses and gentle grounded counsel under 140 words.",
                                            history = askMessages.dropLast(1),
                                            userMessage = q
                                        )
                                        val reply = res.getOrElse { ProphetRepository.askProphetOffline(q) }
                                        askMessages = askMessages + ChatTurn("assistant", reply)
                                        askBusy = false
                                    }
                                } else {
                                    askMessages = askMessages + ChatTurn(
                                        "assistant",
                                        ProphetRepository.askProphetOffline(q)
                                    )
                                }
                            },
                            enabled = !askBusy,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("prophet_ask_send")
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

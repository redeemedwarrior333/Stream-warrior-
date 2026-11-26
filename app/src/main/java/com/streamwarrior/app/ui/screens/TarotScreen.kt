package com.streamwarrior.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.streamwarrior.app.R
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.AiConfig
import com.streamwarrior.app.data.ChatTurn
import com.streamwarrior.app.data.ComposedTarotReading
import com.streamwarrior.app.data.DrawnTarotCard
import com.streamwarrior.app.data.OpenAiClient
import com.streamwarrior.app.data.TarotDeck
import com.streamwarrior.app.data.TarotRepository
import com.streamwarrior.app.data.TarotSpread
import com.streamwarrior.app.data.VerdictTone
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private val TarotBgDeep = Color(0xFF0D0A1A)
private val TarotPanel = Color(0xFF161130)
private val TarotPanelInner = Color(0xFF1E1745)
private val TarotGold = Color(0xFFF5C56B)
private val TarotGoldDim = Color(0xFFB08D4F)
private val TarotLine = Color(0xFF2E2456)
private val TarotBad = Color(0xFFFF7A8A)
private val TarotGood = Color(0xFF7EE08A)

private data class FallingDeckCard(
    val card: DrawnTarotCard,
    val dx: Float,
    val dy: Float,
    val rot: Float
)

@Composable
fun TarotScreen(
    aiConfig: AiConfig,
    onNavigateTool: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val chatInputFocus = remember { FocusRequester() }

    var deck by remember { mutableStateOf(TarotDeck.CLASSIC) }
    var selectedSpread by remember { mutableStateOf(TarotRepository.spreads.first()) }
    var cutDeckMode by remember { mutableStateOf(false) }
    var question by remember { mutableStateOf("") }

    var isBusy by remember { mutableStateOf(false) }
    var buttonLabel by remember { mutableStateOf("\u2728 Reveal My Cards") }

    // Table cards
    var tableCards by remember { mutableStateOf<List<DrawnTarotCard>>(emptyList()) }
    var activeSpreadForDraw by remember { mutableStateOf(TarotRepository.spreads.first()) }
    var readingResult by remember { mutableStateOf<ComposedTarotReading?>(null) }
    var aiOverrideText by remember { mutableStateOf<String?>(null) }
    var inspectedCard by remember { mutableStateOf<DrawnTarotCard?>(null) }

    // Cut-the-deck stage state
    var showShuffleTable by remember { mutableStateOf(false) }
    var shuffleMeta by remember { mutableStateOf("") }
    var fallenCards by remember { mutableStateOf<List<FallingDeckCard>>(emptyList()) }
    var handWobbleTick by remember { mutableIntStateOf(0) }

    // Mystica inline chat state
    var chatInput by remember { mutableStateOf("") }
    var chatTyping by remember { mutableStateOf(false) }
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                ChatTurn("assistant", "Well met, wanderer. \uD83D\uDD2E Pick a spread, draw or cut the deck, and talk to Mystica.")
            )
        )
    }

    fun buildContextSummary(): String {
        val sp = activeSpreadForDraw
        if (tableCards.isEmpty() && chatMessages.size <= 1) return ""
        val cardsSummary = if (tableCards.isNotEmpty()) {
            "Last reading was a ${sp.label} spread: " +
                tableCards.joinToString(", ") { "${it.slotLabel} ${it.card.name}" } + "."
        } else ""
        val recent = chatMessages.takeLast(4).joinToString(" | ") {
            "${if (it.role == "assistant") "Mystica" else "Guest"}: ${it.content.take(80)}"
        }
        return "$cardsSummary Earlier in the chat: $recent".take(500)
    }

    fun finishDraw(cardsToDraw: List<DrawnTarotCard>, spread: TarotSpread, asked: String) {
        scope.launch {
            activeSpreadForDraw = spread
            tableCards = cardsToDraw.map { it.copy(revealed = false) }
            readingResult = null
            aiOverrideText = null
            SoundSynth.play("whoosh")

            val builtin = TarotRepository.composeReading(cardsToDraw, deck, spread, asked)

            val aiJob = if (aiConfig.isEnabled) {
                scope.launch {
                    val cardSummary = cardsToDraw.joinToString("; ") { d ->
                        val mean = TarotRepository.meaningFor(d, deck)
                        "${d.slotLabel} -> ${d.card.name}${if (d.reversed) " (reversed)" else ""}: $mean"
                    }
                    val voice = if (deck == TarotDeck.LOVE) {
                        "You are the Love Oracle: a warm, dramatic, straight-talking love tarot reader for a fun audience."
                    } else {
                        "You are Mystica: a mysterious, cinematic tarot reader with a little edge. Fun, never frightening."
                    }
                    val ctx = buildContextSummary()
                    val system = "$voice Write a short tarot reading (max 170 words) in this exact structure: one intro line, one line per card weaving its meaning into the question, then a final line starting with '🔥 The Verdict:' that clearly answers yes/no/wait. No disclaimers." +
                        if (ctx.isNotBlank()) " Chat context: $ctx" else ""
                    val res = OpenAiClient.chatCompletion(
                        config = aiConfig,
                        systemPrompt = system,
                        userMessage = "Spread: ${spread.label}. Question: ${asked.ifBlank { "(no question given — general energy reading)" }}. Cards drawn: $cardSummary."
                    )
                    res.getOrNull()?.let { aiOverrideText = it }
                }
            } else null

            // Sequential flip animation matching tarot-reader stagger
            cardsToDraw.indices.forEach { idx ->
                delay(if (idx == 0) 350L else 380L)
                SoundSynth.play("whoosh")
                tableCards = tableCards.toMutableList().also { list ->
                    list[idx] = list[idx].copy(revealed = true)
                }
            }

            delay(450L)
            aiJob?.join()
            readingResult = builtin
            isBusy = false
            buttonLabel = "\u2728 Draw Again"
            SoundSynth.play("tada")

            // Mystica automatically reacts to the reading
            if (aiConfig.isEnabled) {
                val reactionRes = OpenAiClient.chatCompletion(
                    config = aiConfig,
                    systemPrompt = "You are Mystica, a fun, warm, slightly mysterious tarot reader in a party game. React to the reading just given in 1-2 short playful sentences, then invite the guest to ask you about it. Never break character.",
                    userMessage = "Question: ${asked.ifBlank { "(none given)" }}. Spread: ${spread.label}. Reading:\n${aiOverrideText ?: builtin.fullText}",
                    maxTokens = 110
                )
                val comment = reactionRes.getOrNull() ?: TarotRepository.openingComment(spread)
                chatMessages = chatMessages + ChatTurn("assistant", comment)
            } else {
                chatMessages = chatMessages + ChatTurn("assistant", TarotRepository.openingComment(spread))
            }
        }
    }

    fun startDrawCards() {
        if (isBusy) return
        isBusy = true
        buttonLabel = "\uD83C\uDCCF Shuffling…"
        showShuffleTable = false
        shuffleMeta = ""
        fallenCards = emptyList()
        readingResult = null
        aiOverrideText = null

        val spread = selectedSpread
        val picked = TarotRepository.cards.shuffled().take(spread.slots.size).mapIndexed { i, card ->
            DrawnTarotCard(
                card = card,
                reversed = Random.nextFloat() < 0.32f,
                slotLabel = spread.slots[i],
                revealed = false
            )
        }
        finishDraw(picked, spread, question.trim())
    }

    fun startCutTheDeck() {
        if (isBusy) return
        val spread = selectedSpread
        isBusy = true
        buttonLabel = "\uD83C\uDF00 The Deck Is Deciding…"
        tableCards = emptyList()
        readingResult = null
        aiOverrideText = null
        fallenCards = emptyList()
        showShuffleTable = true
        shuffleMeta = "Hold still… a hand is holding the deck over the table."

        val pool = TarotRepository.cards.shuffled().take(spread.slots.size)
        scope.launch {
            val caught = mutableListOf<DrawnTarotCard>()
            pool.forEachIndexed { idx, card ->
                delay(if (idx == 0) 600L else 850L)
                handWobbleTick++
                SoundSynth.play("whoosh")
                val drawn = DrawnTarotCard(
                    card = card,
                    reversed = Random.nextFloat() < 0.32f,
                    slotLabel = spread.slots[idx],
                    revealed = true
                )
                caught += drawn
                val dir = if (idx % 2 == 0) -1f else 1f
                fallenCards = fallenCards + FallingDeckCard(
                    card = drawn,
                    dx = dir * (52f + Random.nextFloat() * 75f),
                    dy = 12f + (idx % 3) * 28f,
                    rot = dir * (10f + Random.nextFloat() * 18f)
                )
                shuffleMeta = if (idx + 1 < spread.slots.size) {
                    "${idx + 1} of ${spread.slots.size} fell — the deck keeps deciding…"
                } else {
                    "\u2728 The deck has spoken."
                }
            }
            delay(1000L)
            showShuffleTable = false
            shuffleMeta = ""
            finishDraw(caught, spread, question.trim())
        }
    }

    fun resetReading() {
        question = ""
        readingResult = null
        aiOverrideText = null
        tableCards = emptyList()
        showShuffleTable = false
        shuffleMeta = ""
        fallenCards = emptyList()
        isBusy = false
        buttonLabel = if (cutDeckMode) "\uD83C\uDF00 Start the Shuffle" else "\u2728 Reveal My Cards"
        scope.launch { scrollState.animateScrollTo(0) }
    }

    fun sendMysticaChat() {
        val msg = chatInput.trim()
        if (msg.isBlank() || chatTyping) return
        chatInput = ""
        chatMessages = chatMessages + ChatTurn("user", msg)

        if (aiConfig.isEnabled) {
            chatTyping = true
            scope.launch {
                val ctx = buildContextSummary()
                val voice = if (deck == TarotDeck.LOVE) {
                    "You are the Love Oracle: warm, dramatic, straight-talking love tarot reader for a fun audience."
                } else {
                    "You are Mystica: a mysterious, cinematic tarot reader with a little edge. Fun, never frightening, all-seeing."
                }
                val system = "$voice You are chatting with guests during a tarot session. Reply in 1-4 short sentences, in character, playful and wise." +
                    if (ctx.isNotBlank()) " Context: $ctx" else ""
                val res = OpenAiClient.chatCompletion(
                    config = aiConfig,
                    systemPrompt = system,
                    history = chatMessages.dropLast(1),
                    userMessage = msg,
                    maxTokens = 260
                )
                val reply = res.getOrNull() ?: TarotRepository.mysticaReply(msg, tableCards, deck)
                chatTyping = false
                chatMessages = chatMessages + ChatTurn("assistant", reply)
            }
        } else {
            chatMessages = chatMessages + ChatTurn(
                "assistant",
                TarotRepository.mysticaReply(msg, tableCards, deck)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF241A52).copy(alpha = 0.45f), TarotBgDeep)
                )
            )
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header matching "✦ ASK THE CARDS ✦ / AI Tarot Reader"
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "\u2726 ASK THE CARDS \u2726",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 3.sp,
                    color = TarotGoldDim
                )
                Text(
                    text = "AI Tarot Reader",
                    style = MaterialTheme.typography.displaySmall,
                    color = TarotGold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Pick a spread, cut the deck, and talk to Mystica.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "· ˚ ✦ ˚ ·",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 4.sp,
                    color = VioletPrimary.copy(alpha = 0.6f)
                )
            }

            // Main Control Panel
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TarotPanel,
                border = BorderStroke(1.dp, TarotLine),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deck toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeckToggleButton(
                            label = "\uD83D\uDD2E Mystica",
                            selected = deck == TarotDeck.CLASSIC,
                            onClick = { deck = TarotDeck.CLASSIC },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tarot_deck_classic")
                        )
                        DeckToggleButton(
                            label = "\uD83D\uDC98 Love Oracle",
                            selected = deck == TarotDeck.LOVE,
                            onClick = { deck = TarotDeck.LOVE },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tarot_deck_love")
                        )
                    }

                    // Spread picker grid (2-column cards with blurbs)
                    Text(
                        text = "CHOOSE YOUR SPREAD",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = TarotGoldDim
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TarotRepository.spreads.chunked(2).forEach { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                pair.forEach { spread ->
                                    val on = selectedSpread.id == spread.id
                                    Surface(
                                        onClick = { if (!isBusy) selectedSpread = spread },
                                        shape = RoundedCornerShape(12.dp),
                                        color = TarotPanelInner,
                                        border = BorderStroke(1.dp, if (on) TarotGold else TarotLine),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("tarot_spread_${spread.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = "${spread.icon} ${spread.label}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (on) TarotGold else TextPrimary
                                            )
                                            Text(
                                                text = spread.blurb,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                                if (pair.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }

                    // Mode toggle: Draw Cards vs Cut the Deck
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DeckToggleButton(
                            label = "\uD83C\uDCCF Draw Cards",
                            selected = !cutDeckMode,
                            onClick = {
                                if (!isBusy) {
                                    cutDeckMode = false
                                    showShuffleTable = false
                                    shuffleMeta = ""
                                    buttonLabel = "\u2728 Reveal My Cards"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tarot_mode_draw")
                        )
                        DeckToggleButton(
                            label = "\uD83C\uDF00 Cut the Deck",
                            selected = cutDeckMode,
                            onClick = {
                                if (!isBusy) {
                                    cutDeckMode = true
                                    tableCards = emptyList()
                                    buttonLabel = "\uD83C\uDF00 Start the Shuffle"
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tarot_mode_cut")
                        )
                    }

                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it.take(140) },
                        placeholder = { Text("What do you want to know? (optional)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tarot_question_input")
                    )

                    Button(
                        onClick = {
                            if (cutDeckMode) startCutTheDeck() else startDrawCards()
                        },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TarotGold,
                            contentColor = GoldInk
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("tarot_reveal_button")
                    ) {
                        Text(
                            text = buttonLabel,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Cut-the-Deck Interactive Felt Table
            AnimatedVisibility(visible = showShuffleTable) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CutDeckStage(
                        fallenCards = fallenCards,
                        wobbleTick = handWobbleTick
                    )
                    Text(
                        text = shuffleMeta,
                        style = MaterialTheme.typography.labelLarge,
                        color = TarotGold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Spread Card Table
            AnimatedVisibility(visible = tableCards.isNotEmpty() && !showShuffleTable) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tableCards.chunked(if (tableCards.size <= 3) tableCards.size else 3).forEach { rowCards ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowCards.forEach { drawn ->
                                val idx = tableCards.indexOf(drawn)
                                FlippingTarotCard(
                                    drawn = drawn,
                                    onClick = {
                                        if (drawn.revealed) inspectedCard = drawn
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("tarot_card_slot_$idx")
                                )
                            }
                        }
                    }
                }
            }

            // Reading Output & Verdict Card
            val currentReading = readingResult
            AnimatedVisibility(visible = currentReading != null) {
                if (currentReading != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val aiText = aiOverrideText
                        if (!aiText.isNullOrBlank()) {
                            aiText.split(Regex("\n\n+")).forEach { para ->
                                val isVerdict = para.trimStart().startsWith("\uD83D\uDD25")
                                ReadingBlockCard(
                                    text = para,
                                    isVerdict = isVerdict,
                                    verdictTone = currentReading.verdictTone
                                )
                            }
                        } else {
                            ReadingBlockCard(text = currentReading.intro, isVerdict = false, verdictTone = VerdictTone.NEUTRAL)
                            currentReading.cardLines.forEach { line ->
                                ReadingBlockCard(text = line, isVerdict = false, verdictTone = VerdictTone.NEUTRAL)
                            }
                            ReadingBlockCard(
                                text = currentReading.verdict,
                                isVerdict = true,
                                verdictTone = currentReading.verdictTone
                            )
                        }

                        // Action row: New Reading | Share | Ask Mystica
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = ::resetReading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tarot_new_reading_button")
                            ) {
                                Text("\uD83D\uDD04 New", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    val shareBody = "\uD83C\uDCCF Ask the Cards — my tarot reading:\n\n" +
                                        (aiOverrideText ?: currentReading.fullText)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Ask the Cards Reading")
                                        putExtra(Intent.EXTRA_TEXT, shareBody)
                                    }
                                    runCatching {
                                        context.startActivity(Intent.createChooser(intent, "Share Tarot Reading"))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tarot_share_button")
                            ) {
                                Text("\uD83D\uDCE4 Share", maxLines = 1)
                            }
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                        chatInputFocus.requestFocus()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tarot_ask_mystica_button")
                            ) {
                                Text("\uD83D\uDCAC Mystica", maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Inline Mystica Chat Panel (matches tarot-reader chatpanel)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = TarotPanel,
                border = BorderStroke(1.dp, TarotLine),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(listOf(Color(0xFF3B2B7D), Color(0xFF171040)))
                                )
                                .border(1.dp, TarotGoldDim, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("\uD83D\uDD2E", fontSize = 22.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mystica", style = MaterialTheme.typography.titleMedium, color = TarotGold)
                            Text(
                                text = "Your AI tarot reader. She answers, she chats, she reads you.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(TarotGood)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chatMessages.forEach { msg ->
                            val isUser = msg.role == "user"
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isUser) TarotGold.copy(alpha = 0.14f) else TarotPanelInner,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isUser) TarotGold.copy(alpha = 0.45f) else TarotLine
                                    ),
                                    modifier = Modifier.widthIn(max = 520.dp)
                                ) {
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (chatTyping) {
                        Text(
                            text = "Mystica is shuffling her thoughts…",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it.take(300) },
                            placeholder = { Text("Talk to Mystica…") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(chatInputFocus)
                                .testTag("mystica_chat_input")
                        )
                        IconButton(
                            onClick = ::sendMysticaChat,
                            enabled = !chatTyping,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mystica_chat_send")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.cd_send_message),
                                tint = TarotGold
                            )
                        }
                    }
                }
            }

            // Companion Stream Warrior tool chips & entertainment disclaimer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "EXPLORE MORE STREAM WARRIOR TOOLS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TarotGoldDim
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TarotRepository.companionTools.forEach { tool ->
                        Surface(
                            onClick = { onNavigateTool(tool.routeKey) },
                            shape = RoundedCornerShape(999.dp),
                            color = TarotPanel,
                            border = BorderStroke(1.dp, TarotLine),
                            modifier = Modifier.testTag("tarot_tool_chip_${tool.routeKey}")
                        ) {
                            Text(
                                text = "${tool.emoji} ${tool.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "For entertainment purposes only. The cards don't decide your future — you do.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    inspectedCard?.let { card ->
        Dialog(onDismissRequest = { inspectedCard = null }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = TarotPanel,
                border = BorderStroke(1.5.dp, TarotGold)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = card.card.symbol, fontSize = 44.sp)
                    Text(
                        text = "${card.card.name} (${if (card.reversed) "Reversed" else "Upright"})",
                        style = MaterialTheme.typography.titleLarge,
                        color = TarotGold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Position: ${card.slotLabel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (card.reversed) TarotBad else TarotGood
                    )
                    Text(
                        text = TarotRepository.meaningFor(card, deck),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { inspectedCard = null },
                        colors = ButtonDefaults.buttonColors(containerColor = TarotGold, contentColor = GoldInk),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tarot_detail_close_button")
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeckToggleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = TarotPanelInner,
        border = BorderStroke(1.2.dp, if (selected) TarotGold else TarotLine),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) TarotGold else TextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun FlippingTarotCard(
    drawn: DrawnTarotCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flipRotation by animateFloatAsState(
        targetValue = if (drawn.revealed) 180f else 0f,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "tarotFlip"
    )
    val showingFront = flipRotation >= 90f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = drawn.slotLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.5.sp,
            color = TarotGoldDim,
            textAlign = TextAlign.Center
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(14.dp),
            color = if (showingFront) TarotPanelInner else Color(0xFF231852),
            border = BorderStroke(
                1.2.dp,
                if (showingFront) TarotGoldDim else Color(0xFF3D2F7A)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3.1f)
                .graphicsLayer {
                    rotationY = flipRotation
                    cameraDistance = 12f * density
                }
        ) {
            if (!showingFront) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(7.dp)
                        .border(1.dp, TarotGold.copy(alpha = 0.35f), RoundedCornerShape(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83C\uDF19", fontSize = 34.sp)
                }
            } else {
                // Counter-mirror so front text reads cleanly after 180deg Y flip
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                            rotationZ = if (drawn.reversed) 180f else 0f
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = drawn.card.symbol, fontSize = 32.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = drawn.card.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = TarotGold,
                            textAlign = TextAlign.Center
                        )
                        if (drawn.reversed) {
                            Text(
                                text = "REVERSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = TarotBad,
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CutDeckStage(
    fallenCards: List<FallingDeckCard>,
    wobbleTick: Int
) {
    val handWobble = remember { Animatable(0f) }
    LaunchedEffect(wobbleTick) {
        if (wobbleTick > 0) {
            handWobble.animateTo(-9f, tween(110))
            handWobble.animateTo(8f, tween(130))
            handWobble.animateTo(0f, tween(120))
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF120C2C),
        border = BorderStroke(1.dp, TarotLine),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF1C1440), Color(0xFF120C2C))
                    )
                )
        ) {
            Text(
                text = "the cards that slip out become your reading",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )

            // Fallen cards splayed on table
            fallenCards.forEach { fallen ->
                var animStarted by remember(fallen) { mutableStateOf(false) }
                LaunchedEffect(fallen) { animStarted = true }
                val progress by animateFloatAsState(
                    targetValue = if (animStarted) 1f else 0f,
                    animationSpec = tween(550, easing = FastOutSlowInEasing),
                    label = "dropCard"
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TarotPanelInner,
                    border = BorderStroke(1.2.dp, if (fallen.card.reversed) TarotBad else TarotGold),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset {
                            IntOffset(
                                x = (fallen.dx * progress).roundToInt(),
                                y = (-20f + fallen.dy * progress).roundToInt()
                            )
                        }
                        .graphicsLayer {
                            rotationZ = fallen.rot * progress + if (fallen.card.reversed) 180f else 0f
                        }
                        .size(width = 62.dp, height = 98.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = fallen.card.card.symbol, fontSize = 20.sp)
                        Text(
                            text = fallen.card.card.name,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (fallen.card.reversed) TarotBad else TarotGold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Stacked dealer deck + hand at bottom center
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .graphicsLayer { rotationZ = handWobble.value },
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 26.dp)
                        .size(width = 64.dp, height = 92.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2A1E5E), Color(0xFF171040)))
                        )
                        .border(1.dp, Color(0xFF3D2F7A), RoundedCornerShape(7.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("\uD83C\uDF19", fontSize = 22.sp)
                }
                Text(text = "\uD83E\uDD1A", fontSize = 48.sp)
            }
        }
    }
}

@Composable
private fun ReadingBlockCard(
    text: String,
    isVerdict: Boolean,
    verdictTone: VerdictTone
) {
    val accent = when {
        !isVerdict -> TarotGoldDim
        verdictTone == VerdictTone.GOOD -> TarotGood
        verdictTone == VerdictTone.BAD -> TarotBad
        else -> TarotGold
    }
    val bg = if (isVerdict) TarotGold.copy(alpha = 0.12f) else TarotPanelInner
    Surface(
        shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, topStart = 4.dp, bottomStart = 4.dp),
        color = bg,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isVerdict) accent else TextPrimary,
            fontWeight = if (isVerdict) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(14.dp)
        )
    }
}

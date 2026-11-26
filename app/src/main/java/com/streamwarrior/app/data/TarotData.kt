package com.streamwarrior.app.data

data class TarotCard(
    val number: Int,
    val name: String,
    val symbol: String,
    val upright: String,
    val reversed: String,
    val loveUpright: String,
    val loveReversed: String,
    val good: Int
)

data class TarotSpread(
    val id: String,
    val icon: String,
    val label: String,
    val blurb: String,
    val slots: List<String>
)

data class DrawnTarotCard(
    val card: TarotCard,
    val reversed: Boolean,
    val slotLabel: String,
    val revealed: Boolean = false
)

enum class TarotDeck {
    CLASSIC, LOVE
}

enum class VerdictTone {
    GOOD, NEUTRAL, BAD
}

data class ComposedTarotReading(
    val intro: String,
    val cardLines: List<String>,
    val verdict: String,
    val verdictTone: VerdictTone,
    val fullText: String
)

data class TarotToolSuggestion(
    val label: String,
    val emoji: String,
    val routeKey: String
)

object TarotRepository {
    val spreads = listOf(
        TarotSpread("ppf", "\uD83D\uDD70\uFE0F", "Past • Present • Future",
            "The classic. Where you were, where you are, where it's heading.",
            listOf("Past", "Present", "Future")),
        TarotSpread("single", "\uD83C\uDFAF", "Single Pull",
            "One card. One message. Fast and direct.",
            listOf("Your Message")),
        TarotSpread("yesno", "\u2705", "Yes / No Oracle",
            "One card cuts straight to yes, no, or wait.",
            listOf("The Answer")),
        TarotSpread("sao", "\uD83E\uDDED", "Situation • Action • Outcome",
            "What's happening, what to do, where it lands.",
            listOf("Situation", "Action", "Outcome")),
        TarotSpread("mindbod", "\u262F\uFE0F", "Mind • Body • Spirit",
            "Where your head, energy, and soul are at.",
            listOf("Mind", "Body", "Spirit")),
        TarotSpread("lovecross", "\uD83D\uDC98", "Love Cross",
            "You, them, the spark, the block.",
            listOf("You", "Them", "The Spark", "The Block")),
        TarotSpread("career", "\uD83D\uDCBC", "Career Compass",
            "Where you stand, what's blocking you, your next move, the prize.",
            listOf("Where You Stand", "The Block", "Next Move", "The Prize")),
        TarotSpread("shadow", "\uD83C\uDF11", "Shadow Work",
            "What you hide, what it costs, what frees you.",
            listOf("What You Hide", "What It Costs", "What Frees You")),
        TarotSpread("weekahead", "\uD83D\uDDD3\uFE0F", "Week Ahead",
            "One card per day, plus the weekend's lesson.",
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Weekend Vibe", "The Lesson"))
    )

    val cards = listOf(
        TarotCard(0, "The Fool", "\uD83C\uDCCF",
            "A pure new beginning. Take the leap — the energy is with you.",
            "Reckless moves or cold feet. Look before you jump.",
            "New love energy is circling you. Stay open — someone genuine is close.",
            "Someone is enjoying the chase with no plans to stay. Protect your heart.", 1),
        TarotCard(1, "The Magician", "\uD83E\uDE84",
            "You have every tool you need. Manifest it — say it out loud.",
            "Untapped talent or manipulation nearby. Focus your will.",
            "Magnetic energy. You're more attractive than you realize right now.",
            "Smooth talker alert. Watch the words, not the charm.", 1),
        TarotCard(2, "The High Priestess", "\uD83C\uDF19",
            "Trust your gut — you already know the answer. Secrets surface soon.",
            "You're ignoring your intuition. Silence the noise and listen.",
            "There's more to this person than they show. Your intuition is dead-on.",
            "Hidden feelings or hidden truths. Something is being kept from you.", 1),
        TarotCard(3, "The Empress", "\uD83C\uDF39",
            "Abundance and growth. Nurture what you're building — it's blooming.",
            "Creative block or self-neglect. Pour back into yourself.",
            "Deep, nurturing love. This connection has real roots and real future.",
            "Someone is being smothered, or you're settling for crumbs.", 1),
        TarotCard(4, "The Emperor", "\uD83D\uDC51",
            "Structure and control. Take charge — discipline wins right now.",
            "Rigidity or an ego problem. Loosen the grip.",
            "A protector energy — loyal, stable, all-in. This one shows up.",
            "Control issues or jealousy are poisoning the connection.", 1),
        TarotCard(5, "The Hierophant", "\uD83D\uDD11",
            "Tradition and guidance. Follow the proven path or find a mentor.",
            "Break the rules. The conventional path isn't yours.",
            "Commitment energy — labels, rings, official. This is going public.",
            "Outdated beliefs about love are holding you back. Rewrite them.", 1),
        TarotCard(6, "The Lovers", "\uD83D\uDC9E",
            "A powerful union or a big choice. Lead with the heart, then the head.",
            "Misalignment of values. The choice you're avoiding is the one that matters.",
            "THE love card. Real, mutual, undeniable. Say yes.",
            "A crossroads in love — or a third party stirring the pot. Choose yourself.", 1),
        TarotCard(7, "The Chariot", "\uD83C\uDFCE\uFE0F",
            "Momentum and victory through focus. Grab the reins and go.",
            "Scattered energy. One direction, or you crash.",
            "Go for it. Make the first move — the chase is favored.",
            "Two people pulling in opposite directions. Someone has to steer.", 1),
        TarotCard(8, "Strength", "\uD83E\uDD81",
            "Quiet power. Patience and softness beat force right now.",
            "Self-doubt is lying to you. You're stronger than the fear.",
            "Gentle patience wins this heart. Slow and steady is the play.",
            "Raw emotions running hot. Don't let pride burn the bridge.", 1),
        TarotCard(9, "The Hermit", "\uD83D\uDD6F\uFE0F",
            "Solitude brings the answer. Step back, reflect, recharge.",
            "Isolation has gone too far. Come back to the world.",
            "A meaningful connection needs quiet time to grow. Quality over quantity.",
            "One of you is hiding from the relationship. Distance is a decision.", 0),
        TarotCard(10, "Wheel of Fortune", "\u2638\uFE0F",
            "Fate is turning in your favor. Ride the wave — luck is live.",
            "A rough spin. This cycle is ending to free you.",
            "Destiny energy. A fated meeting or reunion is in motion.",
            "Timing is off — not the person. Let the wheel turn.", 1),
        TarotCard(11, "Justice", "\u2696\uFE0F",
            "Truth and balance. What's owed is coming, both ways.",
            "Bias or avoidance. Face the truth you're dodging.",
            "Karmic fairness — this connection balances you. Honest talk seals it.",
            "An imbalance: someone is giving more. Name it or lose it.", 1),
        TarotCard(12, "The Hanged Man", "\uD83D\uDE43",
            "Pause. The answer arrives when you change perspective.",
            "Stalling as a lifestyle. A sacrifice must be made.",
            "Wait before acting — the view changes and so will the answer.",
            "You're hanging on to something that already ended.", 0),
        TarotCard(13, "Death", "\uD83D\uDC80",
            "Transformation. Something must end so something better begins.",
            "You're gripping a dead chapter. Release it.",
            "A relationship is leveling up — old dynamics die, deeper bonds form.",
            "It's over. You know it. The universe is just waiting on you.", 0),
        TarotCard(14, "Temperance", "\u23F3",
            "Balance and patience. The middle path is the winning path.",
            "Excess somewhere in your life. Recalibrate.",
            "Slow-burn harmony. This one blends into your life beautifully.",
            "Too hot, too cold — someone's all-or-nothing. Find the middle.", 1),
        TarotCard(15, "The Devil", "\uD83D\uDE08",
            "An attachment or temptation has power over you. Name it to break it.",
            "Breaking free from a toxic cycle. Keep climbing.",
            "Intense, magnetic, physical chemistry — just keep your power.",
            "Obsession, jealousy or a toxic loop. This is your sign to walk.", -1),
        TarotCard(16, "The Tower", "\uD83D\uDDFC",
            "Sudden shake-up — painful but liberating. The foundation was fake anyway.",
            "You're avoiding the collapse. It's kinder to let it fall now.",
            "A truth detonates the old dynamic — what survives is real.",
            "Drama incoming. Don't build on someone else's chaos.", -1),
        TarotCard(17, "The Star", "\u2B50",
            "Hope and healing after the storm. Wish big — you're seen.",
            "Faith is flickering. Rest, don't quit.",
            "Wish-fulfillment love. The soft, healing kind you dreamed about.",
            "Healing first, love second. Don't pour from an empty cup.", 1),
        TarotCard(18, "The Moon", "\uD83C\uDF18",
            "Not everything is as it seems. Move slowly through the fog.",
            "The fog lifts. Clarity and truth arrive.",
            "Mixed signals — feel the energy, don't just hear the words.",
            "The confusion clears. What's revealed confirms your gut.", 0),
        TarotCard(19, "The Sun", "\u2600\uFE0F",
            "Full shine. Success, joy, vitality — everything's illuminated.",
            "Dimmed joy. The sun's still there — find it.",
            "Pure, happy, out-in-the-open love. This is the good stuff.",
            "Ego is blocking the warmth. Lead with warmth instead.", 1),
        TarotCard(20, "Judgement", "\uD83D\uDCE2",
            "A calling. Time to rise, decide, and level up for real.",
            "Self-judgment is drowning your potential. Forgive yourself.",
            "A relationship reaches a verdict moment — define it or release it.",
            "Repeating an old lesson with a new face. Break the loop.", 1),
        TarotCard(21, "The World", "\uD83C\uDF0D",
            "Completion and celebration. You finished the cycle — enjoy it.",
            "Almost there. One loose end to tie before the win.",
            "Full-circle love. Everything you asked for is arriving — receive it.",
            "So close to the finish line. Don't quit one step before the blessing.", 1)
    )

    val companionTools = listOf(
        TarotToolSuggestion("Warrior Prophet", "\uD83C\uDF1F", "prophet"),
        TarotToolSuggestion("Life Coach", "\uD83C\uDF31", "coach:life_coach"),
        TarotToolSuggestion("Problem Solver", "\uD83E\uDDE9", "coach:problem_solver"),
        TarotToolSuggestion("Comeback Planner", "\uD83D\uDCC8", "coach:comeback"),
        TarotToolSuggestion("Weird Law", "\u2696\uFE0F", "laws"),
        TarotToolSuggestion("TFT Coach", "\uD83C\uDFAE", "coach:tft_coach"),
        TarotToolSuggestion("Wealth Builder", "\uD83D\uDCB0", "coach:wealth_builder"),
        TarotToolSuggestion("No Excuses", "\uD83D\uDD25", "coach:no_excuses")
    )

    fun meaningFor(drawn: DrawnTarotCard, deck: TarotDeck): String = when (deck) {
        TarotDeck.CLASSIC -> if (drawn.reversed) drawn.card.reversed else drawn.card.upright
        TarotDeck.LOVE -> if (drawn.reversed) drawn.card.loveReversed else drawn.card.loveUpright
    }

    fun questionType(question: String): String {
        val s = question.lowercase()
        if (Regex("\\b(love|crush|ex|relationship|him|her|date|dating|marry|marriage|soulmate|boyfriend|girlfriend|together|back)\\b").containsMatchIn(s)) {
            return "love"
        }
        if (Regex("\\b(should i|career|job|move|quit|start|business|school|choice|decide|path|take the)\\b").containsMatchIn(s)) {
            return "path"
        }
        return "general"
    }

    fun composeReading(
        drawnCards: List<DrawnTarotCard>,
        deck: TarotDeck,
        spread: TarotSpread,
        question: String
    ): ComposedTarotReading {
        val type = questionType(question)
        val opener = if (question.isNotBlank()) "\"${question.trim()}\"" else "your unspoken question"
        val spreadName = spread.label.lowercase()
        val intro = when {
            type == "love" || deck == TarotDeck.LOVE ->
                "\uD83D\uDC98 The Love Oracle turns her cards for $opener. A $spreadName spread — let's see what your heart is really asking."
            type == "path" ->
                "\uD83E\uDDED The cards map your crossroads around $opener. Reading the $spreadName spread:"
            else ->
                "\uD83D\uDD2E Mystica shuffles, the candles flicker, and the $spreadName spread answers $opener — read it with me:"
        }

        var score = 0
        val cardLines = drawnCards.map { d ->
            score += if (d.reversed) -d.card.good else d.card.good
            val revSuffix = if (d.reversed) " (reversed)" else ""
            "\uD83C\uDCCF ${d.slotLabel} — ${d.card.name}$revSuffix: ${meaningFor(d, deck)}"
        }

        if (spread.id == "yesno" && drawnCards.isNotEmpty()) {
            val raw = Math.round(score.toFloat() / (spread.slots.size * 2f) * 3f) - 1
            score = if (raw > 0) 2 else raw
        }

        val verdict = when {
            score >= 2 -> if (type == "love" || deck == TarotDeck.LOVE) {
                "\uD83D\uDD25 The Verdict: The cards say GO. This energy is real — move toward it."
            } else {
                "\uD83D\uDD25 The Verdict: A clear YES. The path is open — take it while it's lit."
            }
            score == 1 -> "\uD83D\uDD25 The Verdict: Leaning yes — but keep your eyes open and your standards higher."
            score == 0 -> if (spread.id == "yesno") {
                "\uD83D\uDD25 The Verdict: WAIT. Not yet. Ask again when the dust settles."
            } else {
                "\uD83D\uDD25 The Verdict: The cards say WAIT. The answer isn't ready — and neither is the situation."
            }
            score == -1 -> "\uD83D\uDD25 The Verdict: Leaning no. Protect your energy before you invest more of it."
            else -> if (type == "love" || deck == TarotDeck.LOVE) {
                "\uD83D\uDD25 The Verdict: Walk away. The cards rarely shout — they're shouting."
            } else {
                "\uD83D\uDD25 The Verdict: Redirect. This door closes so a better one can open."
            }
        }

        val tone = when {
            score >= 2 -> VerdictTone.GOOD
            score <= -1 -> VerdictTone.BAD
            else -> VerdictTone.NEUTRAL
        }

        val fullText = (listOf(intro) + cardLines + listOf(verdict)).joinToString("\n\n")
        return ComposedTarotReading(intro, cardLines, verdict, tone, fullText)
    }

    fun openingComment(spread: TarotSpread): String = listOf(
        "The cards have landed. Ask me anything about them — I'll tell you exactly what I see. \uD83D\uDD2E",
        "That reading has layers. Want me to explain any card, or shall we pull again?",
        "The card on ${spread.slots.random()} is the loudest one. Ask me why. \uD83D\uDC40"
    ).random()

    fun mysticaReply(
        userMessage: String,
        drawnCards: List<DrawnTarotCard>,
        deck: TarotDeck
    ): String {
        val s = userMessage.lowercase()
        return when {
            Regex("\\b(hi|hello|hey|yo)\\b").containsMatchIn(s) ->
                "Well met, wanderer. \uD83D\uDD2E I'm Mystica. Draw some cards and I'll read the story the deck tells — or just keep talking to me."
            Regex("love|crush|ex|him|her|boyfriend|girlfriend").containsMatchIn(s) ->
                "Ah, matters of the heart — my favorite flavor of chaos. Try the Love Oracle deck with the Love Cross spread, then tell me which card landed on 'The Block.' That one is always the truth-teller. \uD83D\uDC98"
            Regex("job|career|money|work|business|quit").containsMatchIn(s) ->
                "Career energy is all about timing. The Career Compass spread maps it well — four cards, four truths. And if you're plotting a comeback, check out Comeback Planner below. \uD83D\uDCC8"
            Regex("\\b(yes|no)\\b").containsMatchIn(s) ->
                "The deck doesn't do maybe — but it loves drama. Try the Yes / No Oracle spread and let's hear the verdict properly."
            Regex("scary|fake|real|believe").containsMatchIn(s) ->
                "Am I real? I'm real enough to keep your secrets. The cards don't decide your future — they just hold up a mirror and let you see clearly. \uD83C\uDF19"
            Regex("mystica|who are you").containsMatchIn(s) ->
                "I am Mystica, keeper of this deck and your tarot reader for tonight. I read cards, I read energy, and I read chat. What shall we ask the universe?"
            drawnCards.isNotEmpty() -> {
                val focus = drawnCards.last()
                "Look closely at ${focus.card.symbol} ${focus.card.name} in ${focus.slotLabel}: ${meaningFor(focus, deck)} Want to pull a clarifying card or test another spread?"
            }
            else -> listOf(
                "The whispers around you say the same thing every time: ask the question you're avoiding — your gut already knows. Want me to pull a card on it?",
                "Mm. The air around that question is thick. Pull a card with me and we'll slice through it together. \uD83D\uDD2E",
                "You know, my cousin over at Warrior Prophet claims she saw your future. Let's check her work with a proper reading. \uD83C\uDF1F",
                "Some things need one card, some need seven. That question? Give it the Week Ahead spread — a card per day, no hiding."
            ).random()
        }
    }
}

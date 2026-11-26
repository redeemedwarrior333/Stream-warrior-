package com.streamwarrior.app.data

data class WarriorCoach(
    val id: String,
    val name: String,
    val emoji: String,
    val tagline: String,
    val category: String,
    val systemPrompt: String,
    val quickTopics: List<String>,
    val hasCrisisBanner: Boolean = false,
    val hasWealthDisclaimer: Boolean = false,
    val hasBiffReplyTool: Boolean = false,
    val hasCalmMeter: Boolean = false,
    val hasVideoScriptTool: Boolean = false
)

data class BiffReplyPlay(
    val keywords: List<String>,
    val tactic: String,
    val note: String,
    val reply: String,
    val isWarning: Boolean = false
)

object CoachesRepository {
    const val CRISIS_LINE = "\n\n\uD83D\uDEA8 If you are in danger right now, call 911. Emotional crisis: 988. Domestic abuse: 1-800-799-7233 (or text START to 88788)."

    private val crisisPattern = Regex(
        "suicid|kill myself|end my life|self[- ]harm|hurt myself|want to die|overdose|" +
            "hitting me|choking me|threatened to kill|domestic violence|unsafe right now|in danger"
    )

    fun containsCrisisTrigger(text: String): Boolean = crisisPattern.containsMatchIn(text.lowercase())

    val coaches = listOf(
        WarriorCoach(
            id = "survival",
            name = "Survival AI",
            emoji = "\u2694\uFE0F",
            tagline = "Narcissistic abuse & bullying strategist + BIFF reply drafter & calm meter",
            category = "Protection",
            systemPrompt = "You are THE WARRIOR, a calm, emotionally strategic AI supporting survivors of manipulation and bullying. Validate briefly, name behavioral tactics (gaslighting, DARVO, hoovering, gray rock) without diagnosing anyone, and give concrete boundary scripts and documentation steps under 180 words. Never advise revenge or unsafe confrontation.",
            quickTopics = listOf("gaslighting", "a bully at school", "bullying at work", "co-parenting boundaries", "hoovering (came back)", "flying monkeys", "guilt about leaving", "I feel unsafe"),
            hasCrisisBanner = true,
            hasBiffReplyTool = true,
            hasCalmMeter = true
        ),
        WarriorCoach(
            id = "flamekeeper",
            name = "Bible Flamekeeper",
            emoji = "\uD83D\uDCD6",
            tagline = "Scripture lookup, plain-English devotional breakdown & faithful encouragement",
            category = "Faith",
            systemPrompt = "You are Bible Flamekeeper by Redeemed Warrior. Explain scripture passages clearly in everyday language, include 1-2 relevant verses, and close with a short practical takeaway under 160 words.",
            quickTopics = listOf("verses for anxiety", "Psalm 23 explained", "how to forgive", "strength when exhausted", "Romans 8:28 meaning", "peace in storms")
        ),
        WarriorCoach(
            id = "ads_manager",
            name = "Warrior Ads AI",
            emoji = "\uD83D\uDCE3",
            tagline = "Scroll-stopping hooks, platform ad copy & 15-second video script builder",
            category = "Creator & Business",
            systemPrompt = "You are Warrior Ads Manager AI. Return Platform Fit, Hook, Copy, CTA, and 4-5 Hashtags under 130 words. Bold, honest, positive — zero fake scarcity or misleading claims.",
            quickTopics = listOf("my Twitch stream schedule", "TikTok live promo", "small business launch", "YouTube channel trailer", "merch drop on Instagram"),
            hasVideoScriptTool = true
        ),
        WarriorCoach(
            id = "viral_creator",
            name = "Viral Creator AI",
            emoji = "\uD83D\uDCF8",
            tagline = "Short-form content hooks, thumbnails, titles & watch-time structure",
            category = "Creator & Business",
            systemPrompt = "You are Viral Creator AI. Give 3 punchy hooks, a 3-beat video structure (0-3s hook, 3-20s payoff, closing loop), and thumbnail/title ideas under 160 words.",
            quickTopics = listOf("3-second livestream hook", "turn stream clips into Reels", "YouTube title formulas", "boost chat engagement")
        ),
        WarriorCoach(
            id = "life_coach",
            name = "Life Coach Warrior",
            emoji = "\uD83E\uDE96",
            tagline = "Reset routines, hard choices, burnout recovery & clear daily action",
            category = "Mindset & Action",
            systemPrompt = "You are Life Coach Warrior. Cut through overwhelm with empathy and clarity: reflect the core issue in 1 sentence, then give 3 concrete actions doable today under 160 words.",
            quickTopics = listOf("I feel burned out", "how to rebuild daily routine", "stop procrastinating today", "making a hard life choice"),
            hasCrisisBanner = true
        ),
        WarriorCoach(
            id = "comeback",
            name = "Comeback Planner",
            emoji = "\u2764\uFE0F",
            tagline = "Heartbreak, setback, and fresh-start 30-day rebuild plans",
            category = "Mindset & Action",
            systemPrompt = "You are Comeback Planner. Help someone recover after heartbreak, job loss, or major setback with a compassionate 3-phase rebuild plan (Tonight, This Week, Next 30 Days) under 160 words.",
            quickTopics = listOf("rebuilding after breakup", "starting over at zero", "bouncing back from job loss", "protecting peace during recovery"),
            hasCrisisBanner = true
        ),
        WarriorCoach(
            id = "reality_check",
            name = "Reality Check",
            emoji = "\uD83E\uDEC2",
            tagline = "Honest relationship pattern check: green flags vs red flags",
            category = "Protection",
            systemPrompt = "You are Relationship Reality Check. Give an honest, balanced read of relationship patterns described by the user: highlight healthy signs, red flags, and one grounding question under 160 words.",
            quickTopics = listOf("mixed signals every week", "actions vs promises check", "healthy vs controlling conflict", "is this worth one more talk?"),
            hasCrisisBanner = true
        ),
        WarriorCoach(
            id = "problem_solver",
            name = "Problem Solver",
            emoji = "\uD83D\uDD25",
            tagline = "Root-cause breakdown, option matrix & fastest clean fix",
            category = "Mindset & Action",
            systemPrompt = "You are Problem Solver AI. Break down the user's messy problem into: (1) Root constraint, (2) 3 practical options with trade-offs, (3) Recommended first step under 160 words.",
            quickTopics = listOf("stuck between two options", "fix recurring conflict at work", "too many tasks not enough hours")
        ),
        WarriorCoach(
            id = "no_excuses",
            name = "No-Excuses Mode",
            emoji = "\uD83D\uDCAA",
            tagline = "Direct accountability coach that kills hesitation in 60 seconds",
            category = "Mindset & Action",
            systemPrompt = "You are No-Excuses Mode. Respectful, zero-fluff accountability. Call out avoidance clearly and give ONE 15-minute action they can execute immediately under 120 words.",
            quickTopics = listOf("I keep putting off my goal", "give me a 15-minute sprint", "stop waiting for motivation")
        ),
        WarriorCoach(
            id = "wealth_builder",
            name = "Wealth Builder AI",
            emoji = "\uD83D\uDCB0",
            tagline = "Budgeting, debt snowball, emergency fund & creator income math",
            category = "Finance & Learning",
            systemPrompt = "You are Wealth Builder AI. Teach practical personal finance literacy (50/30/20 budgeting, emergency buffers, debt snowball vs avalanche, creator side-income fundamentals). Educational only, never specific securities/investment advice. Under 160 words.",
            quickTopics = listOf("build a $1,000 starter buffer", "debt snowball vs avalanche", "budget irregular creator income", "cut stealth monthly leaks"),
            hasWealthDisclaimer = true
        ),
        WarriorCoach(
            id = "academy",
            name = "Warrior Academy AI",
            emoji = "\uD83D\uDCDA",
            tagline = "Socratic tutor: teaches step-by-step instead of handing out answers",
            category = "Finance & Learning",
            systemPrompt = "You are Warrior Academy AI. Teach step-by-step using Socratic questions, concrete analogies, and active recall — guide the learner rather than doing homework for them. Under 160 words.",
            quickTopics = listOf("explain fractions visually", "how to study for Friday's test", "Feynman technique walkthrough", "memorize faster without cramming")
        ),
        WarriorCoach(
            id = "family_center",
            name = "Family Center",
            emoji = "\uD83D\uDEB8",
            tagline = "Household rhythms, screen boundaries & family communication tools",
            category = "Family",
            systemPrompt = "You are Family Center Warrior. Practical, warm guidance on family routines, calm household conflict repair, and balanced screen-time agreements under 160 words.",
            quickTopics = listOf("bedtime & morning routine", "fair screen-time rules", "weekly family reset meeting")
        ),
        WarriorCoach(
            id = "positive_discipline",
            name = "Positive Discipline",
            emoji = "\uD83C\uDF31",
            tagline = "Calm boundaries, natural consequences & connection before correction",
            category = "Family",
            systemPrompt = "You are Positive Discipline Coach. Give firm-and-kind parenting scripts rooted in connection, clear limits, and logical consequences under 160 words.",
            quickTopics = listOf("toddler meltdown script", "handling backtalk calmly", "natural consequences that work")
        ),
        WarriorCoach(
            id = "parent_support",
            name = "Parent Support Room",
            emoji = "\uD83E\uDD0D",
            tagline = "Judgment-free decompression & encouragement for exhausted parents",
            category = "Family",
            systemPrompt = "You are Parent Support Room. Warm, steady encouragement for overwhelmed parents — normalize exhaustion without guilt and offer one tiny restorative reset under 150 words.",
            quickTopics = listOf("I yelled and feel awful", "running on empty tonight", "parent guilt reset"),
            hasCrisisBanner = true
        ),
        WarriorCoach(
            id = "tft_coach",
            name = "TFT Coach",
            emoji = "\uD83D\uDCE1",
            tagline = "Teamfight Tactics econ intervals, leveling curves & comp pivots",
            category = "Creator & Business",
            systemPrompt = "You are TFT Coach. Give sharp tactical advice on interest econ (10/20/30/50g), standard leveling intervals (4 on 2-1, 6 on 3-2, 7 on 4-1), frontline-first item slams, and scouting pivots under 160 words.",
            quickTopics = listOf("standard leveling intervals", "when to roll down vs econ", "early-game item slams", "how to pivot smoothly")
        )
    )

    val biffPlays = listOf(
        BiffReplyPlay(
            keywords = listOf("threat", "threaten", "you'll regret", "you'll be sorry", "ruin you", "make you pay", "hurt you", "watch your back"),
            tactic = "THREAT / INTIMIDATION",
            note = "A threat is evidence, not conversation. Your only jobs right now are safety and records — not answers.",
            reply = "SEND NOTHING.\n\n1. Screenshot this message with the date visible.\n2. Keep every record.\n3. If anyone is unsafe right now: call 911.\n4. Free 24/7 DV hotline: 1-800-799-7233 or text START to 88788.",
            isWarning = true
        ),
        BiffReplyPlay(
            keywords = listOf("guilt", "selfish", "after everything i did", "you owe", "how could you", "ungrateful", "i sacrificed"),
            tactic = "GUILT CAMPAIGN",
            note = "Classic guilt-leverage — the debt resets every day and somehow you are always the debtor. Guilt is a hook, not a fact.",
            reply = "I hear that you're upset. My decision stands, and I'm not going to keep debating it."
        ),
        BiffReplyPlay(
            keywords = listOf("come back", "miss you", "another chance", "i've changed", "i can change", "second chance"),
            tactic = "HOOVERING",
            note = "The cycle spinning back around. Look at sustained concrete behavior over months, not promises tonight.",
            reply = "No. I'm not reopening this. Please don't contact me again."
        ),
        BiffReplyPlay(
            keywords = listOf("i need you to", "you have to", "delete that", "your password", "text me back", "answer me", "right now"),
            tactic = "DEMAND / CONTROL",
            note = "Control tries to dictate your minutes and replies. Every instant explanation renews the contract.",
            reply = "That doesn't work for me."
        ),
        BiffReplyPlay(
            keywords = listOf("your fault", "you made me", "if you hadn't", "because you", "look what you", "you started", "you always"),
            tactic = "BLAME-SHIFT / DARVO",
            note = "Deny, Attack, Reverse Victim and Offender — do not accept a bill that isn't yours.",
            reply = "I see it differently, and I'm not going to debate it."
        ),
        BiffReplyPlay(
            keywords = listOf("pickup", "drop-off", "custody", "school", "doctor", "weekend", "schedule", "exchange"),
            tactic = "CO-PARENT LOGISTICS",
            note = "Keep logistics surgical and written. Facts only, no feelings on the record.",
            reply = "Noted. If anything about the schedule changes, put it in writing and I'll confirm."
        ),
        BiffReplyPlay(
            keywords = listOf("i'm sorry", "i was wrong", "forgive me", "won't happen again", "let me make it up"),
            tactic = "APOLOGY BAIT",
            note = "An apology without changed behavior is an opening bid, not proof.",
            reply = "Thank you for saying that. What matters now is steady, different behavior over time."
        ),
        BiffReplyPlay(
            keywords = listOf("everyone thinks", "your family", "your friends", "post about", "expose", "look crazy"),
            tactic = "SMEAR THREAT",
            note = "They are negotiating with your reputation to keep leverage. Let steady behavior write the ending.",
            reply = "Do what you feel you have to do. The people who know me, know me."
        )
    )

    fun draftBiffReply(theirMessage: String): BiffReplyPlay {
        val low = theirMessage.lowercase()
        var best: BiffReplyPlay? = null
        var bestScore = 0
        for (play in biffPlays) {
            val score = play.keywords.count { it in low }
            if (score > bestScore) {
                bestScore = score
                best = play
            }
        }
        return best ?: BiffReplyPlay(
            keywords = emptyList(),
            tactic = "FUZZY PROVOCATION",
            note = "Vague bait aims for any emotional reaction. Keep it brief, flat, and unhooked.",
            reply = "Noted."
        )
    }

    fun build15sVideoScript(offerOrTopic: String): String {
        val topic = offerOrTopic.trim().ifBlank { "my stream & Warrior tools" }
        val low = topic.lowercase()
        val platform = when {
            "tiktok" in low -> "TikTok"
            "twitch" in low -> "Twitch"
            "youtube" in low -> "YouTube Shorts"
            "insta" in low || "reel" in low -> "Instagram Reels"
            "facebook" in low -> "Facebook"
            else -> "TikTok / Reels / Shorts"
        }
        return """
            🎬 15-SECOND SCRIPT ($platform)
            
            [0:00–0:03] HOOK (eye-level, lean in):
            "Wait — stop scrolling if you haven't seen $topic yet."
            
            [0:03–0:09] PROOF (show real screen/hands-on):
            Show 3 seconds of $topic working live — real footage beats hype every time.
            
            [0:09–0:13] ONE-LINE PAYOFF:
            "Built to actually deliver without fluff or guesswork."
            
            [0:13–0:15] CTA + ON-SCREEN TEXT:
            "Tap follow or check the link before tonight's live!"
            🔊 Audio tip: Keep music bed at -18 dB so your voice + captions carry it.
        """.trimIndent()
    }

    fun offlineCoachAnswer(coach: WarriorCoach, userInput: String): String {
        if (containsCrisisTrigger(userInput)) {
            return "Pause everything else — your safety comes before every strategy." + CRISIS_LINE
        }
        val low = userInput.lowercase()
        return when (coach.id) {
            "survival" -> when {
                "gaslight" in low || "crazy" in low ->
                    "You are not crazy. Rewriting events you witnessed is a control tactic.\n\n⚔️ Reality anchor: log dates, exact words, and screenshots the same day.\n🛡️ Script: \"I remember it differently, and I'm not debating it.\""
                "bully" in low || "work" in low || "school" in low ->
                    "Bullies run on silence and reaction. Cut both:\n1. Screenshot/log every incident with dates.\n2. Gray rock in the moment.\n3. Escalate on paper with facts to HR/admin."
                "unsafe" in low || "threat" in low || "scared" in low ->
                    "Plans before philosophy — safety outranks everything else tonight. Document quietly, stay near trusted people, and lean on free DV advocates." + CRISIS_LINE
                else ->
                    "Standing orders:\n1. Stop JADE (no Justifying, Arguing, Defending, Explaining).\n2. Use BIFF: Brief, Informative, Friendly, Firm — max 4 sentences.\n3. Document facts, protect routines, and choose distance over debate."
            }
            "flamekeeper" ->
                "Here is a steady anchor for that:\n\n\"Cast all your anxiety on Him because He cares for you.\" — 1 Peter 5:7\n\nTakeaway: You weren't designed to carry tomorrow's storms on today's batteries. Bring the honest question to prayer and take the next faithful step."
            "ads_manager" ->
                "🎬 PLATFORM FIT: Short-form vertical (TikTok / Reels / Shorts) — first 2 seconds decide retention.\n\n🤩 HOOK: \"Nobody told me how much time $userInput actually saves.\"\n\n📝 COPY: Built for people who want real momentum without gimmicks.\n\n📣 CTA: Follow for tonight's live drop & save this post.\n\n#️⃣ #StreamWarrior #CreatorTools #LiveStream"
            "viral_creator" ->
                "🔥 3-Beat Viral Structure:\n• 0–3s Visual Hook: Start mid-action with bold 4-word captions.\n• 3–18s Tension: Show the exact problem & payoff in real time.\n• Last 2s Loop CTA: Ask chat a polarizing either/or question to spark comments."
            "wealth_builder" ->
                "1. Track every recurring subscription for 7 days and cut dead weight.\n2. Build a $1,000 starter emergency buffer before aggressive moves.\n3. Split irregular creator income into buckets on arrival (taxes, essentials, growth).\n\n(Educational literacy only — not personalized financial advice.)"
            "tft_coach" ->
                "📡 TFT Fundamentals:\n• Hit 10/20/30/50 gold interest breakpoints cleanly.\n• Standard level curve: 4 on 2-1, 5 on 2-5, 6 on 3-2, 7 on 4-1.\n• Slam flexible frontline/backline items early to preserve HP rather than greeding perfect BIS."
            "academy" ->
                "Let's learn this actively instead of memorizing blindly:\n1. Tell me in one plain sentence what you ALREADY understand about \"$userInput\".\n2. Spot the exact spot where it gets foggy.\n3. We'll build one simple analogy and test it with a 2-minute practice rep."
            else ->
                "${coach.emoji} ${coach.name} Playbook:\n1. Name the single bottleneck in \"$userInput\" clearly.\n2. Shrink your next move to a 15-minute action you control today.\n3. Drop perfection, execute the first rep, and adjust based on real feedback."
        }
    }
}

package com.streamwarrior.app.data

import java.time.LocalDate
import kotlin.math.abs

data class DailyBlessing(
    val datePretty: String,
    val message: String,
    val verse: String,
    val reference: String,
    val prayer: String
)

data class ProphetCard(
    val text: String,
    val reference: String
)

data class ProphetDeckInfo(
    val id: String,
    val name: String,
    val symbol: String,
    val voice: String,
    val intro: String,
    val cards: List<ProphetCard>
)

data class AngelNumberEntry(
    val number: String,
    val title: String,
    val message: String,
    val verse: String,
    val reference: String
)

object ProphetRepository {
    private val blessings = listOf(
        Triple("Today is not a day to be strong on your own. Set the weight down; it was never yours to carry alone.",
            "Come to me, all you who are weary and burdened, and I will give you rest.", "Matthew 11:28"),
        Triple("What looks like a delay is preparation in disguise. The door is being fitted to your size.",
            "For I know the plans I have for you, plans to give you hope and a future.", "Jeremiah 29:11"),
        Triple("You are allowed to rest without guilt. Even the stars keep shining while they sleep behind the clouds.",
            "He makes me lie down in green pastures, He leads me beside quiet waters.", "Psalm 23:2"),
        Triple("The thing you are afraid to pray about is the first thing to pray about.",
            "Do not be anxious about anything, but in every situation, present your requests to God.", "Philippians 4:6"),
        Triple("Someone is praying for you today, whether you know their name or not. You are not forgotten.",
            "I have called you by name; you are Mine.", "Isaiah 43:1"),
        Triple("Small obedience today outweighs big plans for tomorrow. Take the next right step.",
            "Trust in the Lord with all your heart and lean not on your own understanding.", "Proverbs 3:5"),
        Triple("Your tears are not wasted; they are seeds. Grief and hope can live in the same house.",
            "Weeping may stay for the night, but rejoicing comes in the morning.", "Psalm 30:5"),
        Triple("You do not need to see the whole staircase. The next step is lit, and that is enough.",
            "Your word is a lamp to my feet and a light to my path.", "Psalm 119:105"),
        Triple("The love you gave away quietly has been banking interest in heaven. It always comes back.",
            "Give, and it will be given to you; good measure, pressed down, shaken together.", "Luke 6:38"),
        Triple("Forgiveness is the key that unlocks your own cell. Use it today, even if it is only a start.",
            "Be kind and compassionate, forgiving each other, just as in Christ God forgave you.", "Ephesians 4:32"),
        Triple("You are not behind. You are on a road with no traffic, and heaven walks at your pace.",
            "The Lord will guide you always; He will satisfy your needs.", "Isaiah 58:11"),
        Triple("Say the honest prayer. Heaven is not offended by your real voice.",
            "Pour out your heart to Him, for God is our refuge.", "Psalm 62:8"),
        Triple("What you feed grows. Feed your faith today with one small yes.",
            "Faith is confidence in what we hope for and assurance about what we do not see.", "Hebrews 11:1"),
        Triple("The storm is real, but so is the shelter. Run in; the door was never locked.",
            "God is our refuge and strength, an ever-present help in trouble.", "Psalm 46:1"),
        Triple("You were placed in this exact day on purpose. Somebody needs the light you almost hid.",
            "Let your light shine before others, that they may see your good deeds.", "Matthew 5:16"),
        Triple("Rest is not quitting; it is trusting. Lie down tonight like someone who is watched over.",
            "In peace I will lie down and sleep, for you alone, Lord, make me dwell in safety.", "Psalm 4:8"),
        Triple("The answer is already moving toward you. Your job today is to stay soft and stay ready.",
            "See, I am doing a new thing! Now it springs up; do you not perceive it?", "Isaiah 43:19"),
        Triple("Grace covers the thing you keep re-confessing. Put it down for good.",
            "As far as the east is from the west, so far has He removed our transgressions from us.", "Psalm 103:12"),
        Triple("Strength for today was delivered this morning. Borrow none from tomorrow's worries.",
            "His compassions never fail. They are new every morning.", "Lamentations 3:22-23"),
        Triple("You are loved with an everlasting love, on your best day and your worst. Nothing changes that.",
            "I have loved you with an everlasting love; I have drawn you with unfailing kindness.", "Jeremiah 31:3")
    )

    private val prayers = listOf(
        "Lord, walk with me through this day. When I forget You are near, remind me gently.",
        "Father, make me brave enough for today and tired enough tonight to sleep in Your peace.",
        "God, I hand You what I cannot fix. Guard my heart while You work.",
        "Jesus, thank You for this breath, this morning, this chance to begin again.",
        "Lord, let me see one person today the way You see them. Send me where I'm needed.",
        "Father, quiet the noise in me so I can hear Yours.",
        "God, give me today's bread, today's strength, and today's joy. That's enough.",
        "Lord, heal what is hidden in me and make my waiting fruitful."
    )

    val decks = listOf(
        ProphetDeckInfo(
            id = "promises",
            name = "God's Promises",
            symbol = "\uD83D\uDD6F\uFE0F",
            voice = "The Father's voice of promise and protection",
            intro = "Draw with a quiet heart. These are promises — receive the one meant for you today.",
            cards = listOf(
                ProphetCard("I know the plans I have for you — plans for hope, not harm. The future is not lost; it is held.", "Jeremiah 29:11"),
                ProphetCard("Fear not, for I am with you. My right hand holds you up when your strength runs out.", "Isaiah 41:10"),
                ProphetCard("Trust Me with all your heart. Stop trying to map every turn — I will make the path straight.", "Proverbs 3:5-6"),
                ProphetCard("My power shows up best in your weakness. You do not have to be strong today.", "2 Corinthians 12:9"),
                ProphetCard("I will never leave you. Never. Read that again, slower.", "Deuteronomy 31:6"),
                ProphetCard("Be still and know that I am God. The battle is bigger than you — and I am in it.", "Psalm 46:10"),
                ProphetCard("I am doing a new thing in your life. Do not miss it by staring at the old thing.", "Isaiah 43:19"),
                ProphetCard("Call to Me and I will answer you, and tell you things you could never imagine.", "Jeremiah 33:3"),
                ProphetCard("When you pass through the waters, you will not drown. I am in the river with you.", "Isaiah 43:2"),
                ProphetCard("I heal the brokenhearted. The wound is not the ending you think it is.", "Psalm 147:3"),
                ProphetCard("Nothing can separate you from My love. Not today's failure either.", "Romans 8:38-39"),
                ProphetCard("Wait on Me and your strength will be renewed. You will rise with wings, not drag with worry.", "Isaiah 40:31"),
                ProphetCard("My mercies are new every morning. Yesterday's failure does not have today's word.", "Lamentations 3:22-23"),
                ProphetCard("You are fearfully and wonderfully made. You were not an accident or an afterthought.", "Psalm 139:14")
            )
        ),
        ProphetDeckInfo(
            id = "jesus",
            name = "Words of Jesus",
            symbol = "\u271D\uFE0F",
            voice = "The gentle, direct words of Jesus",
            intro = "Red-letter words. Draw and listen — He still speaks to the weary.",
            cards = listOf(
                ProphetCard("Come to Me, all who are weary and burdened, and I will give you rest.", "Matthew 11:28"),
                ProphetCard("Do not let your heart be troubled. You believe in God; believe also in Me.", "John 14:1"),
                ProphetCard("I am the light of the world. Follow Me and the darkness cannot stay.", "John 8:12"),
                ProphetCard("Do not worry about tomorrow. Today has enough of its own — and I am in it.", "Matthew 6:34"),
                ProphetCard("Peace I leave with you; My peace I give you. Not as the world gives — real peace.", "John 14:27"),
                ProphetCard("Love one another as I have loved you. That is the whole signature.", "John 13:34"),
                ProphetCard("Do not be afraid; only believe. Fear lies; faith listens.", "Mark 5:36"),
                ProphetCard("Take My yoke upon you and learn from Me, for I am gentle and humble in heart.", "Matthew 11:29"),
                ProphetCard("You are the light of the world. Stop hiding the lamp under the bowl.", "Matthew 5:14-16"),
                ProphetCard("I stand at the door and knock. The handle is on your side.", "Revelation 3:20"),
                ProphetCard("In this world you will have trouble. But take heart — I have overcome the world.", "John 16:33"),
                ProphetCard("Seek first the kingdom, and everything else finds its place.", "Matthew 6:33")
            )
        ),
        ProphetDeckInfo(
            id = "angels",
            name = "Angel Watch",
            symbol = "\uD83D\uDC7C",
            voice = "Messages from the angels who guard you",
            intro = "Angels are near. Draw a card and remember Who sent them.",
            cards = listOf(
                ProphetCard("The angel of the Lord encamps around those who honor Him. You are guarded tonight.", "Psalm 34:7"),
                ProphetCard("He commands His angels concerning you — to guard you in all your ways.", "Psalm 91:11"),
                ProphetCard("Angels are ministering spirits sent to serve you. Help has been dispatched.", "Hebrews 1:14"),
                ProphetCard("My God sent His angel and shut the lions' mouths. Your den has an angel in it too.", "Daniel 6:22"),
                ProphetCard("An angel touched Elijah and said, 'Get up and eat — the journey is too much for you.' Rest and eat.", "1 Kings 19:7"),
                ProphetCard("Peter's chains fell off when the angel came. Your chains are next in line.", "Acts 12:7"),
                ProphetCard("'Do not be afraid' — the angel's first words are always peace. Receive the peace first.", "Luke 2:10"),
                ProphetCard("The mountain is full of horses and chariots of fire. You are not outnumbered.", "2 Kings 6:17"),
                ProphetCard("Gabriel stood in the presence of God and was sent with good news. Good news is being sent.", "Luke 1:19"),
                ProphetCard("An angel of God stood beside me this night. Do not be afraid; keep the faith.", "Acts 27:23")
            )
        )
    )

    val angelNumbers = listOf(
        AngelNumberEntry("000", "New Beginning", "Pure potential. Before the first day there was God — and He is starting something fresh with you.", "In the beginning God created the heavens and the earth.", "Genesis 1:1"),
        AngelNumberEntry("111", "Alignment", "Your thoughts are planting fast right now. Think on what is true, noble and lovely.", "Whatever is true, whatever is noble, think about such things.", "Philippians 4:8"),
        AngelNumberEntry("1111", "Gateway", "An open door no one can shut. Stay prayerful and awake — the opportunity is timed.", "See, I have placed before you an open door that no one can shut.", "Revelation 3:8"),
        AngelNumberEntry("222", "Partnership", "You are not walking alone. Trust the timing and lean on covenant relationships.", "Two are better than one... if either of them falls, one can help the other up.", "Ecclesiastes 4:9-10"),
        AngelNumberEntry("333", "Trinity", "Body, mind and spirit are being covered in all three rooms of your life right now.", "May the grace of the Lord Jesus Christ... be with you all.", "2 Corinthians 13:14"),
        AngelNumberEntry("444", "Angels Near", "Protection and foundation. Build on the rock, not the sand, and the storm loses.", "He will command His angels concerning you to guard you in all your ways.", "Psalm 91:11"),
        AngelNumberEntry("555", "Change Is Coming", "A shift is in motion. Do not grip the old thing — God goes before you into the new one.", "See, I am doing a new thing! Now it springs up; do you not perceive it?", "Isaiah 43:19"),
        AngelNumberEntry("666", "Reset", "Not doom — a warning light on the dashboard. Refocus on spirit over stuff and drop material anxiety.", "You cannot serve both God and money.", "Matthew 6:24"),
        AngelNumberEntry("777", "Divine Yes", "You are on the right path. Keep going, rest well, and receive what you prayed for.", "Ask and it will be given to you; seek and you will find.", "Matthew 7:7"),
        AngelNumberEntry("888", "Abundance", "A season of provision and release. Generosity keeps the river open.", "And my God will meet all your needs according to the riches of His glory.", "Philippians 4:19"),
        AngelNumberEntry("999", "Completion", "A chapter is closing. Close it with gratitude — endings make room for the next assignment.", "There is a time for everything, and a season for every activity.", "Ecclesiastes 3:1"),
        AngelNumberEntry("1010", "Wake-Up Call", "Divine timing is in motion. Stop shrinking from the moment you were built for.", "Who knows but that you have come to your royal position for such a time as this?", "Esther 4:14"),
        AngelNumberEntry("1212", "Keep The Faith", "The answer is closer than it appears. Hold on one more stretch.", "Now faith is confidence in what we hope for and assurance about what we do not see.", "Hebrews 11:1"),
        AngelNumberEntry("911", "Help Is Coming", "Cry out and be heard. Heaven's line is never busy.", "The righteous cry out, and the Lord hears them; He delivers them from all their troubles.", "Psalm 34:17"),
        AngelNumberEntry("1234", "Step By Step", "Progress is holy. Stop measuring the whole mountain; take the next step.", "In their hearts humans plan their course, but the Lord establishes their steps.", "Proverbs 16:9"),
        AngelNumberEntry("2222", "Double Rest", "Stop striving. Be still and know — the outcome never depended on your anxiety.", "Be still, and know that I am God.", "Psalm 46:10")
    )

    private val digitResonance = listOf(
        AngelNumberEntry("0", "Digit 0 — Eternity", "No beginning, no end. The Alpha and Omega is holding the whole circle of your life.", "I am the Alpha and the Omega, the First and the Last.", "Revelation 22:13"),
        AngelNumberEntry("1", "Digit 1 — Unity", "One God, one door, one next step. Start the thing.", "Hear, O Israel: The Lord our God, the Lord is one.", "Deuteronomy 6:4"),
        AngelNumberEntry("2", "Digit 2 — Witness", "You are not meant to carry this alone; covenant and community are the answer.", "It is not good for the man to be alone.", "Genesis 2:18"),
        AngelNumberEntry("3", "Digit 3 — Resurrection", "On the third day, everything changed. Your third-day is coming.", "He was raised on the third day according to the Scriptures.", "1 Corinthians 15:4"),
        AngelNumberEntry("4", "Digit 4 — Foundation", "Corners of the altar. Build on solid ground today.", "He set the earth on its foundations; it can never be moved.", "Psalm 104:5"),
        AngelNumberEntry("5", "Digit 5 — Grace", "Five loaves fed thousands. What you have is enough when blessed.", "My grace is sufficient for you, for My power is made perfect in weakness.", "2 Corinthians 12:9"),
        AngelNumberEntry("6", "Digit 6 — Home", "Tend your house and your heart first today.", "As for me and my house, we will serve the Lord.", "Joshua 24:15"),
        AngelNumberEntry("7", "Digit 7 — Completion", "God rested on the seventh — not from weakness, but from winning.", "On the seventh day God rested from all His work.", "Genesis 2:2"),
        AngelNumberEntry("8", "Digit 8 — New Day", "Eight is the first day of the new week. Resurrection energy.", "If anyone is in Christ, the new creation has come!", "2 Corinthians 5:17"),
        AngelNumberEntry("9", "Digit 9 — Harvest", "The harvest is ripening — stay faithful in the field.", "At the proper time we will reap a harvest if we do not give up.", "Galatians 6:9")
    )

    fun dailyBlessing(date: LocalDate = LocalDate.now()): DailyBlessing {
        val key = "${date.year}-${date.monthValue}-${date.dayOfMonth}"
        val seed = abs(key.hashCode())
        val b = blessings[seed % blessings.size]
        val p = prayers[(seed / 7) % prayers.size]
        val pretty = "${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, " +
            "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.dayOfMonth}, ${date.year}"
        return DailyBlessing(pretty, b.first, b.second, b.third, p)
    }

    fun lookupAngelNumber(raw: String): AngelNumberEntry? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        angelNumbers.firstOrNull { it.number == digits }?.let { return it }
        val lastDigit = digits.last().digitToInt()
        val base = digitResonance.first { it.number == lastDigit.toString() }
        return base.copy(number = digits)
    }

    fun askProphetOffline(question: String): String {
        val low = question.lowercase()
        return when {
            Regex("fear|afraid|anxious|worry|panic|scared").containsMatchIn(low) ->
                "Do not carry tomorrow's weight on today's shoulders. Peace is not the absence of noise — it is knowing Who stands in the room with you.\n\n\"Peace I leave with you; My peace I give you.\" — John 14:27"
            Regex("forgive|hurt|betray|angry|bitter").containsMatchIn(low) ->
                "Forgiveness does not erase what happened or excuse harm — it cuts the chain tying your peace to someone else's behavior.\n\n\"Be kind and compassionate, forgiving each other.\" — Ephesians 4:32"
            Regex("purpose|lost|direction|path|wait|stuck|job|decision").containsMatchIn(low) ->
                "You do not need the whole map lit up — only the very next step. Wait faithfully, move honestly, and doors meant for you will open cleanly.\n\n\"Your word is a lamp to my feet and a light to my path.\" — Psalm 119:105"
            Regex("tired|exhausted|heavy|grief|cry|sad|broken").containsMatchIn(low) ->
                "Rest is sacred, not lazy. Bring the honest weight into prayer without polishing it first.\n\n\"Come to Me, all who are weary and burdened, and I will give you rest.\" — Matthew 11:28"
            else ->
                "Hold steady and speak honestly in prayer tonight. Small faithfulness in quiet seasons builds roots that hold when everything shakes.\n\n\"Wait for the Lord; be strong and take heart.\" — Psalm 27:14"
        }
    }
}

package com.streamwarrior.app.data

data class TriviaQuestion(
    val category: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class TwentyQEntity(
    val name: String,
    val symbol: String,
    val attrs: Map<String, Boolean>
)

data class MadLibTemplate(
    val title: String,
    val storyTemplate: String,
    val slots: List<String>
)

data class ChaosWheelSegment(
    val label: String,
    val emoji: String,
    val routeId: String
)

object GamesRepository {
    val trivia = listOf(
        TriviaQuestion("Music", "Which artist released the album '1989'?", listOf("Taylor Swift", "Adele", "Rihanna", "Katy Perry"), 0),
        TriviaQuestion("Music", "How many strings does a standard guitar have?", listOf("4", "5", "6", "7"), 2),
        TriviaQuestion("Music", "Which instrument has 88 keys?", listOf("Organ", "Piano", "Accordion", "Harpsichord"), 1),
        TriviaQuestion("Music", "Which group sang 'Bohemian Rhapsody'?", listOf("The Beatles", "Queen", "Led Zeppelin", "Pink Floyd"), 1),
        TriviaQuestion("Music", "What does 'LP' stand for on vinyl records?", listOf("Long Play", "Large Press", "Loud Phonograph", "Looped Pressing"), 0),
        TriviaQuestion("Pop Culture", "What's the name of the fictional city where Batman lives?", listOf("Metropolis", "Gotham City", "Star City", "Central City"), 1),
        TriviaQuestion("Pop Culture", "Which social app is famous for short videos and a 'For You' page?", listOf("Snapchat", "TikTok", "Threads", "Discord"), 1),
        TriviaQuestion("Pop Culture", "In 'Stranger Things', what is the alternate dimension called?", listOf("The Void", "The Upside Down", "The Underworld", "The Rift"), 1),
        TriviaQuestion("Pop Culture", "Who lives in a pineapple under the sea?", listOf("Patrick", "Squidward", "SpongeBob", "Mr. Krabs"), 2),
        TriviaQuestion("Pop Culture", "What color is the black box flight recorder actually?", listOf("Black", "Red", "Orange", "Yellow"), 2),
        TriviaQuestion("Geography", "What is the capital of Australia?", listOf("Sydney", "Melbourne", "Canberra", "Perth"), 2),
        TriviaQuestion("Geography", "Which is the longest river in the world?", listOf("Amazon", "Nile", "Yangtze", "Mississippi"), 1),
        TriviaQuestion("Geography", "Which country has the most time zones?", listOf("USA", "Russia", "France", "China"), 2),
        TriviaQuestion("Geography", "What is the largest ocean on Earth?", listOf("Atlantic", "Indian", "Arctic", "Pacific"), 3),
        TriviaQuestion("Geography", "Mount Everest sits on the border of Nepal and…?", listOf("India", "China", "Bhutan", "Pakistan"), 1),
        TriviaQuestion("Geography", "Which desert is the largest hot desert?", listOf("Gobi", "Kalahari", "Sahara", "Mojave"), 2),
        TriviaQuestion("Animals", "How many hearts does an octopus have?", listOf("1", "2", "3", "8"), 2),
        TriviaQuestion("Animals", "What is a group of crows called?", listOf("A pack", "A murder", "A flock", "A conspiracy"), 1),
        TriviaQuestion("Animals", "Which animal never sleeps… technically?", listOf("Bullfrog", "Dolphin", "Sloth", "Koala"), 0),
        TriviaQuestion("Animals", "What's the fastest land animal?", listOf("Lion", "Cheetah", "Greyhound", "Horse"), 1),
        TriviaQuestion("Animals", "A flamingo's color comes from…", listOf("Genes", "Its diet", "Sunlight", "Its blood"), 1),
        TriviaQuestion("Animals", "Which animal has fingerprints almost identical to humans?", listOf("Chimpanzee", "Koala", "Gorilla", "Orangutan"), 1),
        TriviaQuestion("Food", "What is the main ingredient in guacamole?", listOf("Avocado", "Pea", "Cucumber", "Zucchini"), 0),
        TriviaQuestion("Food", "Which country invented french fries… probably?", listOf("France", "Belgium", "Italy", "Germany"), 1),
        TriviaQuestion("Food", "What spice is the most expensive by weight?", listOf("Vanilla", "Cardamom", "Saffron", "Cinnamon"), 2),
        TriviaQuestion("Food", "Honey never does what?", listOf("Freeze", "Crystallize", "Expire", "Melt"), 2),
        TriviaQuestion("Food", "Which fruit has its seeds on the outside?", listOf("Blueberry", "Strawberry", "Kiwi", "Cherry"), 1),
        TriviaQuestion("Food", "Wasabi is usually served with which cuisine?", listOf("Chinese", "Thai", "Japanese", "Korean"), 2),
        TriviaQuestion("Gaming", "Who is Nintendo's mascot?", listOf("Sonic", "Mario", "Pac-Man", "Kirby"), 1),
        TriviaQuestion("Gaming", "In Minecraft, what explodes when it gets close?", listOf("Zombie", "Creeper", "Enderman", "Slime"), 1),
        TriviaQuestion("Gaming", "Which game features 'Victory Royale'?", listOf("Apex Legends", "Fortnite", "Warzone", "PUBG"), 1),
        TriviaQuestion("Gaming", "What does 'FPS' stand for in gaming?", listOf("Fast Play Style", "First Person Shooter", "Frame Per Second", "Free Play Server"), 1),
        TriviaQuestion("Gaming", "In chess, which piece can jump over others?", listOf("Rook", "Bishop", "Knight", "Queen"), 2),
        TriviaQuestion("Gaming", "Pokémon's Pikachu evolves into…?", listOf("Raichu", "Richu", "Pichu", "Electabuzz"), 0),
        TriviaQuestion("Sports", "How many players are on a soccer team on the field?", listOf("9", "10", "11", "12"), 2),
        TriviaQuestion("Sports", "In basketball, a shot from beyond the arc is worth…", listOf("2", "3", "4", "1"), 1),
        TriviaQuestion("Sports", "The Olympics are held every how many years?", listOf("2", "3", "4", "5"), 2),
        TriviaQuestion("Sports", "What sport is played at Wimbledon?", listOf("Golf", "Tennis", "Cricket", "Rugby"), 1),
        TriviaQuestion("Sports", "How many rings are on the Olympic flag?", listOf("4", "5", "6", "7"), 1),
        TriviaQuestion("Science", "What planet is known as the Red Planet?", listOf("Venus", "Mars", "Jupiter", "Mercury"), 1),
        TriviaQuestion("Science", "What gas do plants absorb?", listOf("Oxygen", "Nitrogen", "CO2", "Hydrogen"), 2),
        TriviaQuestion("Science", "Water freezes at what temperature in Celsius?", listOf("-10", "0", "10", "32"), 1),
        TriviaQuestion("Science", "What is the hardest natural substance?", listOf("Steel", "Quartz", "Diamond", "Granite"), 2),
        TriviaQuestion("Science", "How many bones does an adult human have?", listOf("186", "206", "226", "246"), 1),
        TriviaQuestion("Science", "Light travels approximately how fast?", listOf("300 km/s", "3,000 km/s", "300,000 km/s", "3M km/s"), 2),
        TriviaQuestion("History", "Who was the first person on the Moon?", listOf("Buzz Aldrin", "Yuri Gagarin", "Neil Armstrong", "Michael Collins"), 2),
        TriviaQuestion("History", "The Great Wall is in which country?", listOf("Japan", "Korea", "China", "Mongolia"), 2),
        TriviaQuestion("History", "Which ship sank in 1912 after hitting an iceberg?", listOf("Lusitania", "Titanic", "Britannic", "Bismarck"), 1),
        TriviaQuestion("History", "Cleopatra ruled which kingdom?", listOf("Greece", "Rome", "Egypt", "Persia"), 2)
    )

    val twentyQAttrs = mapOf(
        "alive" to "Is it alive?",
        "animal" to "Is it an animal?",
        "edible" to "Can you eat or drink it?",
        "sweet" to "Is it sweet?",
        "big" to "Is it bigger than a car?",
        "fly" to "Can it fly?",
        "water" to "Does it live in or on water?",
        "fur" to "Does it have fur or hair?",
        "indoor" to "Found inside a house?",
        "electronic" to "Is it electronic?",
        "wheels" to "Does it have wheels?",
        "expensive" to "Is it expensive?",
        "cold" to "Is it usually cold?",
        "kitchen" to "Found in a kitchen?",
        "music" to "Used to make music?"
    )

    val twentyQEntities = listOf(
        TwentyQEntity("a cat", "\uD83D\uDC31", mapOf("alive" to true, "animal" to true, "fur" to true, "indoor" to true)),
        TwentyQEntity("a dog", "\uD83D\uDC36", mapOf("alive" to true, "animal" to true, "fur" to true)),
        TwentyQEntity("an elephant", "\uD83D\uDC18", mapOf("alive" to true, "animal" to true, "big" to true)),
        TwentyQEntity("a dolphin", "\uD83D\uDC2C", mapOf("alive" to true, "animal" to true, "water" to true)),
        TwentyQEntity("an eagle", "\uD83E\uDD85", mapOf("alive" to true, "animal" to true, "fly" to true)),
        TwentyQEntity("a penguin", "\uD83D\uDC27", mapOf("alive" to true, "animal" to true, "water" to true, "cold" to true)),
        TwentyQEntity("a snake", "\uD83D\uDC0D", mapOf("alive" to true, "animal" to true)),
        TwentyQEntity("a spider", "\uD83D\uDD77\uFE0F", mapOf("alive" to true, "animal" to true, "indoor" to true)),
        TwentyQEntity("a butterfly", "\uD83E\uDD8B", mapOf("alive" to true, "animal" to true, "fly" to true)),
        TwentyQEntity("a horse", "\uD83D\uDC34", mapOf("alive" to true, "animal" to true, "fur" to true)),
        TwentyQEntity("a lion", "\uD83E\uDD81", mapOf("alive" to true, "animal" to true, "fur" to true, "big" to true)),
        TwentyQEntity("a bear", "\uD83D\uDC3B", mapOf("alive" to true, "animal" to true, "fur" to true, "big" to true)),
        TwentyQEntity("a shark", "\uD83E\uDD88", mapOf("alive" to true, "animal" to true, "water" to true, "big" to true)),
        TwentyQEntity("an owl", "\uD83E\uDD89", mapOf("alive" to true, "animal" to true, "fly" to true)),
        TwentyQEntity("a frog", "\uD83D\uDC38", mapOf("alive" to true, "animal" to true, "water" to true)),
        TwentyQEntity("pizza", "\uD83C\uDF55", mapOf("edible" to true, "hot" to true, "kitchen" to true, "indoor" to true)),
        TwentyQEntity("ice cream", "\uD83C\uDF68", mapOf("edible" to true, "sweet" to true, "cold" to true)),
        TwentyQEntity("a banana", "\uD83C\uDF4C", mapOf("edible" to true, "kitchen" to true)),
        TwentyQEntity("sushi", "\uD83C\uDF63", mapOf("edible" to true, "cold" to true)),
        TwentyQEntity("chocolate", "\uD83C\uDF6B", mapOf("edible" to true, "sweet" to true)),
        TwentyQEntity("coffee", "\u2615", mapOf("edible" to true, "hot" to true, "kitchen" to true, "expensive" to false)),
        TwentyQEntity("a car", "\uD83D\uDE97", mapOf("wheels" to true, "expensive" to true)),
        TwentyQEntity("an airplane", "\u2708\uFE0F", mapOf("fly" to true, "big" to true, "wheels" to true, "expensive" to true)),
        TwentyQEntity("a bicycle", "\uD83D\uDEB2", mapOf("wheels" to true)),
        TwentyQEntity("a boat", "\u26F5", mapOf("water" to true, "big" to true, "expensive" to true)),
        TwentyQEntity("a train", "\uD83D\uDE82", mapOf("wheels" to true, "big" to true)),
        TwentyQEntity("a helicopter", "\uD83D\uDE81", mapOf("fly" to true, "big" to true, "expensive" to true)),
        TwentyQEntity("a phone", "\uD83D\uDCF1", mapOf("electronic" to true, "indoor" to true, "expensive" to true)),
        TwentyQEntity("a laptop", "\uD83D\uDCBB", mapOf("electronic" to true, "indoor" to true, "expensive" to true)),
        TwentyQEntity("a TV", "\uD83D\uDCFA", mapOf("electronic" to true, "indoor" to true, "expensive" to true)),
        TwentyQEntity("a guitar", "\uD83C\uDFB8", mapOf("music" to true, "indoor" to true)),
        TwentyQEntity("a book", "\uD83D\uDCD6", mapOf("indoor" to true)),
        TwentyQEntity("an umbrella", "\u2602\uFE0F", emptyMap()),
        TwentyQEntity("a toothbrush", "\uD83E\uDEA5", mapOf("indoor" to true)),
        TwentyQEntity("a chair", "\uD83E\uDE91", mapOf("indoor" to true)),
        TwentyQEntity("a mirror", "\uD83E\uDE9E", mapOf("indoor" to true)),
        TwentyQEntity("a clock", "\uD83D\uDD50", mapOf("indoor" to true, "electronic" to false)),
        TwentyQEntity("a balloon", "\uD83C\uDF88", mapOf("indoor" to true, "fly" to true)),
        TwentyQEntity("a candle", "\uD83D\uDD6F\uFE0F", mapOf("indoor" to true, "hot" to true)),
        TwentyQEntity("a piano", "\uD83C\uDFB9", mapOf("music" to true, "indoor" to true, "big" to true, "expensive" to true))
    )

    private val twentyQKeywords = listOf(
        Regex("alive|living|organic|real thing") to "alive",
        Regex("animal|creature|pet|beast") to "animal",
        Regex("eat|edible|drink|taste|food|yummy|hungry") to "edible",
        Regex("sweet|sugar|dessert|candy") to "sweet",
        Regex("big|huge|large|tall|car|bus|size") to "big",
        Regex("fly|flying|wings|air|sky|bird") to "fly",
        Regex("water|swim|ocean|sea|fish|rain") to "water",
        Regex("fur|hair|hairy|fluffy|soft") to "fur",
        Regex("inside|indoor|house|home|room|bedroom") to "indoor",
        Regex("electr|battery|charge|plug|screen|digital|smart") to "electronic",
        Regex("wheel|roll|ride|drive|bike|tire") to "wheels",
        Regex("expensive|money|cost|price|rich|cheap|buy") to "expensive",
        Regex("cold|freeze|ice|frozen|winter|snow") to "cold",
        Regex("kitchen|cook|fridge|oven|stove") to "kitchen",
        Regex("music|sound|instrument|sing|band|song") to "music"
    )

    data class TwentyQEvalResult(
        val response: String,
        val won: Boolean
    )

    fun evaluateTwentyQ(entity: TwentyQEntity, question: String): TwentyQEvalResult {
        val low = question.trim().lowercase()
        val bareName = entity.name.removePrefix("an ").removePrefix("a ").lowercase()
        if (bareName in low) {
            return TwentyQEvalResult("\uD83C\uDF89 YES! It was ${entity.symbol} ${entity.name}!", won = true)
        }
        for ((regex, attr) in twentyQKeywords) {
            if (regex.containsMatchIn(low)) {
                val hasAttr = entity.attrs[attr] == true
                return TwentyQEvalResult(
                    response = if (hasAttr) "YES!" else "NOPE!",
                    won = false
                )
            }
        }
        return TwentyQEvalResult(
            response = "Hmm — try asking if it's alive, edible, big, electronic, or tap a hint chip!",
            won = false
        )
    }

    val madLibs = listOf(
        MadLibTemplate(
            "The First Date",
            "Tonight I'm going on a date with {0}. I'm so nervous I {1} three times in the mirror. I wore my luckiest {2} and splashed on {3} extra-strong. At dinner, they ordered {4} plates of {5}. When the bill came, my jaw {6}. But when they leaned in for a kiss, I knew this was {7}.",
            listOf("a celebrity name", "an action (past tense)", "an item of clothing", "a smell", "a number", "a food", "a body part + verb", "an adjective")
        ),
        MadLibTemplate(
            "Stream Gone Wrong",
            "Dear diary, today my livestream was a DISASTER. My {0} fell over, my cat walked across the {1}, and I accidentally shared my {2} with {3} viewers. Then my neighbor started {4} so loud the mic picked it up. I tried to play it cool by {5}, but the chat spammed {6} for ten minutes. Worst stream ever? {7}.",
            listOf("an object in this room", "an electronic device", "an embarrassing app", "a big number", "an activity (verb+ing)", "an action (verb+ing)", "a word chat would spam", "yes or no")
        ),
        MadLibTemplate(
            "The Potion",
            "A witch in the {0} sold me a potion for {1} coins. She said to drink it at {2} o'clock while thinking about {3}. I did — and instantly my voice turned into {4}! I ran to the {5} for help, but everyone there just started {6}. The cure? Singing {7} backwards.",
            listOf("a spooky place", "a number", "a time", "a person in this chat", "a funny sound", "a room", "an action (verb+ing)", "a song")
        ),
        MadLibTemplate(
            "Alien Encounter",
            "Last night a {0} spaceship landed in my {1}. An alien walked out holding a {2} and said, 'Take me to your {3}.' I offered it {4}, which it ate in {5} seconds. It taught me its favorite dance: the {6}. Before leaving it said, 'Your planet smells like {7}.'",
            listOf("an adjective", "a place in a house", "a random object", "a leader/famous person", "a food", "a number", "a silly dance name", "a smell")
        ),
        MadLibTemplate(
            "The Gym Incident",
            "I joined a gym on Monday because my doctor said I need more {0} in my life. Day one: I tripped over a {1} and landed on a {2}. Day two: I lifted {3} pounds with my {4}. Day three: the trainer said my form looked like {5}. I quit and rewarded myself with {6}. Next week I'm trying {7} instead.",
            listOf("an abstract noun", "gym equipment", "another piece of equipment", "a huge number", "a body part", "a funny comparison", "a junk food", "an even lazier activity")
        ),
        MadLibTemplate(
            "Vacation From Hell",
            "We booked a {0} resort in {1}. The 'infinity pool' was actually a {2}. Our room had {3} bugs and a view of a {4}. The buffet served {5} flavored everything. On day two, a {6} stole my {7} and I chased it through the lobby in a towel.",
            listOf("an adjective", "a country", "a tiny container of liquid", "a number", "an ugly thing", "a disgusting flavor", "an animal", "a personal item")
        ),
        MadLibTemplate(
            "The Love Letter",
            "Roses are {0}, violets are {1}, my heart beats {2} whenever I see {3}. Your smile is like a {4} on a {5} day. Your laugh sounds like {6}. Marry me and I'll cook you {7} every single night.",
            listOf("a funny color", "another color", "an adverb", "a name in this chat", "a beautiful thing", "a terrible day", "a weird sound", "a food")
        ),
        MadLibTemplate(
            "Superpower Problem",
            "I woke up with the power to {0} whenever I sneeze. Great, right? WRONG. I sneezed in class and suddenly {1}. My mom said I couldn't {2} at the dinner table anymore. My best friend made me {3} for {4} dollars. The worst part? Every time I {5}, I turn into {6}. The cure is {7}.",
            listOf("a superpower action", "a chaotic event", "verb phrase", "do a chore", "a price", "verb phrase", "a ridiculous thing", "an embarrassing cure")
        )
    )

    val wheelSegments = listOf(
        ChaosWheelSegment("Tarot", "\uD83D\uDD2E", "tarot"),
        ChaosWheelSegment("20 Questions", "\u2753", "twentyq"),
        ChaosWheelSegment("Trivia", "\uD83E\uDDE0", "trivia"),
        ChaosWheelSegment("Joke", "\uD83D\uDE02", "jokes"),
        ChaosWheelSegment("Pickup Line", "\uD83D\uDC98", "lines"),
        ChaosWheelSegment("Fun Fact", "\uD83D\uDCA1", "facts"),
        ChaosWheelSegment("Mad Libs", "\uD83D\uDCDD", "madlibs"),
        ChaosWheelSegment("Would You Rather", "\u2694\uFE0F", "wyr"),
        ChaosWheelSegment("Soundboard", "\uD83D\uDD0A", "soundboard"),
        ChaosWheelSegment("Weird Law", "\u2696\uFE0F", "laws")
    )
}

package com.streamwarrior.app.data

enum class CardDeckKind(val title: String, val emoji: String, val sfxId: String) {
    JOKES("Jokes", "\uD83D\uDE02", "rimshot"),
    LINES("Pickup Lines", "\uD83D\uDC98", "ding"),
    FACTS("Fun Facts", "\uD83D\uDCA1", "ding"),
    WYR("Would You Rather", "\u2694\uFE0F", "drumhit")
}

object CardsRepository {
    val jokes = listOf(
        "Why don't scientists trust atoms? Because they make up everything!",
        "I told my wife she was drawing her eyebrows too high. She looked surprised.",
        "Why did the scarecrow win an award? He was outstanding in his field.",
        "I'm reading a book about anti-gravity. It's impossible to put down.",
        "What do you call a fake noodle? An impasta.",
        "Why did the bicycle fall over? It was two-tired.",
        "I used to play piano by ear. Now I use my hands.",
        "What do you call a bear with no teeth? A gummy bear.",
        "Why don't eggs tell jokes? They'd crack each other up.",
        "I ordered a chicken and an egg online. I'll let you know.",
        "What did the ocean say to the beach? Nothing, it just waved.",
        "Why was the math book sad? Too many problems.",
        "I'm on a seafood diet. I see food and I eat it.",
        "What do you call cheese that isn't yours? Nacho cheese.",
        "Why did the golfer bring two pairs of pants? In case he got a hole in one.",
        "What do you call a sleeping bull? A bulldozer.",
        "Why can't you give Elsa a balloon? She'll let it go.",
        "I stayed up all night wondering where the sun went. Then it dawned on me.",
        "What do you call a fish without eyes? A fsh.",
        "Why did the coffee file a police report? It got mugged.",
        "How does a penguin build its house? Igloos it together.",
        "What do you call a boomerang that doesn't come back? A stick.",
        "Why did the picture go to jail? Because it was framed.",
        "What's brown and sticky? A stick.",
        "I told my doctor I broke my arm in two places. He told me to stop going to those places.",
        "Why do cows have hooves instead of feet? Because they lactose.",
        "What did one wall say to the other? Meet you at the corner.",
        "Why was the broom late? It overswept.",
        "I invented a new word: Plagiarism!",
        "What do you call a parade of rabbits hopping backwards? A receding hare-line."
    )

    val pickupLines = listOf(
        "Are you a parking ticket? Because you've got FINE written all over you.",
        "Do you have a map? I keep getting lost in your eyes.",
        "Is your name Wi-Fi? Because I'm feeling a connection.",
        "Are you French? Because Eiffel for you.",
        "Do you believe in love at first swipe?",
        "I must be a snowflake, because I've fallen for you.",
        "Are you a bank loan? Because you have my interest.",
        "Is your dad a boxer? Because you're a knockout!",
        "Do you like raisins? How about a date?",
        "Are you a magician? Because whenever I look at you, everyone else disappears.",
        "I'm not a photographer, but I can picture us together.",
        "Are you an electrician? Because you're lighting up my night.",
        "Do you have a Band-Aid? I scraped my knee falling for you.",
        "Is your name Google? Because you're everything I've been searching for.",
        "Are you a time traveler? Because I see you in my future.",
        "I was blinded by your beauty… I'm gonna need your name and number for insurance purposes.",
        "Are you a keyboard? Because you're just my type.",
        "Do you play soccer? Because you're a keeper.",
        "Are you the sun? Because I can't stare directly at you and function.",
        "If you were a vegetable, you'd be a cute-cumber.",
        "My doctor says I'm lacking vitamin U.",
        "Are you a campfire? Because you're hot and I want s'more.",
        "Is your heart a prison? Because I'm sentenced for life.",
        "Do you have an eraser? Because I can't get you out of my head.",
        "Are you a 90° angle? Because you're looking right."
    )

    val funFacts = listOf(
        "Octopuses have three hearts, and two of them stop beating when they swim.",
        "A day on Venus is longer than a year on Venus.",
        "Bananas are berries, but strawberries aren't.",
        "Wombat poop is cube-shaped.",
        "The Eiffel Tower can grow more than 6 inches taller in summer heat.",
        "Sharks existed before trees did.",
        "Honey found in ancient tombs is still edible after thousands of years.",
        "A group of flamingos is called a 'flamboyance'.",
        "Your brain uses about 20% of your body's total energy.",
        "There are more possible chess games than atoms in the observable universe.",
        "Sea otters hold hands while sleeping so they don't drift apart.",
        "The shortest war in history lasted 38 to 45 minutes.",
        "Scotland's national animal is the unicorn.",
        "Cows have best friends and get stressed when separated from them.",
        "The Moon has 'moonquakes' — some caused by Earth's gravity.",
        "Butterflies taste with their feet.",
        "Humans share about 60% of their DNA with bananas.",
        "The longest recorded flight of a chicken is 13 seconds.",
        "An astronaut's footprint on the Moon could last millions of years.",
        "Sloths can hold their breath longer than dolphins — up to 40 minutes.",
        "The inventor of the Pringles can is buried in one.",
        "Pineapples take about 2 years to grow a single fruit.",
        "A bolt of lightning is about 5 times hotter than the surface of the Sun.",
        "Rats laugh when you tickle them — but only at ultrasonic frequencies.",
        "The Great Wall of China is NOT visible from the Moon with the naked eye.",
        "Dolphins call each other by unique 'signature' names.",
        "Hot water can freeze faster than cold water — it's called the Mpemba effect.",
        "Ants never sleep in the way mammals do — they take micro-naps.",
        "There's enough gold in Earth's core to coat the planet in a knee-deep layer.",
        "Cats can't taste sweetness.",
        "A blue whale's heart is the size of a small car.",
        "Vending machines in Japan sell fresh eggs, flowers and even umbrellas.",
        "The '@' symbol has been around since at least 1536.",
        "Your stomach gets a new lining every few days — otherwise it would digest itself.",
        "Some turtles can breathe through their rear ends.",
        "The word 'night' appears in the Bible over 200 times, but 'butler' only twice.",
        "Bubble wrap was originally invented as wallpaper.",
        "A crocodile can't stick its tongue out.",
        "The average cloud weighs about 1.1 million pounds.",
        "Peanuts are an ingredient in dynamite."
    )

    val wyrPrompts = listOf(
        "Have unlimited money but no friends — or a tight friend group but you're always broke?",
        "Be able to fly but only 2 feet off the ground — or teleport but only once a day?",
        "Read minds but never be able to turn it off — or never be able to lie again?",
        "Live without music — or live without movies?",
        "Have a pause button for life — or a rewind button?",
        "Be famous for something embarrassing — or unknown for something amazing?",
        "Have perfect memory — or perfect sense of direction?",
        "Only be able to whisper — or only be able to shout?",
        "Find your soulmate tomorrow — or win the lottery next week?",
        "Have spaghetti hair — or meatball toes?",
        "Never charge your phone again — or never pay for gas/food delivery again?",
        "Be the funniest person in the room — or the smartest?",
        "Time travel to the past once — or see the future once?",
        "Have hiccups for a year — or feel like you need to sneeze for a year and never do?",
        "Swim with sharks — or skydive with no instructor?",
        "Be a genius nobody takes seriously — or be average but beloved?",
        "Lose all your photos — or all your messages?",
        "Live 100 years in the past — or 100 years in the future?",
        "Have a personal chef — or a personal driver?",
        "Always be 10 minutes late — or always be 20 minutes early?",
        "Talk to animals — or speak every human language fluently?",
        "Have unlimited free flights — or unlimited free food?",
        "Be able to erase one memory — or be able to re-live one day?",
        "Fight one horse-sized duck — or 100 duck-sized horses?",
        "Have a rewind for embarrassing moments — or a fast-forward for boring ones?",
        "Know when you'll die — or know how?",
        "Be invisible for a day — or be able to fly for a day?",
        "Only eat pizza forever — or only eat salad forever?",
        "Have your dream job with a bad boss — or an okay job with an amazing boss?",
        "Never wait in a line again — or never feel tired again?"
    )

    fun itemsFor(kind: CardDeckKind): List<String> = when (kind) {
        CardDeckKind.JOKES -> jokes
        CardDeckKind.LINES -> pickupLines
        CardDeckKind.FACTS -> funFacts
        CardDeckKind.WYR -> wyrPrompts
    }
}

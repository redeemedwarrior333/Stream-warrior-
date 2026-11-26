package com.streamwarrior.app

import com.streamwarrior.app.data.CardsRepository
import com.streamwarrior.app.data.CoachesRepository
import com.streamwarrior.app.data.GamesRepository
import com.streamwarrior.app.data.LawsRepository
import com.streamwarrior.app.data.ProphetRepository
import com.streamwarrior.app.data.TarotDeck
import com.streamwarrior.app.data.TarotRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class StreamWarriorLogicTest {

    @Test
    fun twentyQuestionsMatcherEvaluatesAttributesAndExactGuess() {
        val cat = GamesRepository.twentyQEntities.first { it.name == "a cat" }
        val aliveRes = GamesRepository.evaluateTwentyQ(cat, "Is it alive?")
        assertEquals("YES!", aliveRes.response)
        assertFalse(aliveRes.won)

        val wheelsRes = GamesRepository.evaluateTwentyQ(cat, "Does it have wheels?")
        assertEquals("NOPE!", wheelsRes.response)
        assertFalse(wheelsRes.won)

        val winRes = GamesRepository.evaluateTwentyQ(cat, "Is it a cat?")
        assertTrue(winRes.won)
    }

    @Test
    fun tarotDeckAndSpreadsAreComplete() {
        assertEquals(22, TarotRepository.cards.size)
        assertEquals(9, TarotRepository.spreads.size)
        val loveCross = TarotRepository.spreads.first { it.id == "lovecross" }
        assertEquals(listOf("You", "Them", "The Spark", "The Block"), loveCross.slots)

        val ppf = TarotRepository.spreads.first { it.id == "ppf" }
        val drawn = TarotRepository.cards.take(3).mapIndexed { i, c ->
            com.streamwarrior.app.data.DrawnTarotCard(c, reversed = false, slotLabel = ppf.slots[i], revealed = true)
        }
        val reading = TarotRepository.composeReading(drawn, TarotDeck.LOVE, ppf, "Does he like me?")
        assertTrue(reading.fullText.contains("Does he like me?"))
        assertTrue(reading.fullText.contains("The Fool"))
        assertTrue(reading.verdict.contains("The Verdict:"))
    }

    @Test
    fun weirdLawsAndQuizDataAreValid() {
        assertEquals(50, LawsRepository.stateLaws.size)
        assertTrue(LawsRepository.quizItems.size >= 50)
        assertTrue(CardsRepository.jokes.size >= 30)
        assertTrue(CardsRepository.funFacts.size >= 40)
    }

    @Test
    fun prophetDailyBlessingIsDeterministicPerDateAndAngelLookupWorks() {
        val date = LocalDate.of(2026, 11, 23)
        assertEquals(ProphetRepository.dailyBlessing(date), ProphetRepository.dailyBlessing(date))

        val angel777 = ProphetRepository.lookupAngelNumber("777")
        assertNotNull(angel777)
        assertEquals("Divine Yes", angel777?.title)

        val fallback = ProphetRepository.lookupAngelNumber("842")
        assertNotNull(fallback)
        assertEquals("842", fallback?.number)
    }

    @Test
    fun coachesCrisisGuardrailAndBiffDrafterTriggerProperly() {
        assertTrue(CoachesRepository.containsCrisisTrigger("I feel unsafe right now and want to hurt myself"))
        assertFalse(CoachesRepository.containsCrisisTrigger("How do I improve my stream hook?"))

        val threat = CoachesRepository.draftBiffReply("You'll regret this, watch your back")
        assertTrue(threat.isWarning)
        assertTrue(threat.reply.contains("911"))

        val guilt = CoachesRepository.draftBiffReply("You are so selfish after everything I did")
        assertFalse(guilt.isWarning)
        assertEquals("GUILT CAMPAIGN", guilt.tactic)
    }

    @Test
    fun voiceSentenceChunkingAndSpeechSanitizationWorkCorrectly() {
        val text = "Yo chat, listen up! That was an unreal clutch. What should we spin next?"
        val (chunks, offset) = com.streamwarrior.app.ui.VoiceAiViewModel.extractCompleteSentenceChunks(text, 0)
        assertEquals(3, chunks.size)
        assertEquals("Yo chat, listen up!", chunks[0])
        assertEquals("That was an unreal clutch.", chunks[1])
        assertEquals("What should we spin next?", chunks[2])
        assertEquals(text.length, offset)

        val cleaned = com.streamwarrior.app.voice.StreamVoiceEngine.sanitizeForSpeech("**Big news!** #hype `ready`")
        assertEquals("Big news! hype ready", cleaned)
        assertTrue(com.streamwarrior.app.voice.VoicePersonas.all.size >= 5)
    }

    @Test
    fun streamConnectPlatformUrlsAndOverlaySnapshotJsonWork() {
        val ids = com.streamwarrior.app.data.STREAM_PLATFORMS.map { it.id }
        assertTrue(ids.containsAll(listOf("tiktok", "twitch", "kick", "prism", "meetme")))

        val tiktok = com.streamwarrior.app.data.STREAM_PLATFORMS.first { it.id == "tiktok" }
        assertEquals("https://www.tiktok.com/@warrior/live", tiktok.buildChannelUrl("@warrior"))
        assertEquals("https://twitch.tv/live", tiktok.buildChannelUrl("https://twitch.tv/live"))

        val prism = com.streamwarrior.app.data.STREAM_PLATFORMS.first { it.id == "prism" }
        assertTrue(prism.supportsMobileWebWidget)
        assertTrue(prism.packageNames.contains("com.prism.live"))

        val meetme = com.streamwarrior.app.data.STREAM_PLATFORMS.first { it.id == "meetme" }
        assertTrue(meetme.packageNames.contains("com.myyearbook.m"))
        assertTrue(meetme.screenCastSteps.isNotBlank())

        val snapshot = com.streamwarrior.app.stream.LiveOverlaySnapshot(
            streamerName = "Redeemed Warrior",
            voicePersona = "Hype Co-Host",
            voiceAiCaption = "Welcome to the stream!"
        )
        val json = snapshot.toJsonString()
        assertTrue(json.contains("\"streamerName\":\"Redeemed Warrior\""))
        assertTrue(json.contains("\"voiceAiCaption\":\"Welcome to the stream!\""))
    }
}

package com.streamwarrior.app.ui

import com.streamwarrior.app.data.CardDeckKind
import kotlinx.serialization.Serializable

sealed interface AppRoute {
    @Serializable data object Home : AppRoute
    @Serializable data object StreamConnect : AppRoute
    @Serializable data object VoiceAi : AppRoute
    @Serializable data object Wheel : AppRoute
    @Serializable data object Tarot : AppRoute
    @Serializable data object TwentyQ : AppRoute
    @Serializable data object Trivia : AppRoute
    @Serializable data object Soundboard : AppRoute
    @Serializable data object MadLibs : AppRoute
    @Serializable data class CardDeck(val kind: CardDeckKind) : AppRoute
    @Serializable data object WeirdLaws : AppRoute
    @Serializable data object Prophet : AppRoute
    @Serializable data object Den : AppRoute
    @Serializable data object CoachesHub : AppRoute
    @Serializable data class CoachDetail(val coachId: String) : AppRoute
}

data class ModBarDestination(
    val id: String,
    val emoji: String,
    val label: String,
    val route: AppRoute
)

val MOD_BAR_ITEMS = listOf(
    ModBarDestination("home", "\uD83C\uDFE0", "Home", AppRoute.Home),
    ModBarDestination("connect", "\uD83D\uDCE1", "Go Live", AppRoute.StreamConnect),
    ModBarDestination("voice_ai", "\uD83C\uDF99\uFE0F", "Voice AI", AppRoute.VoiceAi),
    ModBarDestination("wheel", "\uD83C\uDFA1", "Chaos", AppRoute.Wheel),
    ModBarDestination("tarot", "\uD83D\uDD2E", "Tarot", AppRoute.Tarot),
    ModBarDestination("twentyq", "\u2753", "20 Q's", AppRoute.TwentyQ),
    ModBarDestination("trivia", "\uD83E\uDDE0", "Trivia", AppRoute.Trivia),
    ModBarDestination("soundboard", "\uD83D\uDD0A", "Sounds", AppRoute.Soundboard),
    ModBarDestination("madlibs", "\uD83D\uDCDD", "Mad Libs", AppRoute.MadLibs),
    ModBarDestination("jokes", "\uD83D\uDE02", "Jokes", AppRoute.CardDeck(CardDeckKind.JOKES)),
    ModBarDestination("lines", "\uD83D\uDC98", "Lines", AppRoute.CardDeck(CardDeckKind.LINES)),
    ModBarDestination("facts", "\uD83D\uDCA1", "Facts", AppRoute.CardDeck(CardDeckKind.FACTS)),
    ModBarDestination("wyr", "\u2694\uFE0F", "WYR", AppRoute.CardDeck(CardDeckKind.WYR)),
    ModBarDestination("laws", "\u2696\uFE0F", "Laws", AppRoute.WeirdLaws),
    ModBarDestination("prophet", "\uD83D\uDE4F", "Prophet", AppRoute.Prophet),
    ModBarDestination("den", "\uD83D\uDD34", "Den", AppRoute.Den),
    ModBarDestination("coaches", "\uD83D\uDD25", "Coaches", AppRoute.CoachesHub)
)

fun routeFromModuleId(id: String): AppRoute = when (id) {
    "connect" -> AppRoute.StreamConnect
    "voice_ai" -> AppRoute.VoiceAi
    "tarot" -> AppRoute.Tarot
    "twentyq" -> AppRoute.TwentyQ
    "trivia" -> AppRoute.Trivia
    "jokes" -> AppRoute.CardDeck(CardDeckKind.JOKES)
    "lines" -> AppRoute.CardDeck(CardDeckKind.LINES)
    "facts" -> AppRoute.CardDeck(CardDeckKind.FACTS)
    "madlibs" -> AppRoute.MadLibs
    "wyr" -> AppRoute.CardDeck(CardDeckKind.WYR)
    "soundboard" -> AppRoute.Soundboard
    "laws" -> AppRoute.WeirdLaws
    "prophet" -> AppRoute.Prophet
    "den" -> AppRoute.Den
    "coaches" -> AppRoute.CoachesHub
    else -> AppRoute.Home
}

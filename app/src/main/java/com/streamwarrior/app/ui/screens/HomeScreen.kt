package com.streamwarrior.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.R
import com.streamwarrior.app.data.CardDeckKind
import com.streamwarrior.app.ui.AppRoute
import com.streamwarrior.app.ui.theme.BgDeep
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.CrimsonAlert
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.TextSecondary
import com.streamwarrior.app.ui.theme.VioletPrimary

private data class HomeModuleCard(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accent: Color,
    val route: AppRoute
)

private val CORE_MODULES = listOf(
    HomeModuleCard("connect", "\uD83D\uDCE1", "Stream Connect", "1-tap OBS Wi-Fi overlay & live stage", Color(0xFF34D399), AppRoute.StreamConnect),
    HomeModuleCard("voice_ai", "\uD83C\uDF99\uFE0F", "Voice AI Co-Host", "Hands-free Gemini voice partner", Color(0xFF22D3EE), AppRoute.VoiceAi),
    HomeModuleCard("wheel", "\uD83C\uDFA1", "Wheel of Chaos", "Spin chat's next challenge", PinkAccent, AppRoute.Wheel),
    HomeModuleCard("tarot", "\uD83D\uDD2E", "Tarot & Mystica", "2 decks, 9 spreads & oracle", VioletPrimary, AppRoute.Tarot),
    HomeModuleCard("twentyq", "\u2753", "20 Questions", "40 secrets + AI host", GoldAccent, AppRoute.TwentyQ),
    HomeModuleCard("trivia", "\uD83E\uDDE0", "Trivia Showdown", "48 questions, 9 categories", Color(0xFF38BDF8), AppRoute.Trivia),
    HomeModuleCard("soundboard", "\uD83D\uDD0A", "Soundboard", "14 live SFX + custom clips", Color(0xFF34D399), AppRoute.Soundboard),
    HomeModuleCard("madlibs", "\uD83D\uDCDD", "Mad Libs", "8 hilarious stream stories", PinkAccent, AppRoute.MadLibs),
    HomeModuleCard("jokes", "\uD83D\uDE02", "Jokes Deck", "Quick punchlines for chat", GoldAccent, AppRoute.CardDeck(CardDeckKind.JOKES)),
    HomeModuleCard("lines", "\uD83D\uDC98", "Pickup Lines", "Cheesy chat icebreakers", PinkAccent, AppRoute.CardDeck(CardDeckKind.LINES)),
    HomeModuleCard("facts", "\uD83D\uDCA1", "Fun Facts", "40 mind-bending facts", Color(0xFF38BDF8), AppRoute.CardDeck(CardDeckKind.FACTS)),
    HomeModuleCard("wyr", "\u2694\uFE0F", "Would You Rather", "30 live audience debates", VioletPrimary, AppRoute.CardDeck(CardDeckKind.WYR)),
    HomeModuleCard("laws", "\u2696\uFE0F", "Weird Laws", "50 states + Real or Fake quiz", GoldAccent, AppRoute.WeirdLaws)
)

private val FAMILY_MODULES = listOf(
    HomeModuleCard("prophet", "\uD83D\uDE4F", "Warrior Prophet", "Daily blessing, 3 decks & Angel Numbers", GoldAccent, AppRoute.Prophet),
    HomeModuleCard("den", "\uD83D\uDD34", "Warrior's Den", "Live alert box, ticker preview & tip links", CrimsonAlert, AppRoute.Den),
    HomeModuleCard("coaches", "\uD83D\uDD25", "Warrior Coaches & Forge", "15 AI coaches, BIFF drafter & tool builder", PinkAccent, AppRoute.CoachesHub)
)

@Composable
fun HomeScreen(
    streamerName: String,
    isWideLayout: Boolean,
    onNavigate: (AppRoute) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (isWideLayout) 3 else 2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 900.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeroBannerCard(
                    streamerName = streamerName,
                    onConnectLive = { onNavigate(AppRoute.StreamConnect) },
                    onOpenVoiceAi = { onNavigate(AppRoute.VoiceAi) },
                    onSpinWheel = { onNavigate(AppRoute.Wheel) },
                    onOpenTarot = { onNavigate(AppRoute.Tarot) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "STREAM MODULES",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            items(CORE_MODULES, key = { it.id }) { mod ->
                ModuleTileCard(
                    card = mod,
                    onClick = { onNavigate(mod.route) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "WARRIOR FAMILY SUITE",
                    style = MaterialTheme.typography.labelLarge,
                    color = GoldAccent,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            items(FAMILY_MODULES, key = { it.id }) { mod ->
                ModuleTileCard(
                    card = mod,
                    onClick = { onNavigate(mod.route) }
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HeroBannerCard(
    streamerName: String,
    onConnectLive: () -> Unit,
    onOpenVoiceAi: () -> Unit,
    onSpinWheel: () -> Unit,
    onOpenTarot: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_cosmos),
                contentDescription = stringResource(R.string.cd_hero_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(22.dp))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                BgDeep.copy(alpha = 0.45f),
                                BgDeep.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = VioletPrimary.copy(alpha = 0.85f)
                ) {
                    Text(
                        text = "\uD83D\uDD34 LIVE STAGE • $streamerName",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Keep Chat Locked In",
                    style = MaterialTheme.typography.displaySmall,
                    color = GoldAccent
                )
                Text(
                    text = "Tap any segment for hands-free Voice AI co-hosting, interactive games, sound drops, tarot spreads, and stream tools.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onConnectLive,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                        modifier = Modifier.testTag("hero_connect_live_button")
                    ) {
                        Text("\uD83D\uDCE1 Go Live", fontWeight = FontWeight.Bold)
                    }
                    Surface(
                        onClick = onOpenVoiceAi,
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.7f)),
                        modifier = Modifier.testTag("hero_voice_ai_button")
                    ) {
                        Text(
                            text = "\uD83C\uDF99\uFE0F Voice AI",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF22D3EE),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                    Surface(
                        onClick = onSpinWheel,
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, PinkAccent.copy(alpha = 0.7f)),
                        modifier = Modifier.testTag("hero_spin_wheel_button")
                    ) {
                        Text(
                            text = "\uD83C\uDFA1 Spin Chaos",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                    Surface(
                        onClick = onOpenTarot,
                        shape = RoundedCornerShape(12.dp),
                        color = CardSurfaceElevated.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.7f)),
                        modifier = Modifier.testTag("hero_tarot_button")
                    ) {
                        Text(
                            text = "\uD83D\uDD2E Pull Tarot",
                            style = MaterialTheme.typography.labelLarge,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleTileCard(
    card: HomeModuleCard,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = CardSurface,
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_card_${card.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(card.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = card.emoji, fontSize = 20.sp)
            }
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

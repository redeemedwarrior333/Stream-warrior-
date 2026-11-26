package com.streamwarrior.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.R
import com.streamwarrior.app.ui.MOD_BAR_ITEMS
import com.streamwarrior.app.ui.ModBarDestination
import com.streamwarrior.app.ui.theme.BgDeep
import com.streamwarrior.app.ui.theme.BgLayer
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.EmeraldGood
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletPrimary

@Composable
fun StreamShell(
    currentModId: String,
    isAiConnected: Boolean,
    isOverlayLive: Boolean,
    barsHidden: Boolean,
    isWideLayout: Boolean,
    onSelectMod: (ModBarDestination) -> Unit,
    onOpenStreamConnect: () -> Unit,
    onToggleBarsHidden: (Boolean) -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgDeep, BgLayer, BgDeep)
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                AnimatedVisibility(
                    visible = !barsHidden,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it }
                ) {
                    StreamTopBar(
                        isAiConnected = isAiConnected,
                        isOverlayLive = isOverlayLive,
                        onBrandClick = { onSelectMod(MOD_BAR_ITEMS.first()) },
                        onOpenStreamConnect = onOpenStreamConnect,
                        onOpenHelp = onOpenHelp,
                        onOpenSettings = onOpenSettings
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = !barsHidden && !isWideLayout,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    BottomModBar(
                        currentModId = currentModId,
                        onSelectMod = onSelectMod,
                        onHideBars = { onToggleBarsHidden(true) }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!barsHidden && isWideLayout) {
                    SideModRail(
                        currentModId = currentModId,
                        onSelectMod = onSelectMod,
                        onHideBars = { onToggleBarsHidden(true) }
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    content()
                }
            }
        }

        // Clean Scene Mode restore pill (always accessible when bars are hidden)
        AnimatedVisibility(
            visible = barsHidden,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 8.dp),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                onClick = { onToggleBarsHidden(false) },
                shape = CircleShape,
                color = CardSurface.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f)),
                modifier = Modifier.testTag("restore_bars_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = stringResource(R.string.show_bars_hint),
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.show_bars_hint),
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamTopBar(
    isAiConnected: Boolean,
    isOverlayLive: Boolean,
    onBrandClick: () -> Unit,
    onOpenStreamConnect: () -> Unit,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        color = CardSurface.copy(alpha = 0.94f),
        border = BorderStroke(0.5.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onBrandClick)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("top_brand_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(VioletPrimary, PinkAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "\uD83D\uDEE1\uFE0F", fontSize = 18.sp)
                }
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isAiConnected) EmeraldGood else GoldAccent)
                        )
                        Text(
                            text = if (isAiConnected) {
                                stringResource(R.string.ai_status_live)
                            } else {
                                stringResource(R.string.ai_status_builtin)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = onOpenStreamConnect,
                    shape = CircleShape,
                    color = if (isOverlayLive) EmeraldGood.copy(alpha = 0.18f) else CardSurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isOverlayLive) EmeraldGood.copy(alpha = 0.7f) else GoldAccent.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier.testTag("top_connect_stream_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isOverlayLive) EmeraldGood else GoldAccent)
                        )
                        Text(
                            text = if (isOverlayLive) "Live Link" else "Go Live",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isOverlayLive) EmeraldGood else GoldAccent
                        )
                    }
                }
                IconButton(
                    onClick = onOpenHelp,
                    modifier = Modifier.testTag("top_help_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = stringResource(R.string.cd_help_button),
                        tint = GoldAccent
                    )
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.cd_settings_button),
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomModBar(
    currentModId: String,
    onSelectMod: (ModBarDestination) -> Unit,
    onHideBars: () -> Unit
) {
    Surface(
        color = CardSurface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MOD_BAR_ITEMS.forEach { item ->
                val selected = item.id == currentModId
                ModChip(
                    emoji = item.emoji,
                    label = item.label,
                    selected = selected,
                    onClick = { onSelectMod(item) },
                    modifier = Modifier.testTag("mod_bar_${item.id}")
                )
            }
            ModChip(
                emoji = "\uD83D\uDC41\uFE0F",
                label = stringResource(R.string.hide_bars_label),
                selected = false,
                onClick = onHideBars,
                modifier = Modifier.testTag("mod_bar_hide")
            )
        }
    }
}

@Composable
private fun SideModRail(
    currentModId: String,
    onSelectMod: (ModBarDestination) -> Unit,
    onHideBars: () -> Unit
) {
    Surface(
        color = CardSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier
            .fillMaxHeight()
            .width(104.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MOD_BAR_ITEMS.forEach { item ->
                ModChip(
                    emoji = item.emoji,
                    label = item.label,
                    selected = item.id == currentModId,
                    onClick = { onSelectMod(item) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rail_mod_${item.id}")
                )
            }
            Surface(
                onClick = onHideBars,
                shape = RoundedCornerShape(12.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rail_mod_hide")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = stringResource(R.string.hide_bars_label),
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.hide_bars_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun ModChip(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) VioletPrimary else CardSurfaceElevated
    val borderColor = if (selected) GoldAccent else CardBorder
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) TextPrimary else TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

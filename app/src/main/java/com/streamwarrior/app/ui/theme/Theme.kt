package com.streamwarrior.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkCosmicColorScheme = darkColorScheme(
    primary = VioletPrimary,
    onPrimary = TextPrimary,
    primaryContainer = VioletDeep,
    onPrimaryContainer = TextPrimary,
    secondary = GoldAccent,
    onSecondary = GoldInk,
    secondaryContainer = CardSurfaceElevated,
    onSecondaryContainer = GoldAccent,
    tertiary = PinkAccent,
    onTertiary = TextPrimary,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = CardSurface,
    onSurface = TextPrimary,
    surfaceVariant = CardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = CrimsonAlert,
    onError = TextPrimary
)

val StreamWarriorShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun StreamWarriorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkCosmicColorScheme,
        typography = StreamWarriorTypography,
        shapes = StreamWarriorShapes,
        content = content
    )
}

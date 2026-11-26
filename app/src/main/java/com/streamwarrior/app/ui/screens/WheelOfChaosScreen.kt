package com.streamwarrior.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streamwarrior.app.audio.SoundSynth
import com.streamwarrior.app.data.ChaosWheelSegment
import com.streamwarrior.app.data.GamesRepository
import com.streamwarrior.app.ui.AppRoute
import com.streamwarrior.app.ui.routeFromModuleId
import com.streamwarrior.app.ui.theme.CardBorder
import com.streamwarrior.app.ui.theme.CardSurface
import com.streamwarrior.app.ui.theme.CardSurfaceElevated
import com.streamwarrior.app.ui.theme.GoldAccent
import com.streamwarrior.app.ui.theme.GoldInk
import com.streamwarrior.app.ui.theme.PinkAccent
import com.streamwarrior.app.ui.theme.TextMuted
import com.streamwarrior.app.ui.theme.TextPrimary
import com.streamwarrior.app.ui.theme.VioletDeep
import com.streamwarrior.app.ui.theme.VioletPrimary
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val WHEEL_COLORS = listOf(
    Color(0xFF7C3AED),
    Color(0xFFEC4899),
    Color(0xFF0EA5E9),
    Color(0xFFF59E0B),
    Color(0xFF10B981),
    Color(0xFF6366F1),
    Color(0xFFF43F5E),
    Color(0xFF14B8A6),
    Color(0xFF8B5CF6),
    Color(0xFFEAB308)
)

@Composable
fun WheelOfChaosScreen(
    onLaunchModule: (AppRoute) -> Unit
) {
    val segments = GamesRepository.wheelSegments
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var landed by remember { mutableStateOf<ChaosWheelSegment?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.widthIn(max = 560.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "\uD83C\uDFA1 Wheel of Chaos",
                style = MaterialTheme.typography.headlineMedium,
                color = GoldAccent
            )
            Text(
                text = "Let fate pick what happens next on your live stream.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(1f)
                    .semantics { contentDescription = "Spinning Wheel of Chaos" },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension * 0.92f
                    val radius = diameter / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val topLeft = Offset(center.x - radius, center.y - radius)
                    val sweep = 360f / segments.size

                    // Outer glow ring
                    drawCircle(
                        color = GoldAccent.copy(alpha = 0.25f),
                        radius = radius + 12f,
                        center = center
                    )

                    rotate(degrees = rotation.value, pivot = center) {
                        segments.forEachIndexed { idx, seg ->
                            val startAngle = idx * sweep - 90f
                            drawArc(
                                color = WHEEL_COLORS[idx % WHEEL_COLORS.size],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = topLeft,
                                size = Size(diameter, diameter)
                            )
                            drawArc(
                                color = Color(0xFF0B0B14),
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = topLeft,
                                size = Size(diameter, diameter),
                                style = Stroke(width = 3f)
                            )

                            val midDeg = startAngle + sweep / 2f
                            val midRad = Math.toRadians(midDeg.toDouble())
                            val emojiRadius = radius * 0.68f
                            val ex = center.x + (emojiRadius * cos(midRad)).toFloat()
                            val ey = center.y + (emojiRadius * sin(midRad)).toFloat()
                            val measured = textMeasurer.measure(
                                text = seg.emoji,
                                style = TextStyle(fontSize = 20.sp)
                            )
                            drawText(
                                textLayoutResult = measured,
                                topLeft = Offset(
                                    ex - measured.size.width / 2f,
                                    ey - measured.size.height / 2f
                                )
                            )
                        }
                    }

                    // Gold rim
                    drawCircle(
                        color = GoldAccent,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 6f)
                    )

                    // Center hub
                    drawCircle(color = CardSurface, radius = radius * 0.20f, center = center)
                    drawCircle(color = GoldAccent, radius = radius * 0.20f, center = center, style = Stroke(width = 5f))

                    // Top pointer triangle
                    val pointer = Path().apply {
                        moveTo(center.x, center.y - radius + 16f)
                        lineTo(center.x - 18f, center.y - radius - 14f)
                        lineTo(center.x + 18f, center.y - radius - 14f)
                        close()
                    }
                    drawPath(pointer, color = GoldAccent)
                }
            }

            Button(
                onClick = {
                    if (isSpinning) return@Button
                    isSpinning = true
                    landed = null
                    SoundSynth.play("drumroll")
                    scope.launch {
                        val extraTurns = 1440f + Random.nextFloat() * 720f
                        val target = rotation.value + extraTurns
                        rotation.animateTo(
                            targetValue = target,
                            animationSpec = tween(
                                durationMillis = 3200,
                                easing = CubicBezierEasing(0.12f, 0.85f, 0.2f, 1f)
                            )
                        )
                        val sweep = 360f / segments.size
                        // Pointer is at -90 deg (top), segments start at -90 deg + rotation
                        val norm = ((360f - (rotation.value % 360f)) + 360f) % 360f
                        val index = ((norm / sweep).toInt()) % segments.size
                        landed = segments[index]
                        isSpinning = false
                        SoundSynth.play("tada")
                    }
                },
                enabled = !isSpinning,
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = GoldInk),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("wheel_spin_button")
            ) {
                Text(
                    text = if (isSpinning) "Spinning…" else "\uD83C\uDFA1 SPIN THE WHEEL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            landed?.let { result ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardSurface,
                    border = BorderStroke(1.5.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${result.emoji} Landed on ${result.label}!",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldAccent
                        )
                        Button(
                            onClick = { onLaunchModule(routeFromModuleId(result.routeId)) },
                            colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wheel_launch_result_button")
                        ) {
                            Text("Launch ${result.label}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Legend grid
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardSurfaceElevated,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Wheel Slices", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    segments.chunked(2).forEach { pair ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { seg ->
                                Surface(
                                    onClick = { onLaunchModule(routeFromModuleId(seg.routeId)) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = CardSurface,
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("wheel_slice_${seg.routeId}")
                                ) {
                                    Text(
                                        text = "${seg.emoji} ${seg.label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

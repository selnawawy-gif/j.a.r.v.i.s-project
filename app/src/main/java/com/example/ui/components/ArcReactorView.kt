package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StarkArcBlue
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkCoreGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorView(
    modifier: Modifier = Modifier,
    size: Dp = 240.dp,
    rmsAudioLevel: Float = 0f,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorTransition")

    // Slow clockwise rotation for outer energy coils
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    // Faster counter-clockwise rotation for inner turbine blades
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    // Core energetic breathing pulse
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingPulse"
    )

    // Dynamic scale combining breathing + live audio / speaking excitement
    val dynamicAudioBoost = if (isSpeaking) 0.15f else (rmsAudioLevel * 0.25f)
    val totalScale = (breathingPulse + dynamicAudioBoost).coerceIn(0.9f, 1.35f)

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = size / 2),
                onClick = onClick
            )
            .testTag("arc_reactor_core"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (this.size.minDimension / 2f) * 0.88f
            val currentRadius = baseRadius * totalScale

            // 1. Ambient Holographic Core Glow
            val glowColor = when {
                isSpeaking -> StarkCoreGold
                isListening -> StarkArcCyan
                else -> StarkArcBlue
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = 0.45f),
                        StarkArcCyan.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.25f
                ),
                radius = currentRadius * 1.25f,
                center = center
            )

            // 2. Outermost Reticle Ring with hash marks
            drawCircle(
                color = StarkArcCyan.copy(alpha = 0.4f),
                radius = currentRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Outer tick marks
            val tickCount = 36
            for (i in 0 until tickCount) {
                val angle = Math.toRadians((i * (360.0 / tickCount)))
                val isMajor = i % 3 == 0
                val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val startX = (center.x + (currentRadius - tickLen) * cos(angle)).toFloat()
                val startY = (center.y + (currentRadius - tickLen) * sin(angle)).toFloat()
                val endX = (center.x + currentRadius * cos(angle)).toFloat()
                val endY = (center.y + currentRadius * sin(angle)).toFloat()

                drawLine(
                    color = if (isMajor) StarkArcCyan.copy(alpha = 0.85f) else StarkArcCyan.copy(alpha = 0.35f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx()
                )
            }

            // 3. Rotating 10 Magnetic Energy Coils (Outer Ring)
            rotate(degrees = outerRotation, pivot = center) {
                val coilCount = 10
                val coilRadius = currentRadius * 0.78f
                val coilArcSpan = 22f

                for (i in 0 until coilCount) {
                    val startAngle = i * 36f + 7f
                    drawArc(
                        color = StarkArcCyan,
                        startAngle = startAngle,
                        sweepAngle = coilArcSpan,
                        useCenter = false,
                        topLeft = Offset(center.x - coilRadius, center.y - coilRadius),
                        size = Size(coilRadius * 2, coilRadius * 2),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Inner golden core filament in each coil
                    drawArc(
                        color = StarkCoreGold,
                        startAngle = startAngle + 4f,
                        sweepAngle = coilArcSpan - 8f,
                        useCenter = false,
                        topLeft = Offset(center.x - coilRadius, center.y - coilRadius),
                        size = Size(coilRadius * 2, coilRadius * 2),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Middle Concentric HUD Rings
            drawCircle(
                color = StarkArcCyan.copy(alpha = 0.55f),
                radius = currentRadius * 0.58f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 5. Rotating Turbine Blades (Counter-clockwise)
            rotate(degrees = innerRotation, pivot = center) {
                val bladeCount = 8
                val bladeInnerRadius = currentRadius * 0.34f
                val bladeOuterRadius = currentRadius * 0.54f

                for (b in 0 until bladeCount) {
                    val angle = Math.toRadians((b * (360.0 / bladeCount)))
                    val p1X = (center.x + bladeInnerRadius * cos(angle - 0.15)).toFloat()
                    val p1Y = (center.y + bladeInnerRadius * sin(angle - 0.15)).toFloat()
                    val p2X = (center.x + bladeOuterRadius * cos(angle)).toFloat()
                    val p2Y = (center.y + bladeOuterRadius * sin(angle)).toFloat()
                    val p3X = (center.x + bladeInnerRadius * cos(angle + 0.15)).toFloat()
                    val p3Y = (center.y + bladeInnerRadius * sin(angle + 0.15)).toFloat()

                    val bladePath = Path().apply {
                        moveTo(p1X, p1Y)
                        lineTo(p2X, p2Y)
                        lineTo(p3X, p3Y)
                        close()
                    }

                    drawPath(
                        path = bladePath,
                        brush = Brush.linearGradient(
                            colors = listOf(StarkArcCyan.copy(alpha = 0.9f), StarkArcBlue.copy(alpha = 0.4f)),
                            start = Offset(p1X, p1Y),
                            end = Offset(p2X, p2Y)
                        )
                    )
                }
            }

            // 6. Central Arc Reactor Core (Luminescent Palladium / New Element Core)
            val coreRadius = currentRadius * 0.28f

            // Core boundary ring
            drawCircle(
                color = StarkArcCyan,
                radius = coreRadius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            // High-intensity luminous plasma fill
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        StarkArcCyan,
                        StarkArcBlue,
                        StarkArcCyan.copy(alpha = 0.3f)
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius * 0.95f,
                center = center
            )

            // Center triangular element aperture
            val triRadius = coreRadius * 0.55f
            val triPath = Path().apply {
                val topAngle = -Math.PI / 2
                val rightAngle = topAngle + (2 * Math.PI / 3)
                val leftAngle = topAngle + (4 * Math.PI / 3)

                moveTo((center.x + triRadius * cos(topAngle)).toFloat(), (center.y + triRadius * sin(topAngle)).toFloat())
                lineTo((center.x + triRadius * cos(rightAngle)).toFloat(), (center.y + triRadius * sin(rightAngle)).toFloat())
                lineTo((center.x + triRadius * cos(leftAngle)).toFloat(), (center.y + triRadius * sin(leftAngle)).toFloat())
                close()
            }

            drawPath(
                path = triPath,
                color = Color.White.copy(alpha = 0.95f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

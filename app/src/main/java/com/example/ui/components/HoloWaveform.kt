package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkCoreGold
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun HoloWaveform(
    modifier: Modifier = Modifier,
    rmsAudioLevel: Float = 0f,
    isSpeaking: Boolean = false,
    isListening: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformTransition")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val barCount = 28
        val spacing = 4.dp.toPx()
        val totalSpacing = spacing * (barCount - 1)
        val availableWidth = size.width - totalSpacing
        val barWidth = (availableWidth / barCount).coerceAtLeast(2f)
        val midY = size.height / 2f

        for (i in 0 until barCount) {
            val normalizedIdx = (i - barCount / 2f) / (barCount / 2f)
            val bellCurve = (1f - abs(normalizedIdx * 0.7f)).coerceIn(0.2f, 1f)

            // Sine wave modulation
            val wave = abs(sin(phase + (i * 0.35f)))

            val baseHeight = when {
                isSpeaking -> 14.dp.toPx() + (wave * 26.dp.toPx() * bellCurve)
                isListening -> 8.dp.toPx() + (rmsAudioLevel * 32.dp.toPx() * bellCurve) + (wave * 6.dp.toPx())
                else -> 4.dp.toPx() + (wave * 4.dp.toPx() * bellCurve)
            }

            val finalHeight = baseHeight.coerceIn(4.dp.toPx(), size.height)
            val x = i * (barWidth + spacing)
            val y = midY - (finalHeight / 2f)

            val barColor = when {
                isSpeaking -> StarkCoreGold
                isListening -> StarkArcCyan
                else -> StarkArcCyan.copy(alpha = 0.4f)
            }

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        barColor,
                        barColor.copy(alpha = 0.5f)
                    ),
                    startY = y,
                    endY = y + finalHeight
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, finalHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkBorder
import com.example.ui.theme.StarkCoreGold
import com.example.ui.theme.StarkSuccessGreen
import com.example.ui.theme.StarkSurface
import com.example.ui.theme.StarkTextPrimary
import com.example.ui.theme.StarkTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StarkHudHeader(
    modifier: Modifier = Modifier,
    batteryPercent: Int = 98,
    isListening: Boolean = true,
    isSpeaking: Boolean = false,
    isForeground: Boolean = true
) {
    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
        while (true) {
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "RadarPulse")
    val radarAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(StarkSurface.copy(alpha = 0.88f))
            .border(1.dp, StarkBorder, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top row: Brand & Telemetry
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = StarkArcCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STARK INDUSTRIES",
                    color = StarkArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = " // MARK LXXXV",
                    color = StarkTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Real battery & Clock
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery Level",
                    tint = if (batteryPercent > 20) StarkArcCyan else StarkCoreGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$batteryPercent%",
                    color = StarkTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = currentTimeString,
                    color = StarkTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.padding(top = 6.dp))

        // Status pill row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !isForeground -> Color.Gray
                                isSpeaking -> StarkCoreGold
                                isListening -> StarkSuccessGreen
                                else -> StarkArcCyan
                            }
                        )
                        .alpha(if (isForeground) radarAlpha else 0.4f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when {
                        !isForeground -> "STANDBY // BACKGROUND PAUSED"
                        isSpeaking -> "VOCALIZING RESPONSE..."
                        isListening -> "MONITORING: 'HEY JARVIS'"
                        else -> "SYSTEM IDLE"
                    },
                    color = when {
                        !isForeground -> StarkTextSecondary
                        isSpeaking -> StarkCoreGold
                        isListening -> StarkSuccessGreen
                        else -> StarkArcCyan
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            // Foreground security tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(StarkBorder.copy(alpha = 0.5f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = StarkArcCyan,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "FOREGROUND ONLY",
                    color = StarkArcCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

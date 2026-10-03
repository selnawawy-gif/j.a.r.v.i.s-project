package com.example.ui.screens

import android.speech.tts.Voice
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkBorder
import com.example.ui.theme.StarkBorderBright
import com.example.ui.theme.StarkCoreGold
import com.example.ui.theme.StarkDarkBg
import com.example.ui.theme.StarkSuccessGreen
import com.example.ui.theme.StarkSurface
import com.example.ui.theme.StarkSurfaceElevated
import com.example.ui.theme.StarkSurfaceVariant
import com.example.ui.theme.StarkTextPrimary
import com.example.ui.theme.StarkTextSecondary

@Composable
fun VoiceSettingsScreen(
    voicePitch: Float,
    voiceSpeed: Float,
    selectedVoiceName: String,
    availableVoices: List<Voice>,
    isWakeWordEnabled: Boolean,
    isForeground: Boolean,
    hasAudioPermission: Boolean,
    hasCalendarPermission: Boolean,
    onPitchChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onSelectVoice: (String) -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onTestVoice: (String) -> Unit,
    onRequestAudioPermission: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVoiceSelector by remember { mutableStateOf(false) }

    val quotes = listOf(
        "At your service, sir. All core systems are functioning within normal parameters.",
        "Mark 85 armor is armed and calibrated. Shall I engage repulsor flight systems, sir?",
        "Good day, sir. Your schedule and protocols are synchronized and ready for inspection.",
        "As always, an absolute pleasure serving you, Mr. Stark."
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StarkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Header
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "JARVIS VOICE CALIBRATION",
                    color = StarkArcCyan,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "PAUL BETTANY ACOUSTIC SYNTHESIS PROTOCOL",
                    color = StarkTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2. Foreground-Only Enforcement Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("foreground_policy_card"),
                colors = CardDefaults.cardColors(containerColor = StarkSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(StarkArcCyan, StarkBorder))
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StarkSurfaceVariant)
                            .border(1.dp, StarkSuccessGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = StarkSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "FOREGROUND ONLY PROTOCOL",
                                color = StarkSuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StarkSuccessGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ENFORCED",
                                    color = StarkSuccessGreen,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Jarvis voice listening and audio responses run strictly while the app is in the foreground. No background audio capture.",
                            color = StarkTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // 3. Paul Bettany Voice Tuning Sliders
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("voice_tuning_card"),
                colors = CardDefaults.cardColors(containerColor = StarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(StarkBorder, StarkBorderBright))
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = StarkArcCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACOUSTIC CADENCE & PITCH",
                                color = StarkArcCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Button(
                            onClick = {
                                onPitchChange(0.92f)
                                onSpeedChange(1.00f)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StarkSurfaceVariant, contentColor = StarkArcCyan),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Reset", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Pitch Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Voice Pitch (Resonance)",
                            color = StarkTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format("%.2fx", voicePitch),
                            color = StarkArcCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = voicePitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.70f..1.30f,
                        colors = SliderDefaults.colors(
                            thumbColor = StarkArcCyan,
                            activeTrackColor = StarkArcCyan,
                            inactiveTrackColor = StarkBorder
                        ),
                        modifier = Modifier.testTag("pitch_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Speech Rate Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Speech Rate (Pacing)",
                            color = StarkTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = String.format("%.2fx", voiceSpeed),
                            color = StarkCoreGold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Slider(
                        value = voiceSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.70f..1.30f,
                        colors = SliderDefaults.colors(
                            thumbColor = StarkCoreGold,
                            activeTrackColor = StarkCoreGold,
                            inactiveTrackColor = StarkBorder
                        ),
                        modifier = Modifier.testTag("speed_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Voice engine selection info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(StarkSurfaceVariant)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE TTS PROFILE",
                                color = StarkTextSecondary,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (selectedVoiceName.isNotEmpty()) selectedVoiceName else "British English (UK Bettany)",
                                color = StarkTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 4. Test Voice Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "TEST JARVIS PHRASEOLOGY",
                    color = StarkArcCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                quotes.forEachIndexed { idx, quote ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onTestVoice(quote) }
                            .testTag("test_quote_$idx"),
                        colors = CardDefaults.cardColors(containerColor = StarkSurface),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(StarkBorder, StarkBorderBright))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play Quote",
                                tint = StarkArcCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "\"$quote\"",
                                color = StarkTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // 5. Wake Word & Permissions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = StarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(StarkBorder, StarkBorderBright))
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Wake Word Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Wake Word: 'Hey Jarvis'",
                                color = StarkTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Continuous listening while in foreground",
                                color = StarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = isWakeWordEnabled,
                            onCheckedChange = onToggleWakeWord,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StarkDarkBg,
                                checkedTrackColor = StarkArcCyan,
                                uncheckedThumbColor = StarkTextSecondary,
                                uncheckedTrackColor = StarkSurfaceVariant
                            ),
                            modifier = Modifier.testTag("wake_word_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio Permission status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (hasAudioPermission) StarkSuccessGreen else StarkCoreGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Microphone Permission",
                                color = StarkTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                        if (hasAudioPermission) {
                            Text(
                                text = "CONNECTED",
                                color = StarkSuccessGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Button(
                                onClick = onRequestAudioPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = StarkCoreGold, contentColor = StarkDarkBg),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Grant", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Permission status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = if (hasCalendarPermission) StarkSuccessGreen else StarkCoreGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Device Calendar Permission",
                                color = StarkTextPrimary,
                                fontSize = 12.sp
                            )
                        }
                        if (hasCalendarPermission) {
                            Text(
                                text = "CONNECTED",
                                color = StarkSuccessGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Button(
                                onClick = onRequestCalendarPermission,
                                colors = ButtonDefaults.buttonColors(containerColor = StarkCoreGold, contentColor = StarkDarkBg),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Grant", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

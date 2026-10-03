package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.JarvisInteraction
import com.example.ui.components.ArcReactorView
import com.example.ui.components.HoloWaveform
import com.example.ui.theme.StarkArcBlue
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkBorder
import com.example.ui.theme.StarkBorderBright
import com.example.ui.theme.StarkCoreGold
import com.example.ui.theme.StarkDarkBg
import com.example.ui.theme.StarkSurface
import com.example.ui.theme.StarkSurfaceElevated
import com.example.ui.theme.StarkSurfaceVariant
import com.example.ui.theme.StarkTextPrimary
import com.example.ui.theme.StarkTextSecondary
import com.example.voice.AssistantAudioState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    audioState: AssistantAudioState,
    rmsAudioLevel: Float,
    isSpeaking: Boolean,
    currentSpokenText: String,
    liveTranscript: String,
    isWakeWordEnabled: Boolean,
    interactions: List<JarvisInteraction>,
    onArcReactorClick: () -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onQuickCommand: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onReplayAudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.US)

    val quickCommands = listOf(
        "Summarize my day",
        "What's on my schedule?",
        "Schedule meeting at 4 PM",
        "Add task: Review Mark 85",
        "Diagnostics",
        "Open Settings",
        "Who are you?"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(StarkDarkBg, Color(0xFF09101C), StarkDarkBg)
                )
            )
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Central Arc Reactor Core
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ARC REACTOR CORE // MARK LXXXV",
                    color = StarkArcCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = if (isSpeaking) "VOCALIZING RESPONSE..." else if (audioState == AssistantAudioState.LISTENING_FOR_COMMAND) "LISTENING TO INSTRUCTION..." else "TAP REACTOR TO ENGAGE VOCAL LINK",
                    color = if (isSpeaking) StarkCoreGold else StarkTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                ArcReactorView(
                    size = 200.dp,
                    rmsAudioLevel = rmsAudioLevel,
                    isSpeaking = isSpeaking,
                    isListening = audioState == AssistantAudioState.LISTENING_FOR_WAKE_WORD || audioState == AssistantAudioState.LISTENING_FOR_COMMAND,
                    onClick = onArcReactorClick
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Holographic Waveform
                HoloWaveform(
                    rmsAudioLevel = rmsAudioLevel,
                    isSpeaking = isSpeaking,
                    isListening = audioState == AssistantAudioState.LISTENING_FOR_WAKE_WORD || audioState == AssistantAudioState.LISTENING_FOR_COMMAND
                )

                // Wake word toggle badge
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(StarkSurfaceVariant)
                        .border(1.dp, StarkBorder, RoundedCornerShape(20.dp))
                        .clickable { onToggleWakeWord(!isWakeWordEnabled) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("wake_word_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isWakeWordEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Toggle Wake Word",
                        tint = if (isWakeWordEnabled) StarkArcCyan else StarkTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWakeWordEnabled) "Wake Word: 'Hey Jarvis' ACTIVE" else "Wake Word: PAUSED (Tap to Enable)",
                        color = if (isWakeWordEnabled) StarkTextPrimary else StarkTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. Currently Speaking / Live Transcript Card
        item {
            AnimatedVisibility(
                visible = isSpeaking || liveTranscript.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("live_speech_card"),
                    colors = CardDefaults.cardColors(containerColor = StarkSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarkArcCyan, StarkCoreGold))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .background(if (isSpeaking) StarkCoreGold else StarkArcCyan)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSpeaking) "// VOCAL FEED: J.A.R.V.I.S." else "// LIVE INPUT AUDIO",
                                    color = if (isSpeaking) StarkCoreGold else StarkArcCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isSpeaking) {
                                IconButton(
                                    onClick = onStopSpeaking,
                                    modifier = Modifier.size(28.dp).testTag("stop_speaking_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Silence Jarvis",
                                        tint = StarkCoreGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isSpeaking) currentSpokenText else liveTranscript,
                            color = StarkTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 3. Quick Action Holographic Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "DIRECT PROTOCOLS",
                    color = StarkArcCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickCommands) { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(StarkSurfaceVariant)
                                .border(1.dp, StarkBorderBright, RoundedCornerShape(8.dp))
                                .clickable { onQuickCommand(cmd) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("quick_cmd_${cmd.take(8).lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = cmd,
                                color = StarkTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 4. Holographic Interaction Log Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSMISSION FEED // REAL-TIME RESPONSES",
                    color = StarkArcCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${interactions.size} LOGS",
                    color = StarkTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 5. Interaction items
        items(interactions) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .testTag("interaction_log_${item.id}"),
                colors = CardDefaults.cardColors(containerColor = StarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(StarkBorder, StarkBorderBright))),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMMAND: \"${item.userQuery}\"",
                            color = StarkArcCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = timeFormatter.format(Date(item.timestampMillis)),
                            color = StarkTextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.jarvisResponse,
                        color = StarkTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onReplayAudio(item.jarvisResponse) },
                            modifier = Modifier.size(24.dp).testTag("replay_audio_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Replay Jarvis Response",
                                tint = StarkArcCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

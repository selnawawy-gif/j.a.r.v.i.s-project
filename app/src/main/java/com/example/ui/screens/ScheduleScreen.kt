package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calendar.CalendarEventItem
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    todayEvents: List<CalendarEventItem>,
    hasCalendarPermission: Boolean,
    onRequestCalendarPermission: () -> Unit,
    onVocalizeBriefing: () -> Unit,
    onAddEvent: (title: String, startMillis: Long, endMillis: Long, desc: String, loc: String) -> Unit,
    onRefreshCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.US)
    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    var showAddEventDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StarkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Header & Date
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "OPERATIONAL AGENDA",
                        color = StarkArcCyan,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = dateFormatter.format(Date()),
                        color = StarkTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = { showAddEventDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StarkArcCyan, contentColor = StarkDarkBg),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_event_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Vocalize Briefing Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("vocalize_briefing_card"),
                colors = CardDefaults.cardColors(containerColor = StarkSurfaceElevated),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StarkArcCyan, StarkCoreGold))),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = StarkCoreGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DAILY OPERATIONAL BRIEFING",
                                color = StarkCoreGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Command Jarvis to synthesize your device calendar events and pending tasks into a spoken daily schedule briefing.",
                        color = StarkTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onVocalizeBriefing,
                        colors = ButtonDefaults.buttonColors(containerColor = StarkCoreGold, contentColor = StarkDarkBg),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("trigger_vocal_briefing_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak Briefing",
                            tint = StarkDarkBg,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vocalize Schedule Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 3. Calendar Permission Banner
        if (!hasCalendarPermission) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("calendar_permission_banner"),
                    colors = CardDefaults.cardColors(containerColor = StarkSurfaceVariant),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StarkArcCyan, StarkBorder))),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DEVICE CALENDAR SYNC",
                                color = StarkArcCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Grant calendar permission to enable real-time reading and voice event creation.",
                                color = StarkTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onRequestCalendarPermission,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StarkArcCyan),
                            border = ButtonDefaults.outlinedButtonBorder().copy(brush = Brush.horizontalGradient(listOf(StarkArcCyan, StarkArcCyan))),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("grant_calendar_permission_button")
                        ) {
                            Text(text = "Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Events count and sync
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCHEDULED ENGAGEMENTS (${todayEvents.size})",
                    color = StarkArcCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                IconButton(
                    onClick = onRefreshCalendar,
                    modifier = Modifier.size(24.dp).testTag("refresh_calendar_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Refresh Calendar",
                        tint = StarkArcCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 5. Today's Events List
        if (todayEvents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StarkSurface)
                        .border(1.dp, StarkBorder, RoundedCornerShape(10.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = StarkTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Calendar Engagements Today",
                            color = StarkTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Say \"Hey Jarvis, schedule meeting at 3 PM\" or tap Add Event above.",
                            color = StarkTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(todayEvents) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("calendar_event_${event.id}"),
                    colors = CardDefaults.cardColors(containerColor = StarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(StarkBorder, StarkBorderBright))),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Time Pill
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = timeFormatter.format(Date(event.startEpochMillis)),
                                color = StarkArcCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                color = StarkTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (event.location.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = StarkTextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = event.location,
                                        color = StarkTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StarkBorder.copy(alpha = 0.5f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SYNCED",
                                color = StarkArcCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AddEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, startMillis, endMillis, desc, loc ->
                onAddEvent(title, startMillis, endMillis, desc, loc)
                showAddEventDialog = false
            }
        )
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, startMillis: Long, endMillis: Long, desc: String, loc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hoursFromNow by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StarkSurfaceElevated,
        title = {
            Text(
                text = "LOG CALENDAR ENGAGEMENT",
                color = StarkArcCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarkTextPrimary,
                        unfocusedTextColor = StarkTextPrimary,
                        focusedBorderColor = StarkArcCyan,
                        unfocusedBorderColor = StarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarkTextPrimary,
                        unfocusedTextColor = StarkTextPrimary,
                        focusedBorderColor = StarkArcCyan,
                        unfocusedBorderColor = StarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("event_location_input")
                )

                OutlinedTextField(
                    value = hoursFromNow,
                    onValueChange = { hoursFromNow = it },
                    label = { Text("Starts in (Hours from now)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarkTextPrimary,
                        unfocusedTextColor = StarkTextPrimary,
                        focusedBorderColor = StarkArcCyan,
                        unfocusedBorderColor = StarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("event_hours_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val hours = hoursFromNow.toIntOrNull() ?: 1
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.HOUR_OF_DAY, hours)
                        val start = cal.timeInMillis
                        val end = start + 3600000L
                        onConfirm(title, start, end, description, location)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StarkArcCyan, contentColor = StarkDarkBg),
                modifier = Modifier.testTag("confirm_add_event_button")
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StarkTextSecondary)
            }
        }
    )
}

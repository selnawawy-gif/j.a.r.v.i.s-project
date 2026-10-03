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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.JarvisTask
import com.example.data.TaskPriority
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkBorder
import com.example.ui.theme.StarkBorderBright
import com.example.ui.theme.StarkCoreGold
import com.example.ui.theme.StarkCrimson
import com.example.ui.theme.StarkDarkBg
import com.example.ui.theme.StarkSurface
import com.example.ui.theme.StarkSurfaceElevated
import com.example.ui.theme.StarkSurfaceVariant
import com.example.ui.theme.StarkTextPrimary
import com.example.ui.theme.StarkTextSecondary

@Composable
fun ProtocolsScreen(
    tasks: List<JarvisTask>,
    onAddTask: (title: String, priority: String, description: String) -> Unit,
    onToggleTask: (JarvisTask) -> Unit,
    onDeleteTask: (JarvisTask) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTasks = when (selectedFilter) {
        "ACTIVE" -> tasks.filter { !it.isCompleted }
        "COMPLETED" -> tasks.filter { it.isCompleted }
        else -> tasks
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(StarkDarkBg)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Top Header
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
                        text = "TASK PROTOCOLS",
                        color = StarkArcCyan,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${tasks.count { !it.isCompleted }} PENDING / ${tasks.size} TOTAL",
                        color = StarkTextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = StarkArcCyan, contentColor = StarkDarkBg),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_protocol_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Protocol", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Task", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Filters row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "ACTIVE", "COMPLETED").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StarkArcCyan,
                            selectedLabelColor = StarkDarkBg,
                            containerColor = StarkSurfaceVariant,
                            labelColor = StarkTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = StarkBorder,
                            selectedBorderColor = StarkArcCyan
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("filter_${filter.lowercase()}")
                    )
                }
            }
        }

        // Tasks list
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StarkSurface)
                        .border(1.dp, StarkBorder, RoundedCornerShape(10.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = StarkTextSecondary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Protocols Found",
                            color = StarkTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Voice command: \"Hey Jarvis, add task: [Task name]\"",
                            color = StarkTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredTasks) { task ->
                val priorityColor = when (task.priority) {
                    TaskPriority.OMEGA.code -> StarkCrimson
                    TaskPriority.ALPHA.code -> StarkCoreGold
                    TaskPriority.BETA.code -> StarkArcCyan
                    else -> StarkTextSecondary
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("task_item_${task.id}"),
                    colors = CardDefaults.cardColors(containerColor = StarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (task.isCompleted) StarkBorder else StarkBorderBright)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Checkbox
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.5.dp, if (task.isCompleted) StarkArcCyan else priorityColor, RoundedCornerShape(6.dp))
                                .background(if (task.isCompleted) StarkArcCyan else Color.Transparent)
                                .clickable { onToggleTask(task) }
                                .testTag("task_checkbox_${task.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = StarkDarkBg,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Priority Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(priorityColor.copy(alpha = 0.2f))
                                        .border(1.dp, priorityColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.priority,
                                        color = priorityColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    color = if (task.isCompleted) StarkTextSecondary else StarkTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }

                            if (task.description.isNotEmpty()) {
                                Text(
                                    text = task.description,
                                    color = StarkTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDeleteTask(task) },
                            modifier = Modifier.size(28.dp).testTag("delete_task_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Protocol",
                                tint = StarkTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, priority, desc ->
                onAddTask(title, priority, desc)
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, priority: String, desc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.ALPHA.code) }

    val priorities = listOf(
        TaskPriority.OMEGA.code,
        TaskPriority.ALPHA.code,
        TaskPriority.BETA.code,
        TaskPriority.DELTA.code
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StarkSurfaceElevated,
        title = {
            Text(
                text = "LOG NEW PROTOCOL",
                color = StarkArcCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Protocol Name / Objective") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarkTextPrimary,
                        unfocusedTextColor = StarkTextPrimary,
                        focusedBorderColor = StarkArcCyan,
                        unfocusedBorderColor = StarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("new_task_title_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details (Optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarkTextPrimary,
                        unfocusedTextColor = StarkTextPrimary,
                        focusedBorderColor = StarkArcCyan,
                        unfocusedBorderColor = StarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("new_task_desc_input")
                )

                Text(
                    text = "PRIORITY CLASSIFICATION",
                    color = StarkArcCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    priorities.forEach { p ->
                        val isSelected = selectedPriority == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) StarkArcCyan else StarkSurfaceVariant)
                                .clickable { selectedPriority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                color = if (isSelected) StarkDarkBg else StarkTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedPriority, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StarkArcCyan, contentColor = StarkDarkBg),
                modifier = Modifier.testTag("confirm_add_task_button")
            ) {
                Text("Register Protocol", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = StarkTextSecondary)
            }
        }
    )
}

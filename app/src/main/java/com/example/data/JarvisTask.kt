package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val displayName: String, val code: String) {
    OMEGA("Omega - Critical", "OMEGA"),
    ALPHA("Alpha - High", "ALPHA"),
    BETA("Beta - Standard", "BETA"),
    DELTA("Delta - Routine", "DELTA")
}

@Entity(tableName = "jarvis_tasks")
data class JarvisTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: String = TaskPriority.ALPHA.code,
    val dueDateEpochMillis: Long? = null,
    val isCompleted: Boolean = false,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val source: String = "JARVIS_PROTOCOL"
)

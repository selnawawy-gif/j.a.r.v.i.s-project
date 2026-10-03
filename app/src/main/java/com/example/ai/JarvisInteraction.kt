package com.example.ai

enum class InteractionActionType {
    GENERAL_TALK,
    CALENDAR_SUMMARY,
    CALENDAR_ADD,
    TASK_ADD,
    TASK_COMPLETE,
    APP_LAUNCH,
    DIAGNOSTICS,
    WAKE_WORD_ACK
}

data class JarvisInteraction(
    val id: Long = System.currentTimeMillis(),
    val timestampMillis: Long = System.currentTimeMillis(),
    val userQuery: String,
    val jarvisResponse: String,
    val actionType: InteractionActionType = InteractionActionType.GENERAL_TALK
)

package com.example.calendar

data class CalendarEventItem(
    val id: Long,
    val title: String,
    val description: String = "",
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val location: String = "",
    val isAllDay: Boolean = false,
    val isDeviceCalendar: Boolean = true
)

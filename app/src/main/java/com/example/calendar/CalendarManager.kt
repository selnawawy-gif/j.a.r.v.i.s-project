package com.example.calendar

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.Calendar
import java.util.TimeZone

class CalendarManager(private val context: Context) {

    fun hasCalendarPermission(): Boolean {
        val readGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val writeGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        return readGranted && writeGranted
    }

    fun getTodayEvents(): List<CalendarEventItem> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        return getEventsBetween(startOfDay, endOfDay)
    }

    fun getUpcomingEvents(daysAhead: Int = 3): List<CalendarEventItem> {
        val calendar = Calendar.getInstance()
        val startMillis = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, daysAhead)
        val endMillis = calendar.timeInMillis

        return getEventsBetween(startMillis, endMillis)
    }

    private fun getEventsBetween(startMillis: Long, endMillis: Long): List<CalendarEventItem> {
        val events = mutableListOf<CalendarEventItem>()
        if (!hasCalendarPermission()) {
            return events
        }

        try {
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startMillis)
            ContentUris.appendId(builder, endMillis)
            val uri = builder.build()

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.DESCRIPTION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.ALL_DAY
            )

            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID)
                val titleIdx = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
                val descIdx = cursor.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                val beginIdx = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
                val endIdx = cursor.getColumnIndex(CalendarContract.Instances.END)
                val locIdx = cursor.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
                val allDayIdx = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                while (cursor.moveToNext()) {
                    val id = if (idIdx >= 0) cursor.getLong(idIdx) else 0L
                    val title = if (titleIdx >= 0) cursor.getString(titleIdx) ?: "Scheduled Event" else "Scheduled Event"
                    val desc = if (descIdx >= 0) cursor.getString(descIdx) ?: "" else ""
                    val begin = if (beginIdx >= 0) cursor.getLong(beginIdx) else startMillis
                    val end = if (endIdx >= 0) cursor.getLong(endIdx) else begin + 3600000L
                    val loc = if (locIdx >= 0) cursor.getString(locIdx) ?: "" else ""
                    val allDay = if (allDayIdx >= 0) cursor.getInt(allDayIdx) == 1 else false

                    events.add(
                        CalendarEventItem(
                            id = id,
                            title = title,
                            description = desc,
                            startEpochMillis = begin,
                            endEpochMillis = end,
                            location = loc,
                            isAllDay = allDay,
                            isDeviceCalendar = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return events
    }

    fun addEvent(
        title: String,
        startMillis: Long,
        endMillis: Long,
        description: String = "",
        location: String = ""
    ): Long? {
        if (!hasCalendarPermission()) return null

        val calendarId = getDefaultCalendarId() ?: return null

        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            uri?.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteEvent(eventId: Long): Boolean {
        if (!hasCalendarPermission()) return false
        return try {
            val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.delete(deleteUri, null, null)
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun getDefaultCalendarId(): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.VISIBLE
        )
        try {
            context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val primaryIdx = cursor.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)

                var fallbackId: Long? = null
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIdx)
                    val isPrimary = if (primaryIdx >= 0) cursor.getInt(primaryIdx) == 1 else false
                    if (isPrimary) {
                        return id
                    }
                    if (fallbackId == null) {
                        fallbackId = id
                    }
                }
                return fallbackId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

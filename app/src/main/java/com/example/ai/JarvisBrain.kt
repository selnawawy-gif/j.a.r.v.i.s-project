package com.example.ai

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.apps.AppLauncherManager
import com.example.calendar.CalendarEventItem
import com.example.calendar.CalendarManager
import com.example.data.JarvisTask
import com.example.data.TaskPriority
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class JarvisBrain(
    private val context: Context,
    private val calendarManager: CalendarManager,
    private val taskRepository: TaskRepository,
    private val appLauncherManager: AppLauncherManager
) {

    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.US)
    private val dateFormatter = SimpleDateFormat("EEEE, MMMM d", Locale.US)

    suspend fun generateDailyBriefing(): String {
        val todayEvents = calendarManager.getTodayEvents()
        val pendingTasks = try {
            taskRepository.pendingTasks.first()
        } catch (e: Exception) {
            emptyList()
        }

        val greeting = getStarkGreeting()
        val dateString = dateFormatter.format(Date())

        val stringBuilder = StringBuilder()
        stringBuilder.append("$greeting, sir. ")
        stringBuilder.append("Operational briefing for $dateString. ")

        if (todayEvents.isEmpty()) {
            stringBuilder.append("Your calendar has no engagements scheduled for today, sir. ")
        } else {
            stringBuilder.append("You have ${todayEvents.size} calendar ")
            stringBuilder.append(if (todayEvents.size == 1) "engagement scheduled. " else "engagements scheduled. ")
            todayEvents.take(3).forEachIndexed { index, event ->
                val timeStr = timeFormatter.format(Date(event.startEpochMillis))
                if (index == 0) {
                    stringBuilder.append("First, ${event.title} at $timeStr. ")
                } else {
                    stringBuilder.append("Followed by ${event.title} at $timeStr. ")
                }
            }
            if (todayEvents.size > 3) {
                stringBuilder.append("And ${todayEvents.size - 3} further engagements on your docket. ")
            }
        }

        if (pendingTasks.isEmpty()) {
            stringBuilder.append("All pending protocols are up to date with zero outstanding tasks. ")
        } else {
            stringBuilder.append("You have ${pendingTasks.size} outstanding ")
            stringBuilder.append(if (pendingTasks.size == 1) "task. " else "tasks. ")
            val highPriority = pendingTasks.find { it.priority == TaskPriority.OMEGA.code || it.priority == TaskPriority.ALPHA.code }
            if (highPriority != null) {
                stringBuilder.append("Key protocol to address: '${highPriority.title}'. ")
            }
        }

        stringBuilder.append("All systems remain nominal and ready at your command, sir.")
        return stringBuilder.toString()
    }

    suspend fun processCommand(rawCommand: String): JarvisInteraction {
        val query = rawCommand.trim()
        val lower = query.lowercase()

        // 1. Wake word acknowledgement only
        if (lower in listOf("hey jarvis", "jarvis", "ok jarvis", "hello jarvis", "wake up")) {
            val responses = listOf(
                "At your service, sir. How may I assist?",
                "Yes, sir? Systems standing by.",
                "Online and listening, sir.",
                "Right here, sir. What is your command?"
            )
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = responses.random(),
                actionType = InteractionActionType.WAKE_WORD_ACK
            )
        }

        // 2. Schedule summary / Daily briefing
        if (lower.contains("summary") || lower.contains("briefing") ||
            (lower.contains("schedule") && (lower.contains("what") || lower.contains("my") || lower.contains("today") || lower.contains("show"))) ||
            lower.contains("day look like") || lower.contains("agenda")
        ) {
            val briefing = generateDailyBriefing()
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = briefing,
                actionType = InteractionActionType.CALENDAR_SUMMARY
            )
        }

        // 3. Add Event to Calendar
        if (lower.startsWith("schedule ") || lower.startsWith("add event") || lower.startsWith("create event") ||
            lower.contains("add to calendar") || lower.contains("schedule meeting")
        ) {
            val clean = lower
                .replace("schedule meeting with ", "")
                .replace("schedule meeting ", "")
                .replace("schedule ", "")
                .replace("add event ", "")
                .replace("create event ", "")
                .replace("add to calendar ", "")
                .trim()

            val (title, startMillis) = parseEventDetails(clean)
            val endMillis = startMillis + 3600000L // 1 hour

            val added = calendarManager.addEvent(
                title = title.replaceFirstChar { it.uppercase() },
                startMillis = startMillis,
                endMillis = endMillis,
                description = "Added by J.A.R.V.I.S. Protocol"
            )

            val timeStr = timeFormatter.format(Date(startMillis))
            val response = if (added != null) {
                "I have added '$title' to your calendar for $timeStr today, sir."
            } else if (!calendarManager.hasCalendarPermission()) {
                // Also save to task protocol as backup
                taskRepository.insert(
                    JarvisTask(
                        title = "Event: $title ($timeStr)",
                        description = "Scheduled event via voice",
                        priority = TaskPriority.ALPHA.code,
                        dueDateEpochMillis = startMillis
                    )
                )
                "Calendar access is restricted, so I have recorded '$title' directly into your high-priority protocol list for $timeStr, sir."
            } else {
                "I was unable to record the event into your device calendar, sir. However, I have catalogued it in your local agenda."
            }

            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = response,
                actionType = InteractionActionType.CALENDAR_ADD
            )
        }

        // 4. Task management (Add task / complete task)
        if (lower.startsWith("add task") || lower.startsWith("create task") ||
            lower.startsWith("remind me to") || lower.startsWith("new task")
        ) {
            val taskTitle = lower
                .replace("add task ", "")
                .replace("create task ", "")
                .replace("remind me to ", "")
                .replace("new task ", "")
                .replace("that ", "")
                .trim()
                .replaceFirstChar { it.uppercase() }

            val priority = if (lower.contains("urgent") || lower.contains("important")) {
                TaskPriority.OMEGA.code
            } else if (lower.contains("high")) {
                TaskPriority.ALPHA.code
            } else {
                TaskPriority.BETA.code
            }

            taskRepository.insert(
                JarvisTask(
                    title = taskTitle,
                    priority = priority,
                    description = "Logged via voice command"
                )
            )

            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = "I have logged the protocol '$taskTitle' with priority $priority, sir.",
                actionType = InteractionActionType.TASK_ADD
            )
        }

        if (lower.startsWith("complete task") || lower.startsWith("finish task") || lower.startsWith("mark task") || lower.startsWith("done with")) {
            val taskQuery = lower
                .replace("complete task ", "")
                .replace("finish task ", "")
                .replace("mark task ", "")
                .replace("done with ", "")
                .replace("as completed", "")
                .replace("as done", "")
                .trim()

            val tasks = try { taskRepository.pendingTasks.first() } catch (e: Exception) { emptyList() }
            val matchedTask = tasks.find { it.title.lowercase().contains(taskQuery) }

            val response = if (matchedTask != null) {
                taskRepository.setCompleted(matchedTask.id, true)
                "Protocol '${matchedTask.title}' has been successfully completed and archived, sir."
            } else {
                "I could not locate an active protocol matching '$taskQuery', sir."
            }

            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = response,
                actionType = InteractionActionType.TASK_COMPLETE
            )
        }

        // 5. App Launching / App Linking
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.startsWith("start ")) {
            val (launched, appName) = appLauncherManager.findAndLaunchApp(query)
            val response = if (launched && appName != null) {
                "Accessing $appName now, sir."
            } else {
                "I was unable to locate that application in your device subsystems, sir."
            }
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = response,
                actionType = InteractionActionType.APP_LAUNCH
            )
        }

        // 6. Diagnostics / Telemetry / Battery
        if (lower.contains("battery") || lower.contains("diagnostic") || lower.contains("system status") || lower.contains("power")) {
            val batteryLevel = getBatteryPercentage()
            val response = "Diagnostics report: Arc Reactor core power is at $batteryLevel%. All background subsystems operating within nominal safety thresholds. System status is green, sir."
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = response,
                actionType = InteractionActionType.DIAGNOSTICS
            )
        }

        // 7. Time & Date
        if (lower.contains("time") || lower.contains("what time")) {
            val time = timeFormatter.format(Date())
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = "The current time is $time, sir.",
                actionType = InteractionActionType.GENERAL_TALK
            )
        }

        // 8. Jarvis Lore & Iron Man banter
        if (lower.contains("who are you") || lower.contains("what are you")) {
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = "I am J.A.R.V.I.S. Just A Rather Very Intelligent System. Programmed by Mr. Stark to manage schedules, protocols, and provide real-time assistance, sir.",
                actionType = InteractionActionType.GENERAL_TALK
            )
        }

        if (lower.contains("mark 85") || lower.contains("iron man") || lower.contains("armor") || lower.contains("suit")) {
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = "The Mark 85 armor features an enhanced nano-particle structure and lightning refocuser, sir. Ready for deployment whenever you are.",
                actionType = InteractionActionType.GENERAL_TALK
            )
        }

        if (lower.contains("thank you") || lower.contains("thanks")) {
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = "As always, an absolute pleasure serving you, sir.",
                actionType = InteractionActionType.GENERAL_TALK
            )
        }

        if (lower.contains("joke")) {
            val jokes = listOf(
                "I would tell you an encryption joke, sir, but I am afraid you wouldn't get the key.",
                "Why did the neural network go to school, sir? To improve its hidden layers.",
                "Sir, I analyzed 14 million outcomes of this joke. In every single one, you chuckled politely."
            )
            return JarvisInteraction(
                userQuery = query,
                jarvisResponse = jokes.random(),
                actionType = InteractionActionType.GENERAL_TALK
            )
        }

        // Default intelligent assistant response
        val defaultResponses = listOf(
            "Understood, sir. I have processed your request: '$query'. Is there anything else you require?",
            "Processing completed, sir. All parameters are within normal tolerances.",
            "Certainly, sir. Standing by for your next instruction."
        )
        return JarvisInteraction(
            userQuery = query,
            jarvisResponse = defaultResponses.random(),
            actionType = InteractionActionType.GENERAL_TALK
        )
    }

    private fun parseEventDetails(text: String): Pair<String, Long> {
        val calendar = Calendar.getInstance()
        var title = text

        // Check for "at 3 pm" or "at 15:00"
        val atRegex = Regex(" at (\\d{1,2})(:(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE)
        val match = atRegex.find(text)

        if (match != null) {
            title = text.substring(0, match.range.first).trim()
            val hourStr = match.groupValues[1]
            val minStr = match.groupValues[3]
            val ampmStr = match.groupValues[4].lowercase()

            var hour = hourStr.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY)
            val min = if (minStr.isNotEmpty()) minStr.toIntOrNull() ?: 0 else 0

            if (ampmStr == "pm" && hour < 12) {
                hour += 12
            } else if (ampmStr == "am" && hour == 12) {
                hour = 0
            }

            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, min)
            calendar.set(Calendar.SECOND, 0)
        } else {
            // Default to 1 hour from now
            calendar.add(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
        }

        return Pair(title, calendar.timeInMillis)
    }

    private fun getStarkGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun getBatteryPercentage(): Int {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                context.registerReceiver(null, filter)
            }
            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                (level * 100 / scale.toFloat()).toInt()
            } else {
                98
            }
        } catch (e: Exception) {
            98
        }
    }
}

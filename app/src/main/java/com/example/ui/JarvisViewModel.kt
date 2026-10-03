package com.example.ui

import android.app.Application
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.Voice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.InteractionActionType
import com.example.ai.JarvisBrain
import com.example.ai.JarvisInteraction
import com.example.apps.AppLauncherManager
import com.example.apps.InstalledAppItem
import com.example.calendar.CalendarEventItem
import com.example.calendar.CalendarManager
import com.example.data.JarvisDatabase
import com.example.data.JarvisTask
import com.example.data.TaskPriority
import com.example.data.TaskRepository
import com.example.voice.AssistantAudioState
import com.example.voice.JarvisVoiceEngine
import com.example.voice.WakeWordEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext

    // Dependencies
    private val database = JarvisDatabase.getInstance(context)
    val taskRepository = TaskRepository(database.taskDao())
    val calendarManager = CalendarManager(context)
    val appLauncherManager = AppLauncherManager(context)
    val voiceEngine = JarvisVoiceEngine(context)

    val brain = JarvisBrain(
        context = context,
        calendarManager = calendarManager,
        taskRepository = taskRepository,
        appLauncherManager = appLauncherManager
    )

    // Wake Word Engine with callbacks
    val wakeWordEngine = WakeWordEngine(
        context = context,
        onWakeWordDetected = { onWakeWordDetected() },
        onCommandReceived = { command -> processSpokenCommand(command) }
    )

    // Observable states
    val allTasks: StateFlow<List<JarvisTask>> = taskRepository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasksCount: StateFlow<Int> = taskRepository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _todayEvents = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    val todayEvents: StateFlow<List<CalendarEventItem>> = _todayEvents.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    private val _interactions = MutableStateFlow<List<JarvisInteraction>>(
        listOf(
            JarvisInteraction(
                userQuery = "System Diagnostics",
                jarvisResponse = "Online and fully operational, sir. J.A.R.V.I.S. protocol Mark LXXXV is standing by. Speak 'Hey Jarvis' or tap the Arc Reactor.",
                actionType = InteractionActionType.DIAGNOSTICS
            )
        )
    )
    val interactions: StateFlow<List<JarvisInteraction>> = _interactions.asStateFlow()

    val audioState: StateFlow<AssistantAudioState> = wakeWordEngine.audioState
    val rmsAudioLevel: StateFlow<Float> = wakeWordEngine.rmsAudioLevel
    val liveTranscript: StateFlow<String> = wakeWordEngine.liveTranscript
    val isWakeWordEnabled: StateFlow<Boolean> = wakeWordEngine.isWakeWordEnabled

    val isSpeaking: StateFlow<Boolean> = voiceEngine.isSpeaking
    val currentSpokenText: StateFlow<String> = voiceEngine.currentSpokenText
    val availableVoices: StateFlow<List<Voice>> = voiceEngine.availableVoices
    val selectedVoiceName: StateFlow<String> = voiceEngine.selectedVoiceName

    private val _batteryPercent = MutableStateFlow(98)
    val batteryPercent: StateFlow<Int> = _batteryPercent.asStateFlow()

    private val _isForeground = MutableStateFlow(true)
    val isForeground: StateFlow<Boolean> = _isForeground.asStateFlow()

    private val _hasCalendarPermission = MutableStateFlow(calendarManager.hasCalendarPermission())
    val hasCalendarPermission: StateFlow<Boolean> = _hasCalendarPermission.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()

    private val _voicePitch = MutableStateFlow(voiceEngine.pitch)
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(voiceEngine.speechRate)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    init {
        refreshCalendarEvents()
        refreshApps()
        updateBatteryLevel()

        // Insert initial demo Stark tasks if database is empty
        viewModelScope.launch(Dispatchers.IO) {
            val existing = allTasks.value
            if (existing.isEmpty()) {
                taskRepository.insert(
                    JarvisTask(
                        title = "Calibrate Mark 85 Flight Stabilizers",
                        description = "Verify nano-particle distribution on left repulsor",
                        priority = TaskPriority.OMEGA.code
                    )
                )
                taskRepository.insert(
                    JarvisTask(
                        title = "Review Clean Energy Grid Analytics",
                        description = "Stark Tower arc reactor efficiency diagnostics",
                        priority = TaskPriority.ALPHA.code
                    )
                )
                taskRepository.insert(
                    JarvisTask(
                        title = "Schedule Avengers Briefing",
                        description = "Compound tactical update session",
                        priority = TaskPriority.BETA.code
                    )
                )
            }
        }
    }

    fun setAppForeground(inForeground: Boolean) {
        _isForeground.value = inForeground
        voiceEngine.isForeground = inForeground
        wakeWordEngine.isForeground = inForeground

        if (inForeground) {
            refreshCalendarEvents()
            updateBatteryLevel()
        }
    }

    fun setAudioPermissionGranted(granted: Boolean) {
        _hasAudioPermission.value = granted
        if (granted && _isForeground.value && isWakeWordEnabled.value) {
            wakeWordEngine.startWakeWordListening()
        }
    }

    fun setCalendarPermissionGranted(granted: Boolean) {
        _hasCalendarPermission.value = granted
        refreshCalendarEvents()
    }

    fun refreshCalendarEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            _hasCalendarPermission.value = calendarManager.hasCalendarPermission()
            val events = calendarManager.getTodayEvents()
            _todayEvents.value = events
        }
    }

    fun refreshApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = appLauncherManager.getInstalledApps()
            _installedApps.value = apps
        }
    }

    fun updateBatteryLevel() {
        viewModelScope.launch(Dispatchers.IO) {
            val level = brain.getBatteryPercentage()
            _batteryPercent.value = level
        }
    }

    private fun onWakeWordDetected() {
        triggerHaptic()
        viewModelScope.launch(Dispatchers.Main) {
            val responses = listOf(
                "At your service, sir.",
                "Yes, sir?",
                "Standing by, sir.",
                "Online, sir. How may I assist?"
            )
            val ack = responses.random()
            voiceEngine.speak(ack)
        }
    }

    fun processSpokenCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val interaction = brain.processCommand(command)

            // Add to timeline
            _interactions.value = listOf(interaction) + _interactions.value

            // Vocalize through Jarvis's authentic voice engine
            if (_isForeground.value) {
                voiceEngine.speak(interaction.jarvisResponse)
            }

            // Refresh events or tasks if modified
            if (interaction.actionType == InteractionActionType.CALENDAR_ADD) {
                refreshCalendarEvents()
            }
        }
    }

    fun vocalizeDailyBriefing() {
        viewModelScope.launch(Dispatchers.IO) {
            val briefing = brain.generateDailyBriefing()
            val interaction = JarvisInteraction(
                userQuery = "Daily Operational Briefing",
                jarvisResponse = briefing,
                actionType = InteractionActionType.CALENDAR_SUMMARY
            )
            _interactions.value = listOf(interaction) + _interactions.value

            if (_isForeground.value) {
                voiceEngine.speak(briefing)
            }
        }
    }

    fun triggerArcReactorTap() {
        triggerHaptic()
        if (voiceEngine.isSpeaking.value) {
            voiceEngine.stop()
        } else {
            wakeWordEngine.startDirectCommandListening()
        }
    }

    fun toggleWakeWordEnabled(enabled: Boolean) {
        wakeWordEngine.setWakeWordEnabled(enabled)
    }

    fun addTask(title: String, priority: String, description: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.insert(
                JarvisTask(
                    title = title,
                    priority = priority,
                    description = description
                )
            )
            voiceEngine.speak("Task '$title' logged to protocols, sir.")
        }
    }

    fun toggleTaskCompleted(task: JarvisTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.setCompleted(task.id, !task.isCompleted)
            if (!task.isCompleted) {
                triggerHaptic()
                voiceEngine.speak("Protocol '${task.title}' archived as complete, sir.")
            }
        }
    }

    fun deleteTask(task: JarvisTask) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.delete(task)
        }
    }

    fun addCalendarEvent(
        title: String,
        startMillis: Long,
        endMillis: Long,
        description: String = "",
        location: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = calendarManager.addEvent(title, startMillis, endMillis, description, location)
            if (id != null) {
                refreshCalendarEvents()
                voiceEngine.speak("Engagement '$title' synchronized to calendar, sir.")
            } else {
                // Save to local task fallback
                taskRepository.insert(
                    JarvisTask(
                        title = "Event: $title",
                        description = "Location: $location. $description",
                        priority = TaskPriority.ALPHA.code,
                        dueDateEpochMillis = startMillis
                    )
                )
                voiceEngine.speak("Calendar writing is restricted; I've saved '$title' to your protocols, sir.")
            }
        }
    }

    fun launchApp(app: InstalledAppItem) {
        viewModelScope.launch(Dispatchers.Main) {
            voiceEngine.speak("Accessing ${app.name}, sir.")
            appLauncherManager.launchApp(app.packageName)
        }
    }

    fun setVoicePitch(pitch: Float) {
        _voicePitch.value = pitch
        voiceEngine.pitch = pitch
    }

    fun setVoiceSpeed(speed: Float) {
        _voiceSpeed.value = speed
        voiceEngine.speechRate = speed
    }

    fun selectVoice(name: String) {
        voiceEngine.selectVoiceByName(name)
    }

    fun testJarvisVoice(quote: String) {
        voiceEngine.speak(quote)
    }

    fun speakText(text: String) {
        voiceEngine.speak(text)
    }

    fun stopSpeaking() {
        voiceEngine.stop()
    }

    private fun triggerHaptic() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        wakeWordEngine.destroy()
        voiceEngine.shutdown()
    }
}

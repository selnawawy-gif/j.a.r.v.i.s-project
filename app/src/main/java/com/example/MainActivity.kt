package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.JarvisViewModel
import com.example.ui.components.StarkHudHeader
import com.example.ui.screens.AppLauncherScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ProtocolsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.VoiceSettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StarkArcCyan
import com.example.ui.theme.StarkBorder
import com.example.ui.theme.StarkDarkBg
import com.example.ui.theme.StarkSurface
import com.example.ui.theme.StarkTextSecondary

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setAppForeground(true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setAppForeground(false)
    }

    override fun onStop() {
        super.onStop()
        viewModel.setAppForeground(false)
    }
}

@Composable
fun JarvisApp(viewModel: JarvisViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe Android lifecycle to strictly enforce foreground-only operation
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setAppForeground(true)
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> viewModel.setAppForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Permission Launchers
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setAudioPermissionGranted(isGranted)
    }

    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_CALENDAR] == true
        viewModel.setCalendarPermissionGranted(granted)
    }

    // Check initial permissions
    LaunchedEffect(Unit) {
        val audioGranted = ContextCompat.checkSelfPermission(
            viewModel.getApplication(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setAudioPermissionGranted(audioGranted)

        if (!audioGranted) {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        val calGranted = ContextCompat.checkSelfPermission(
            viewModel.getApplication(),
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setCalendarPermissionGranted(calGranted)
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    val navTabs = listOf(
        NavTabItem("Core", Icons.Default.Radio, "nav_core"),
        NavTabItem("Agenda", Icons.Default.CalendarMonth, "nav_agenda"),
        NavTabItem("Protocols", Icons.Default.TaskAlt, "nav_protocols"),
        NavTabItem("Uplink", Icons.Default.Apps, "nav_uplink"),
        NavTabItem("Audio", Icons.Default.Tune, "nav_audio")
    )

    // State collections
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val rmsAudioLevel by viewModel.rmsAudioLevel.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val currentSpokenText by viewModel.currentSpokenText.collectAsStateWithLifecycle()
    val liveTranscript by viewModel.liveTranscript.collectAsStateWithLifecycle()
    val isWakeWordEnabled by viewModel.isWakeWordEnabled.collectAsStateWithLifecycle()
    val interactions by viewModel.interactions.collectAsStateWithLifecycle()
    val batteryPercent by viewModel.batteryPercent.collectAsStateWithLifecycle()
    val isForeground by viewModel.isForeground.collectAsStateWithLifecycle()

    val todayEvents by viewModel.todayEvents.collectAsStateWithLifecycle()
    val hasCalendarPermission by viewModel.hasCalendarPermission.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    val voicePitch by viewModel.voicePitch.collectAsStateWithLifecycle()
    val voiceSpeed by viewModel.voiceSpeed.collectAsStateWithLifecycle()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsStateWithLifecycle()
    val availableVoices by viewModel.availableVoices.collectAsStateWithLifecycle()
    val hasAudioPermission by viewModel.hasAudioPermission.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(StarkDarkBg),
        topBar = {
            StarkHudHeader(
                batteryPercent = batteryPercent,
                isListening = audioState == com.example.voice.AssistantAudioState.LISTENING_FOR_WAKE_WORD || audioState == com.example.voice.AssistantAudioState.LISTENING_FOR_COMMAND,
                isSpeaking = isSpeaking,
                isForeground = isForeground
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, StarkBorder)
            ) {
                navTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StarkDarkBg,
                            selectedTextColor = StarkArcCyan,
                            indicatorColor = StarkArcCyan,
                            unselectedIconColor = StarkTextSecondary,
                            unselectedTextColor = StarkTextSecondary
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    audioState = audioState,
                    rmsAudioLevel = rmsAudioLevel,
                    isSpeaking = isSpeaking,
                    currentSpokenText = currentSpokenText,
                    liveTranscript = liveTranscript,
                    isWakeWordEnabled = isWakeWordEnabled,
                    interactions = interactions,
                    onArcReactorClick = { viewModel.triggerArcReactorTap() },
                    onToggleWakeWord = { viewModel.toggleWakeWordEnabled(it) },
                    onQuickCommand = { viewModel.processSpokenCommand(it) },
                    onStopSpeaking = { viewModel.stopSpeaking() },
                    onReplayAudio = { viewModel.speakText(it) }
                )
                1 -> ScheduleScreen(
                    todayEvents = todayEvents,
                    hasCalendarPermission = hasCalendarPermission,
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            )
                        )
                    },
                    onVocalizeBriefing = { viewModel.vocalizeDailyBriefing() },
                    onAddEvent = { title, start, end, desc, loc ->
                        viewModel.addCalendarEvent(title, start, end, desc, loc)
                    },
                    onRefreshCalendar = { viewModel.refreshCalendarEvents() }
                )
                2 -> ProtocolsScreen(
                    tasks = allTasks,
                    onAddTask = { title, priority, desc ->
                        viewModel.addTask(title, priority, desc)
                    },
                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )
                3 -> AppLauncherScreen(
                    installedApps = installedApps,
                    onLaunchApp = { viewModel.launchApp(it) }
                )
                4 -> VoiceSettingsScreen(
                    voicePitch = voicePitch,
                    voiceSpeed = voiceSpeed,
                    selectedVoiceName = selectedVoiceName,
                    availableVoices = availableVoices,
                    isWakeWordEnabled = isWakeWordEnabled,
                    isForeground = isForeground,
                    hasAudioPermission = hasAudioPermission,
                    hasCalendarPermission = hasCalendarPermission,
                    onPitchChange = { viewModel.setVoicePitch(it) },
                    onSpeedChange = { viewModel.setVoiceSpeed(it) },
                    onSelectVoice = { viewModel.selectVoice(it) },
                    onToggleWakeWord = { viewModel.toggleWakeWordEnabled(it) },
                    onTestVoice = { viewModel.testJarvisVoice(it) },
                    onRequestAudioPermission = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR
                            )
                        )
                    }
                )
            }
        }
    }
}

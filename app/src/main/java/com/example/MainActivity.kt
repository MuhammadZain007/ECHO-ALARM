package com.example

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.NotificationHelper
import com.example.ui.EchoAlarmViewModel
import com.example.ui.screens.*
import com.example.ui.theme.EchoAlarmTheme

class MainActivity : ComponentActivity() {

    private var alarmTriggerReceiver: BroadcastReceiver? = null
    private var echoAlarmVm: EchoAlarmViewModel? = null

    // Register launchers for recording mic permission requests
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Mic Recording perm granted.")
        } else {
            Log.w("MainActivity", "Mic Recording perm denied.")
        }
    }

    // Register launcher for post notification permission requests (Android 13+)
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted.")
        } else {
            Log.w("MainActivity", "Notification permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create the notification channel
        NotificationHelper.createNotificationChannel(this)

        // Check and ask for recording permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // Check and ask for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val viewModel: EchoAlarmViewModel = viewModel()
            echoAlarmVm = viewModel

            // Direct start check from scheduled Alarm payloads
            LaunchedEffect(intent) {
                handleRingerPayload(intent, viewModel)
            }

            val currentTheme by viewModel.currentTheme.collectAsState()
            var currentTab by remember { mutableStateOf("Home") }

            EchoAlarmTheme(themeName = currentTheme) {
                // Main outer scaffold respects notch/system cutouts
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .testTag("app_navigation_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars)
                            ) {
                                val navTabs = listOf(
                                    NavTabItem("Home", Icons.Default.Home, "home_tab"),
                                    NavTabItem("Alarms", Icons.Default.Alarm, "alarms_tab"),
                                    NavTabItem("Themes", Icons.Default.Palette, "themes_tab"),
                                    NavTabItem("Sleep", Icons.Default.Bedtime, "sleep_tab"),
                                    NavTabItem("Settings", Icons.Default.Settings, "settings_tab")
                                )

                                navTabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = currentTab == tab.name,
                                        onClick = { currentTab = tab.name },
                                        icon = { Icon(tab.icon, contentDescription = tab.name) },
                                        label = { Text(tab.name) },
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        // Crossfaded Screen transitions
                        Crossfade(
                            targetState = currentTab,
                            animationSpec = tween(250),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            label = "screen_fade"
                        ) { tabState ->
                            when (tabState) {
                                "Home" -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToAlarms = { currentTab = "Alarms" }
                                )
                                "Alarms" -> AlarmsScreen(viewModel = viewModel)
                                "Themes" -> ThemesScreen(viewModel = viewModel)
                                "Sleep" -> SleepScreen(viewModel = viewModel)
                                "Settings" -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // 3. OVERLAY RINGER: Overrides all active screens on alarm triggers
                    val activeRingerAlarm by viewModel.activeRingerAlarm.collectAsState()
                    AnimatedVisibility(
                        visible = activeRingerAlarm != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        RingerOverlay(
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // Register dynamic receiver for inline alarm triggers
        registerTriggerReceiver()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        echoAlarmVm?.let { vm ->
            handleRingerPayload(intent, vm)
        }
    }

    private fun handleRingerPayload(intent: Intent?, vm: EchoAlarmViewModel) {
        val alarmId = intent?.getIntExtra("RINGING_ALARM_ID", -1) ?: -1
        if (alarmId != -1) {
            Log.d("MainActivity", "Launch ringer payload for alarm ID: $alarmId")
            vm.triggerAlarmRinging(alarmId)
        }
    }

    private fun registerTriggerReceiver() {
        alarmTriggerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val alarmId = intent.getIntExtra("RINGING_ALARM_ID", -1)
                if (alarmId != -1) {
                    echoAlarmVm?.triggerAlarmRinging(alarmId)
                }
            }
        }
        
        val filter = IntentFilter("com.example.echoalarm.ALARM_TRIGGERED")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(alarmTriggerReceiver, filter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(alarmTriggerReceiver, filter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmTriggerReceiver?.let {
            unregisterReceiver(it)
        }
        alarmTriggerReceiver = null
    }
}

data class NavTabItem(
    val name: String,
    val icon: ImageVector,
    val testTag: String
)

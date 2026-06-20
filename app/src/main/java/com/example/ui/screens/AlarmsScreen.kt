package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Alarm
import com.example.data.VoiceNote
import com.example.ui.EchoAlarmViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    viewModel: EchoAlarmViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val voiceNotes by viewModel.voiceNotes.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val isTtsReady by viewModel.isTtsReady.collectAsState()
    val isPlayingPreview by viewModel.isPlayingPreview.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var selectedAlarmForEdit by remember { mutableStateOf<Alarm?>(null) }

    // Form states
    var formHour by remember { mutableIntStateOf(7) }
    var formMinute by remember { mutableIntStateOf(0) }
    var formLabel by remember { mutableStateOf("Morning Alarm") }
    val formRepeatDays = remember { mutableStateListOf<Int>() } // Mon=1..Sun=7
    var formSnoozeMinutes by remember { mutableIntStateOf(5) }
    var formVoiceType by remember { mutableStateOf("AI") } // "AI", "RECORDED", "SYSTEM", "PRESET"
    var formVoiceText by remember { mutableStateOf("Wake up Zain, it's time for university.") }
    var formVoiceLanguage by remember { mutableStateOf("English") }
    var formVoiceStyle by remember { mutableStateOf("Motivational Coach") }
    var formVoiceFilePath by remember { mutableStateOf<String?>(null) }
    var formChallengeType by remember { mutableStateOf("NONE") } // "NONE", "MATH", "SHAKE", "MEMORY", "TYPING", "CAPTCHA"
    var formChallengeDifficulty by remember { mutableStateOf("MEDIUM") }
    var formIsGradualVolume by remember { mutableStateOf(true) }
    var formAlarmTheme by remember { mutableStateOf("Bold Typography") }

    // Direct recording name state
    var micRecordTitle by remember { mutableStateOf("") }

    val openAddForm = {
        val calendar = Calendar.getInstance()
        formHour = calendar.get(Calendar.HOUR_OF_DAY)
        formMinute = calendar.get(Calendar.MINUTE)
        formLabel = "Morning Alarm"
        formRepeatDays.clear()
        formSnoozeMinutes = 5
        formVoiceType = "AI"
        formVoiceText = "Rise and shine! Time to get going."
        formVoiceLanguage = "English"
        formVoiceStyle = "Motivational Coach"
        formVoiceFilePath = null
        formChallengeType = "NONE"
        formChallengeDifficulty = "MEDIUM"
        formIsGradualVolume = true
        formAlarmTheme = currentTheme
        selectedAlarmForEdit = null
        showEditor = true
    }

    val openEditForm = { alarm: Alarm ->
        formHour = alarm.hour
        formMinute = alarm.minute
        formLabel = alarm.label
        formRepeatDays.clear()
        formRepeatDays.addAll(alarm.getRepeatDaysList())
        formSnoozeMinutes = alarm.snoozeDurationMinutes
        formVoiceType = alarm.voiceType
        formVoiceText = alarm.voiceText
        formVoiceLanguage = alarm.voiceLanguage
        formVoiceStyle = alarm.voiceStyle
        formVoiceFilePath = alarm.voiceFilePath
        formChallengeType = alarm.challengeType
        formChallengeDifficulty = alarm.challengeDifficulty
        formIsGradualVolume = alarm.isGradualVolume
        formAlarmTheme = alarm.themeName
        selectedAlarmForEdit = alarm
        showEditor = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_alarm_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ALARM MANAGEMENT",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "No alarms",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "No Alarms Scheduled Today",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Tap + to construct your custom echo alarm.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        Card(
                            onClick = { openEditForm(alarm) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (alarm.isEnabled)
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (alarm.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .testTag("alarm_item_${alarm.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = alarm.getFormattedTime(),
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (alarm.isEnabled)
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = alarm.label,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Quick testing trigger button
                                        IconButton(
                                            onClick = { viewModel.testRingerTransition(alarm) },
                                            modifier = Modifier.testTag("test_trigger_alarm_${alarm.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Test trigger",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Switch(
                                            checked = alarm.isEnabled,
                                            onCheckedChange = { viewModel.toggleAlarm(alarm) },
                                            modifier = Modifier.testTag("toggle_switch_${alarm.id}")
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Days: ${alarm.getRepeatDaysShortText()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Display little icons based on selected features
                                        if (alarm.voiceType == "AI") {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text("AI Voice: ${alarm.voiceStyle}", fontSize = 9.sp) },
                                                icon = { Icon(Icons.Default.AutoAwesome, "AI", modifier = Modifier.size(10.dp)) }
                                            )
                                        }
                                        if (alarm.challengeType != "NONE") {
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(alarm.challengeType, fontSize = 9.sp) },
                                                icon = { Icon(Icons.Default.Extension, "Challenge", modifier = Modifier.size(10.dp)) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Comprehensive Editor Overlay (Direct Bottom Sheet or Modal style Dialog)
    if (showEditor) {
        AlertDialog(
            onDismissRequest = { showEditor = false },
            confirmButton = {
                Button(
                    onClick = {
                        val repeatStr = formRepeatDays.sorted().joinToString(",")
                        if (selectedAlarmForEdit == null) {
                            // Add new
                            viewModel.addAlarm(
                                hour = formHour,
                                minute = formMinute,
                                label = formLabel,
                                repeatDays = formRepeatDays.toList(),
                                voiceType = formVoiceType,
                                voiceText = formVoiceText,
                                voiceLanguage = formVoiceLanguage,
                                voiceStyle = formVoiceStyle,
                                voiceFilePath = formVoiceFilePath,
                                challengeType = formChallengeType,
                                challengeDifficulty = formChallengeDifficulty,
                                isGradualVolume = formIsGradualVolume,
                                themeName = formAlarmTheme
                            )
                        } else {
                            // Edit existing
                            val updated = selectedAlarmForEdit!!.copy(
                                hour = formHour,
                                minute = formMinute,
                                label = formLabel,
                                repeatDays = repeatStr,
                                voiceType = formVoiceType,
                                voiceText = formVoiceText,
                                voiceLanguage = formVoiceLanguage,
                                voiceStyle = formVoiceStyle,
                                voiceFilePath = formVoiceFilePath,
                                challengeType = formChallengeType,
                                challengeDifficulty = formChallengeDifficulty,
                                isGradualVolume = formIsGradualVolume,
                                themeName = formAlarmTheme
                            )
                            viewModel.editAlarm(updated)
                        }
                        showEditor = false
                    },
                    modifier = Modifier.testTag("save_alarm_button")
                ) {
                    Text("Save Alarm")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedAlarmForEdit != null) {
                        TextButton(
                            onClick = {
                                viewModel.deleteAlarm(selectedAlarmForEdit!!)
                                showEditor = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.testTag("delete_alarm_button")
                        ) {
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { showEditor = false }) {
                        Text("Cancel")
                    }
                }
            },
            title = {
                Text(
                    text = if (selectedAlarmForEdit == null) "Construct New Alarm" else "Configure Alarm",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            },
            text = {
                val focusManager = LocalFocusManager.current
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Time Selector
                    item {
                        Text("TIME SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour Picker
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = { formHour = (formHour + 1) % 24 },
                                    colors = ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Icon(Icons.Default.ArrowDropUp, "+") }
                                Text(
                                    text = String.format("%02d", formHour),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = { formHour = (formHour + 23) % 24 },
                                    colors = ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Icon(Icons.Default.ArrowDropDown, "-") }
                            }
                            Text(":", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                            // Minute Picker
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(
                                    onClick = { formMinute = (formMinute + 1) % 60 },
                                    colors = ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Icon(Icons.Default.ArrowDropUp, "+") }
                                Text(
                                    text = String.format("%02d", formMinute),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Button(
                                    onClick = { formMinute = (formMinute + 59) % 60 },
                                    colors = ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier.size(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Icon(Icons.Default.ArrowDropDown, "-") }
                            }
                        }
                    }

                    // Label String
                    item {
                        OutlinedTextField(
                            value = formLabel,
                            onValueChange = { formLabel = it },
                            label = { Text("Alarm Label") },
                            modifier = Modifier.fillMaxWidth().testTag("alarm_label_input")
                        )
                    }

                    // Schedule Repeat Days Selection
                    item {
                        Text("REPEAT DAYS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (dayIndex in 1..7) {
                                val letter = dayLetters[dayIndex - 1]
                                val isSelected = formRepeatDays.contains(dayIndex)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .clickable {
                                            if (isSelected) {
                                                formRepeatDays.remove(dayIndex)
                                            } else {
                                                formRepeatDays.add(dayIndex)
                                            }
                                        }
                                        .testTag("day_selector_$dayIndex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = letter,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Theme selector override setting
                    item {
                        Text("ALARM DESIGN OVERRIDE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        val systemThemes = listOf(
                            "Bold Typography", "AMOLED Black", "Neon Cyberpunk", "Matrix Green", "Glassmorphism",
                            "Minimal White", "Luxury Gold", "Modern Steel", "Wooden Clock", "Futuristic Clock"
                        )
                        var expandedTheme by remember { mutableStateOf(false) }
                        
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            OutlinedButton(
                                onClick = { expandedTheme = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Selected Theme: $formAlarmTheme")
                            }
                            DropdownMenu(
                                expanded = expandedTheme,
                                onDismissRequest = { expandedTheme = false }
                            ) {
                                systemThemes.forEach { themed ->
                                    DropdownMenuItem(
                                        text = { Text(themed) },
                                        onClick = {
                                            formAlarmTheme = themed
                                            expandedTheme = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Voice Tone Trigger Method
                    item {
                        Text("ECHO VOICE TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("AI", "RECORDED", "SYSTEM").forEach { vt ->
                                FilterChip(
                                    selected = formVoiceType == vt,
                                    onClick = { formVoiceType = vt },
                                    label = { Text(vt, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f).testTag("voice_chip_$vt")
                                )
                            }
                        }
                    }

                    // Dynamic Subforms based on Voice Trigger type
                    item {
                        AnimatedVisibility(visible = formVoiceType == "AI") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("AI Voice Generator Script", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                OutlinedTextField(
                                    value = formVoiceText,
                                    onValueChange = { formVoiceText = it },
                                    label = { Text("Typed Voice message") },
                                    modifier = Modifier.fillMaxWidth().testTag("ai_voice_message_input")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Custom Language selection
                                    var langExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { langExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(formVoiceLanguage, fontSize = 10.sp, maxLines = 1)
                                        }
                                        DropdownMenu(
                                            expanded = langExpanded,
                                            onDismissRequest = { langExpanded = false }
                                        ) {
                                            listOf("English", "Urdu", "Hindi", "Arabic").forEach { lang ->
                                                DropdownMenuItem(
                                                    text = { Text(lang) },
                                                    onClick = {
                                                        formVoiceLanguage = lang
                                                        langExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Voice style selection
                                    var styleExpanded by remember { mutableStateOf(false) }
                                    Box(modifier = Modifier.weight(1f)) {
                                        OutlinedButton(
                                            onClick = { styleExpanded = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(formVoiceStyle, fontSize = 10.sp, maxLines = 1)
                                        }
                                        DropdownMenu(
                                            expanded = styleExpanded,
                                            onDismissRequest = { styleExpanded = false }
                                        ) {
                                            listOf("Male", "Female", "Robotic", "Motivational Coach", "Celebrity").forEach { vstyle ->
                                                DropdownMenuItem(
                                                    text = { Text(vstyle) },
                                                    onClick = {
                                                        formVoiceStyle = vstyle
                                                        styleExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.convertTextToAIStyle(formVoiceText, formVoiceLanguage, formVoiceStyle) },
                                    colors = ButtonDefaults.filledTonalButtonColors(),
                                    modifier = Modifier.fillMaxWidth().testTag("preview_ai_speech_button")
                                ) {
                                    Icon(Icons.Default.Hearing, "Preview")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Preview AI Voice Tone", fontSize = 12.sp)
                                }
                            }
                        }

                        AnimatedVisibility(visible = formVoiceType == "RECORDED") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("In-App Voice Recorder", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                                if (voiceNotes.isEmpty()) {
                                    Text(
                                        text = "No recorded voice clips found.\nRecord below to create your first clip!",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    // List existing voice clips
                                    Text("Captured Audio Library:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        voiceNotes.forEach { note ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (formVoiceFilePath == note.filePath) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                                    )
                                                    .clickable { formVoiceFilePath = note.filePath }
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (formVoiceFilePath == note.filePath) Icons.Default.CheckCircle else Icons.Default.Mic,
                                                        contentDescription = "Active",
                                                        tint = if (formVoiceFilePath == note.filePath) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                                    )
                                                    Text(note.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    // Preview play
                                                    IconButton(
                                                        onClick = {
                                                            if (isPlayingPreview) {
                                                                viewModel.stopRecordedVoicePreview()
                                                            } else {
                                                                viewModel.playRecordedVoicePreview(note.filePath)
                                                            }
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                            contentDescription = "Preview"
                                                        )
                                                    }
                                                    // Delete record
                                                    IconButton(
                                                        onClick = { viewModel.deleteVoiceNote(note) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // MIC recorder controls
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Direct Studio Recording", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        OutlinedTextField(
                                            value = micRecordTitle,
                                            onValueChange = { micRecordTitle = it },
                                            placeholder = { Text("E.g., University Wakeup message...") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        ) {
                                            if (isRecording) {
                                                Icon(
                                                    Icons.Default.Mic,
                                                    "Recording",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text("Recording: $recordingDuration s", color = Color.Red, fontWeight = FontWeight.Black)
                                                Button(
                                                    onClick = {
                                                        viewModel.stopRecordingCustomVoice(micRecordTitle)
                                                        micRecordTitle = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                                ) {
                                                    Text("Stop & Save")
                                                }
                                            } else {
                                                Button(
                                                    onClick = { viewModel.startRecordingCustomVoice() }
                                                ) {
                                                    Icon(Icons.Default.Mic, "Record")
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Start Recording")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Smart Wake-up Challenge selection
                    item {
                        Text("SMART WAKE UP CHALLENGE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        val challengesList = listOf(
                            "NONE" to "Direct Dismiss",
                            "MATH" to "Solve Math Equations",
                            "SHAKE" to "Shake Phone Accelerometer",
                            "MEMORY" to "Simon Sequence Puzzle",
                            "TYPING" to "Typing Quick Quotes",
                            "CAPTCHA" to "Security Verification Word"
                        )
                        var chalExpanded by remember { mutableStateOf(false) }
                        
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            OutlinedButton(
                                onClick = { chalExpanded = true },
                                modifier = Modifier.fillMaxWidth().testTag("challenge_selector_button")
                            ) {
                                Text("Wake Up Task: ${challengesList.find { it.first == formChallengeType }?.second}")
                            }
                            DropdownMenu(
                                expanded = chalExpanded,
                                onDismissRequest = { chalExpanded = false }
                            ) {
                                challengesList.forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = { Text(name) },
                                        onClick = {
                                            formChallengeType = code
                                            chalExpanded = false
                                        },
                                        modifier = Modifier.testTag("challenge_choice_$code")
                                    )
                                }
                            }
                        }
                    }

                    // Gradual rise and custom snooze
                    item {
                        Text("GENTLE WAKE OPTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Gradual Volume Rise (Fade-in)", fontSize = 13.sp)
                            Switch(
                                checked = formIsGradualVolume,
                                onCheckedChange = { formIsGradualVolume = it }
                            )
                        }

                        OutlinedTextField(
                            value = formSnoozeMinutes.toString(),
                            onValueChange = { formSnoozeMinutes = it.toIntOrNull() ?: 5 },
                            label = { Text("Snooze Duration (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        )
    }
}

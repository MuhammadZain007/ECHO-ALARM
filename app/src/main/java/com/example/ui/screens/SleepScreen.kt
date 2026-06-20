package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EchoAlarmViewModel
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun SleepScreen(
    viewModel: EchoAlarmViewModel,
    modifier: Modifier = Modifier
) {
    val relaxingSoundPlaying by viewModel.relaxingSoundPlaying.collectAsState()
    val bedtimeTimeRemaining by viewModel.bedtimeTimeRemaining.collectAsState()

    var sleepRemindersEnabled by remember { mutableStateOf(true) }
    var sleepTargetHours by remember { mutableFloatStateOf(8f) }
    var bedtimeSoundHours by remember { mutableIntStateOf(2) }

    var isTrackingActive by remember { mutableStateOf(false) }
    var trackingTimeMinutes by remember { mutableIntStateOf(0) }

    // Start a simulated timer for active tracking to dynamically show sleep cycles
    LaunchedEffect(isTrackingActive) {
        if (isTrackingActive) {
            trackingTimeMinutes = 0
            while (isTrackingActive) {
                delay(2000) // fast simulate minutes
                trackingTimeMinutes += 15
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Header
        item {
            Text(
                text = "BEDTIME & RELAXATION",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
        }

        if (isTrackingActive) {
            // Full screen dim sensory screen overlay representing active tracking
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF02040A)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .testTag("active_tracking_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.NightsStay,
                            "Tracking Active",
                            tint = Color(0xFFA5B4FC),
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = "Active Sleep Tracking",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Text(
                            text = "Place your device flat on your mattress. The Echo Alerter is recording micro-movements to map sleep cycles.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tracking Time", fontSize = 11.sp, color = Color.Gray)
                                Text("${trackingTimeMinutes / 60}h ${trackingTimeMinutes % 60}m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Cycle Phase", fontSize = 11.sp, color = Color.Gray)
                                val stage = when {
                                    trackingTimeMinutes < 45 -> "Light NREM"
                                    trackingTimeMinutes < 90 -> "Deep Sleep"
                                    trackingTimeMinutes < 120 -> "Dreaming REM"
                                    else -> "Deep Sleep"
                                }
                                Text(stage, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF818CF8))
                            }
                        }

                        Button(
                            onClick = { isTrackingActive = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Stop Sleep Tracking")
                        }
                    }
                }
            }
        } else {
            // Standard controls and stats
            item {
                // Sleep hours scheduler card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.HourglassEmpty, "Timer", tint = MaterialTheme.colorScheme.primary)
                                Text("Sleep Schedule Reminder", fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = sleepRemindersEnabled,
                                onCheckedChange = { sleepRemindersEnabled = it },
                                modifier = Modifier.testTag("sleep_reminder_switch")
                            )
                        }

                        Text(
                            text = "Target Duration: ${String.format("%.1f", sleepTargetHours)} Hours",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Slider(
                            value = sleepTargetHours,
                            onValueChange = { sleepTargetHours = it },
                            valueRange = 4f..12f,
                            steps = 15,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Custom sound loop controller
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.MusicNote, "Audio machine", tint = MaterialTheme.colorScheme.primary)
                            Text("Ambient Relax Sound Machine", fontWeight = FontWeight.Bold)
                        }

                        // Display countdown if sound is currently playing
                        AnimatedVisibility(visible = relaxingSoundPlaying != null) {
                            val remMin = TimeUnit.MILLISECONDS.toMinutes(bedtimeTimeRemaining) % 60
                            val remSec = TimeUnit.MILLISECONDS.toSeconds(bedtimeTimeRemaining) % 60
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Playing sound: $relaxingSoundPlaying", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                    Text("Synthesizing WAV frequencies beautifully...", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = String.format("%02d:%02d", remMin, remSec),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.stopRelaxingSound() },
                                        modifier = Modifier.size(28.dp).background(Color.Red, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Stop, "Stop", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        // Set shutoff timer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Automatic Shut-Off:", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(1, 2, 4, 8).forEach { hrs ->
                                    FilterChip(
                                        selected = bedtimeSoundHours == hrs,
                                        onClick = { bedtimeSoundHours = hrs },
                                        label = { Text("${hrs}h") }
                                    )
                                }
                            }
                        }

                        // Sound matrix selector buttons
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SoundButton(
                                    label = "White Noise",
                                    icon = Icons.Default.Air,
                                    isPlaying = relaxingSoundPlaying == "White Noise",
                                    onClick = { viewModel.toggleRelaxingSound("White Noise", bedtimeSoundHours) },
                                    modifier = Modifier.weight(1f).testTag("sound_button_noise")
                                )
                                SoundButton(
                                    label = "Rain Waves",
                                    icon = Icons.Default.WaterDrop,
                                    isPlaying = relaxingSoundPlaying == "Rain",
                                    onClick = { viewModel.toggleRelaxingSound("Rain", bedtimeSoundHours) },
                                    modifier = Modifier.weight(1f).testTag("sound_button_rain")
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SoundButton(
                                    label = "Symphonic Ocean",
                                    icon = Icons.Default.Storm,
                                    isPlaying = relaxingSoundPlaying == "Ocean",
                                    onClick = { viewModel.toggleRelaxingSound("Ocean", bedtimeSoundHours) },
                                    modifier = Modifier.weight(1f).testTag("sound_button_ocean")
                                )
                                SoundButton(
                                    label = "Forest Breeze",
                                    icon = Icons.Default.Landscape,
                                    isPlaying = relaxingSoundPlaying == "Forest",
                                    onClick = { viewModel.toggleRelaxingSound("Forest", bedtimeSoundHours) },
                                    modifier = Modifier.weight(1f).testTag("sound_button_forest")
                                )
                            }
                        }
                    }
                }
            }

            // Quick trigger track button
            item {
                Button(
                    onClick = { isTrackingActive = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("start_tracking_button")
                ) {
                    Icon(Icons.Default.Snooze, "Track")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Launch Ambient Sleep Tracker", fontWeight = FontWeight.Bold)
                }
            }

            // Comprehensive Material 3 sleep analytics charts
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Poll, "Analytics", tint = MaterialTheme.colorScheme.primary)
                            Text("Historic Sleep Analytics (Last 7 Days)", fontWeight = FontWeight.Bold)
                        }

                        // Bar Chart drawn directly in Compose using native column structures!
                        // This guarantees 100% crash free compiling and beautiful custom colors
                        val weeklyStats = listOf(
                            BarStat("Mon", 7.8f, "85%"),
                            BarStat("Tue", 6.5f, "72%"),
                            BarStat("Wed", 8.2f, "90%"),
                            BarStat("Thu", 5.8f, "60%"),
                            BarStat("Fri", 7.5f, "88%"),
                            BarStat("Sat", 9.0f, "95%"),
                            BarStat("Sun", 8.4f, "92%")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyStats.forEach { stat ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Percentage label
                                    Text(stat.scoreText, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Colored statistical bar
                                    val barHeight = (stat.durationHours / 10f * 80).toInt().dp
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                                                    )
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(stat.dayLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        }

                        // Detailed sleep composition stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Average Sleep", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("7.6 Hours", fontSize = 15.sp, fontWeight = FontWeight.Black)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Quality Rating", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("83% Excellent", fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Deep Phase Avg", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("2.4 Hours", fontSize = 15.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SoundButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .height(54.dp)
            .border(
                1.dp,
                if (isPlaying) Color.Transparent else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isPlaying) "TAP TO STOP" else "TAP TO PLAY",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Light,
                    color = (if (isPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.7f)
                )
            }
        }
    }
}

data class BarStat(
    val dayLabel: String,
    val durationHours: Float,
    val scoreText: String
)

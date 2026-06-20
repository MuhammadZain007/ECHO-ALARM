package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EchoAlarmViewModel
import com.example.ui.components.AnimatedClock
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: EchoAlarmViewModel,
    onNavigateToAlarms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.alarms.collectAsState()
    val weatherTemp by viewModel.weatherTemp.collectAsState()
    val weatherHumidity by viewModel.weatherHumidity.collectAsState()
    val weatherCondition by viewModel.weatherCondition.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()

    val nextAlarm = remember(alarms) {
        alarms.filter { it.isEnabled }
            .minByOrNull { alarm ->
                // Calculate closest next occurrence
                alarm.hour * 60 + alarm.minute
            }
    }

    val dateStr = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Echo Alarm Header Bar
        Text(
            text = "ECHO ALARM",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Large 15-Theme Configurable Animation Clock
        AnimatedClock(
            themeName = currentTheme,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Glassmorphic Weather Panel & Next Alarm Panel Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Weather Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .testTag("weather_indicator_card")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (weatherCondition) {
                                "Bright Sunny" -> Icons.Default.WbSunny
                                "Sunny Fog" -> Icons.Default.BlurOn
                                "Sunset Amber" -> Icons.Default.WbTwilight
                                else -> Icons.Default.NightsStay
                            },
                            contentDescription = "Weather",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Weather",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Text(
                        text = "$weatherTemp°C",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "$weatherCondition\nHumidity: $weatherHumidity%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }

            // Next Alarm Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToAlarms() }
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .testTag("next_alarm_card")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Upcoming",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Next Alarm",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    if (nextAlarm != null) {
                        Text(
                            text = nextAlarm.getFormattedTime(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${nextAlarm.label}\n${nextAlarm.getRepeatDaysShortText()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    } else {
                        Text(
                            text = "--:--",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            text = "No active alarms\nTap to create one",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Quick Alarm Presets panel
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "⚡ QUICK PRESETS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(5, 10, 30, 60).forEach { mins ->
                    val presetLabel = when (mins) {
                        60 -> "1 Hour"
                        else -> "$mins Mins"
                    }
                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.MINUTE, mins)
                            val targetHour = calendar.get(Calendar.HOUR_OF_DAY)
                            val targetMin = calendar.get(Calendar.MINUTE)
                            
                            viewModel.addAlarm(
                                hour = targetHour,
                                minute = targetMin,
                                label = "Quick Preset $presetLabel",
                                repeatDays = emptyList(),
                                voiceType = "SYSTEM",
                                voiceText = "Time to wake up! Your quick $presetLabel countdown is finished.",
                                themeName = currentTheme
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = presetLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

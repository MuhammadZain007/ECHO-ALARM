package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EchoAlarmViewModel
import java.util.*
import kotlin.math.sin

@Composable
fun RingerOverlay(
    viewModel: EchoAlarmViewModel,
    modifier: Modifier = Modifier
) {
    val activeAlarm by viewModel.activeRingerAlarm.collectAsState()
    val ringerVolume by viewModel.ringerVolume.collectAsState()

    // 1. Challenges states
    val mathQuestion by viewModel.challengeMathQuestion.collectAsState()
    var mathInput by remember { mutableStateOf("") }

    val shakeCount by viewModel.challengeShakeCount.collectAsState()

    val memorySequence by viewModel.challengeMemorySequence.collectAsState()
    val memoryUserSequence by viewModel.challengeMemoryUserSequence.collectAsState()

    val typingPrompt by viewModel.challengeTypingPrompt.collectAsState()
    var typingInput by remember { mutableStateOf("") }

    val captchaPrompt by viewModel.challengeCaptchaPrompt.collectAsState()
    var captchaInput by remember { mutableStateOf("") }

    // 2. Sunrise Simulation Background Pulsing Transition
    val infiniteTransition = rememberInfiniteTransition(label = "sunrise")
    val sunriseStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radialShift"
    )

    // Blend warm morning glow colors (Amber, Deep Crimson, Gold sunbursts)
    val sunriseGlow = Brush.radialGradient(
        colors = listOf(
            Color(0xFFFBBF24).copy(alpha = 0.5f + (sunriseStep * 0.3f)), // Golden sun
            Color(0xFFF59E0B).copy(alpha = 0.4f),
            Color(0xFFDC2626).copy(alpha = 0.2f),
            Color.Black // Deep night fading out
        ),
        center = Offset.Zero,
        radius = 500f + (sunriseStep * 300f)
    )

    if (activeAlarm != null) {
        val alarm = activeAlarm!!
        
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black) // Dark base
                .background(sunriseGlow) // Overlay rich sunburst simulation
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Glow Header Info
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 28.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Sunrise simulation active",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "SUNRISE ALARM TRIGGERED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFBBF24),
                            letterSpacing = 2.sp
                        )
                    }

                    Text(
                        text = alarm.getFormattedTime(),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Text(
                        text = alarm.label,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CHALLENGE MODE PANEL
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xF20F111A)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title of Task to execute
                        Text(
                            text = when (alarm.challengeType) {
                                "MATH" -> "⚡ TASK: UNLOCK EQUATION"
                                "SHAKE" -> "⚡ TASK: ACCELEROMETER SHAKE"
                                "MEMORY" -> "⚡ TASK: REMEMBER SEQUENCE"
                                "TYPING" -> "⚡ TASK: TYPING SPEED"
                                "CAPTCHA" -> "⚡ TASK: CAPTCHA SECURITY CODE"
                                else -> "⚡ SIMULATED WAKE UP"
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        // Particular challenge layouts
                        when (alarm.challengeType) {
                            "MATH" -> {
                                Text(
                                    text = "Solve: $mathQuestion",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                OutlinedTextField(
                                    value = mathInput,
                                    onValueChange = { mathInput = it },
                                    label = { Text("Solve calculations...") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.Gray
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("math_challenge_input")
                                )
                                Button(
                                    onClick = {
                                        val subVal = mathInput.toIntOrNull() ?: 0
                                        if (viewModel.submitChallengeMath(subVal)) {
                                            mathInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("submit_math_button")
                                ) {
                                    Text("Verify & Dismiss")
                                }
                            }
                            "SHAKE" -> {
                                Icon(
                                    Icons.Default.PhoneAndroid,
                                    "Shake",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .offset(y = if (shakeCount % 2 == 0) (-8).dp else 8.dp) // bouncy simulation!
                                )
                                Text(
                                    text = "Shakes Counted: $shakeCount / 15",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                LinearProgressIndicator(
                                    progress = { shakeCount / 15f },
                                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))
                                )
                                Button(
                                    onClick = { viewModel.incrementShakeChallenge() },
                                    modifier = Modifier.fillMaxWidth().testTag("shake_simulation_button")
                                ) {
                                    Text("👋 SHAKE PHONE (SIMULATE)")
                                }
                            }
                            "MEMORY" -> {
                                Text(
                                    text = "Replicate sequence correctly!",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                                // Render simple memory sequence tracker light indicators
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    memorySequence.forEachIndexed { index, pos ->
                                        val lit = memoryUserSequence.size > index && memoryUserSequence[index] == pos
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (lit) MaterialTheme.colorScheme.primary
                                                    else Color.DarkGray
                                                )
                                        )
                                    }
                                }

                                // Interactive Simon Grid (3x2 puzzle buttons)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for (row in 0 until 2) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            for (col in 1..3) {
                                                val idVal = row * 3 + col
                                                val tapped = memoryUserSequence.contains(idVal)
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            if (tapped) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                            else MaterialTheme.colorScheme.secondaryContainer
                                                        )
                                                        .border(
                                                            1.5.dp,
                                                            if (tapped) MaterialTheme.colorScheme.primary else Color.Gray,
                                                            RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { viewModel.submitMemorySequence(idVal) }
                                                        .testTag("memory_grid_button_$idVal"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        idVal.toString(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "TYPING" -> {
                                Text(
                                    text = "\"$typingPrompt\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5B4FC),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                OutlinedTextField(
                                    value = typingInput,
                                    onValueChange = { typingInput = it },
                                    label = { Text("Retype prompt exactly...") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("typing_challenge_input")
                                )
                                Button(
                                    onClick = {
                                        if (viewModel.submitTypingChallenge(typingInput)) {
                                            typingInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("submit_typing_button")
                                ) {
                                    Text("Verify & Dismiss")
                                }
                            }
                            "CAPTCHA" -> {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.15f))
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = captchaPrompt,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFF87171),
                                        fontFamily = FontFamily.Serif,
                                        letterSpacing = 4.sp
                                    )
                                }
                                OutlinedTextField(
                                    value = captchaInput,
                                    onValueChange = { captchaInput = it },
                                    label = { Text("Spell verification digits") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("captcha_challenge_input")
                                )
                                Button(
                                    onClick = {
                                        if (viewModel.submitCaptchaChallenge(captchaInput)) {
                                            captchaInput = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("submit_captcha_button")
                                ) {
                                    Text("Verify & Dismiss")
                                }
                            }
                            else -> {
                                Text(
                                    "No challenges active.",
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                                Button(
                                    onClick = { viewModel.dismissActiveRinger() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth().testTag("standard_dismiss_button")
                                ) {
                                    Icon(Icons.Default.AlarmOff, "Dismiss")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Double Tap to Dismiss")
                                }
                            }
                        }
                    }
                }

                // Volume gradual increase indicator bar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Ringer volume fade-in progress: ${(ringerVolume * 100).toInt()}%",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                    LinearProgressIndicator(
                        progress = { ringerVolume },
                        color = Color(0xFFFBBF24),
                        trackColor = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }

                // SNOOZE ACTION BUTTONS Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.snoozeActiveRinger() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray, contentColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .testTag("snooze_alarm_button")
                    ) {
                        Icon(Icons.Default.Snooze, "Snooze")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Snooze (${viewModel.snoozeMinutes.value}m)")
                    }

                    // Standard direct bypass cheat if they want to bypass during debugging
                    TextButton(
                        onClick = { viewModel.dismissActiveRinger() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("Bypass Task", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

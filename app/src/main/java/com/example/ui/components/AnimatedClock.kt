package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnimatedClock(
    themeName: String,
    modifier: Modifier = Modifier
) {
    // Dynamic time tracking state updated continuously
    var timeCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            timeCalendar = Calendar.getInstance()
            delay(30) // Rapid smooth sub-second updates for fluid sweeping hands
        }
    }

    val hour = timeCalendar.get(Calendar.HOUR)
    val minute = timeCalendar.get(Calendar.MINUTE)
    val second = timeCalendar.get(Calendar.SECOND)
    val millisecond = timeCalendar.get(Calendar.MILLISECOND)

    // Calculate exact float values of hands for a fluid sweeping motion
    val secondsWithMs = second + (millisecond / 1000f)
    val minutesWithSec = minute + (secondsWithMs / 60f)
    val hoursWithMin = hour + (minutesWithSec / 60f)

    // Check if the current theme represents an Analog or Digital Face
    val isAnalog = when (themeName) {
        "Luxury Gold", "Modern Steel", "Wooden Clock", "Futuristic Clock" -> true
        else -> false
    }

    // Infinite transitions for orbiting stars, glowing mesh, or radioactive particles
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFactor by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating backing lights for futuristic or space themes
        val backdropColors = getThemeBackdrops(themeName)
        if (backdropColors.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = backdropColors,
                            center = Offset.Zero,
                            radius = 350f * pulseFactor
                        ),
                        alpha = 0.15f
                    )
            )
        }

        if (isAnalog) {
            AnalogClockFace(
                themeName = themeName,
                hours = hoursWithMin,
                minutes = minutesWithSec,
                seconds = secondsWithMs,
                floatingOffset = floatingOffset,
                modifier = Modifier.size(250.dp)
            )
        } else {
            DigitalClockFace(
                themeName = themeName,
                timeCalendar = timeCalendar,
                pulseFactor = pulseFactor,
                floatingOffset = floatingOffset
            )
        }
    }
}

@Composable
fun AnalogClockFace(
    themeName: String,
    hours: Float,
    minutes: Float,
    seconds: Float,
    floatingOffset: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.width / 2

        // 1. Draw Theme Background Texture / Rim
        when (themeName) {
            "Luxury Gold" -> {
                // Shiny golden dual rims
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(Color(0xFFE5C060), Color(0xFFD4AF37), Color(0xFF8B7322), Color(0xFFE5C060))
                    ),
                    radius = radius,
                    style = Stroke(width = 12f)
                )
                drawCircle(
                    color = Color(0x33E5C060),
                    radius = radius - 8f
                )
            }
            "Modern Steel" -> {
                // Riveted gunmetal slate rim
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF334155), Color(0xFF64748B), Color(0xFF0F172A))
                    ),
                    radius = radius,
                    style = Stroke(width = 16f)
                )
                // Metal ticks
                for (angle in 0 until 360 step 30) {
                    val angleRad = angle * PI / 180f
                    val outerX = centerX + (radius - 24f) * sin(angleRad).toFloat()
                    val outerY = centerY - (radius - 24f) * cos(angleRad).toFloat()
                    val innerX = centerX + (radius - 40f) * sin(angleRad).toFloat()
                    val innerY = centerY - (radius - 40f) * cos(angleRad).toFloat()
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(innerX, innerY),
                        end = Offset(outerX, outerY),
                        strokeWidth = 6f
                    )
                }
            }
            "Wooden Clock" -> {
                // Concentric wood ring slices
                drawCircle(
                    color = Color(0xFF472808),
                    radius = radius,
                    style = Stroke(width = 14f)
                )
                for (r in (radius.toInt() - 25) downTo 20 step 30) {
                    drawCircle(
                        color = Color(0x117F4F24),
                        radius = r.toFloat(),
                        style = Stroke(width = 2f)
                    )
                }
            }
            "Futuristic Clock" -> {
                // Quantum hologram laser mesh
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00FFE0).copy(alpha = 0.3f), Color.Transparent)
                    ),
                    radius = radius
                )
                drawRect(
                    color = Color(0x2200FFE0),
                    topLeft = Offset(centerX - radius + 10f, centerY - 1f),
                    size = Size(radius * 2 - 20f, 2f)
                )
                drawRect(
                    color = Color(0x2200FFE0),
                    topLeft = Offset(centerX - 1f, centerY - radius + 10f),
                    size = Size(2f, radius * 2 - 20f)
                )
                drawCircle(
                    color = Color(0xFF00FFE0),
                    radius = radius - 4f,
                    style = Stroke(width = 4f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f))
                )
            }
        }

        // 2. Draw standard dial ticks for reference
        if (themeName != "Modern Steel") {
            for (i in 0 until 12) {
                val angleRad = (i * 30f) * PI / 180f
                val length = if (i % 3 == 0) 25f else 12f
                val weight = if (i % 3 == 0) 6f else 3f
                val startX = centerX + (radius - 12f - length) * sin(angleRad).toFloat()
                val startY = centerY - (radius - 12f - length) * cos(angleRad).toFloat()
                val endX = centerX + (radius - 12f) * sin(angleRad).toFloat()
                val endY = centerY - (radius - 12f) * cos(angleRad).toFloat()

                drawLine(
                    color = primaryColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = weight,
                    cap = StrokeCap.Round
                )
            }
        }

        // 3. Draw Hour Hand
        val hourAngle = (hours * 30) * PI / 180f
        val hourHandLength = radius * 0.5f
        val hourEndX = centerX + hourHandLength * sin(hourAngle).toFloat()
        val hourEndY = centerY - hourHandLength * cos(hourAngle).toFloat()
        drawLine(
            color = primaryColor,
            start = Offset(centerX, centerY),
            end = Offset(hourEndX, hourEndY),
            strokeWidth = 14f,
            cap = StrokeCap.Round
        )

        // 4. Draw Minute Hand
        val minAngle = (minutes * 6) * PI / 180f
        val minHandLength = radius * 0.72f
        val minEndX = centerX + minHandLength * sin(minAngle).toFloat()
        val minEndY = centerY - minHandLength * cos(minAngle).toFloat()
        drawLine(
            color = secondaryColor,
            start = Offset(centerX, centerY),
            end = Offset(minEndX, minEndY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )

        // 5. Draw Sweeping Second Hand with offset trail
        val secAngle = (seconds * 6) * PI / 180f
        val secHandLength = radius * 0.88f
        val secEndX = centerX + secHandLength * sin(secAngle).toFloat()
        val secEndY = centerY - secHandLength * cos(secAngle).toFloat()
        
        // Draw trailing ghost wave
        drawLine(
            color = tertiaryColor.copy(alpha = 0.25f),
            start = Offset(centerX, centerY),
            end = Offset(
                centerX + secHandLength * sin(secAngle - 0.08f).toFloat(),
                centerY - secHandLength * cos(secAngle - 0.08f).toFloat()
            ),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // Actual second needle
        drawLine(
            color = tertiaryColor,
            start = Offset(centerX, centerY),
            end = Offset(secEndX, secEndY),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )

        // Floating particle spark at tip of second hand
        drawCircle(
            color = tertiaryColor,
            radius = 8f + (floatingOffset / 6f),
            center = Offset(secEndX, secEndY)
        )

        // 6. Polished Center Cap Pin
        drawCircle(
            color = Color.White,
            radius = 10f
        )
        drawCircle(
            color = primaryColor,
            radius = 6f
        )
    }
}

@Composable
fun DigitalClockFace(
    themeName: String,
    timeCalendar: Calendar,
    pulseFactor: Float,
    floatingOffset: Float
) {
    val currentHour = timeCalendar.get(Calendar.HOUR_OF_DAY)
    val curMin = timeCalendar.get(Calendar.MINUTE)
    val curSec = timeCalendar.get(Calendar.SECOND)

    val timeDisplay = String.format("%02d:%02d", currentHour, curMin)
    val secondsDisplay = String.format("%02d", curSec)

    // Compute styles based on thematic criteria
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    if (themeName == "Bold Typography") {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.wrapContentSize()
            ) {
                // Large White Hour & Minute text
                Text(
                    text = timeDisplay,
                    color = Color.White,
                    fontSize = 100.sp, // beautiful giant text matching the font-black look
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-4).sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color(0xFFD0BCFF).copy(alpha = 0.6f),
                            offset = Offset.Zero,
                            blurRadius = 35f
                        )
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // AM / PM floating indicator to the right/bottom
                val amPmStr = if (currentHour >= 12) "PM" else "AM"
                Text(
                    text = amPmStr,
                    color = Color(0xFFD0BCFF),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .offset(x = 38.dp, y = (-8).dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Thematic "AI VOICE ACTIVE" badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color(0xFF1C1B1F), androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                    .border(1.dp, Color(0xFF49454F), androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                // Neon blinking dot
                val alphaDot = remember { Animatable(1f) }
                LaunchedEffect(Unit) {
                    while (true) {
                        alphaDot.animateTo(0.3f, animationSpec = tween(1000, easing = LinearEasing))
                        alphaDot.animateTo(1f, animationSpec = tween(1000, easing = LinearEasing))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF00FF00).copy(alpha = alphaDot.value), androidx.compose.foundation.shape.RoundedCornerShape(50.dp))
                )
                Text(
                    text = "AI VOICE ACTIVE",
                    color = Color(0xFFE6E1E5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    } else {
        val typeface = when (themeName) {
            "Neon Cyberpunk" -> FontFamily.Serif             // Razor sharp digital monospace look
            "Matrix Green" -> FontFamily.Monospace          // Classical green terminal matrix
            "Gaming Theme" -> FontFamily.SansSerif
            "Space Theme" -> FontFamily.Default
            else -> FontFamily.Default
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Main Digit Display
                Text(
                    text = timeDisplay,
                    color = primaryColor,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = typeface,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                    // Subtle matrix text-shadow simulation can also be customized inside style
                )

                // Sweeping active blink indicators and seconds counter offset
                Text(
                    text = secondsDisplay,
                    color = secondaryColor.copy(alpha = 0.85f),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = typeface,
                    modifier = Modifier
                        .offset(x = 36.dp, y = (-12).dp)
                        .background(Color.Transparent)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Small thematic message/status line
            val statusText = when (themeName) {
                "Neon Cyberpunk" -> "ECHO_NET RUNNING // SYNC_OK"
                "Matrix Green" -> "FOLLOW_THE_WHITE_RABBIT >"
                "Glassmorphism" -> "Crystal Clarity Enabled"
                "AMOLED Black" -> "Amoled Power Save Mode"
                "Space Theme" -> "Deep space telemetry online"
                "Galaxy Theme" -> "Stardust Orbit Status: Latent"
                "Nature Theme" -> "Clean breeze index: 96%"
                "Anime Theme" -> "Sakura blossoms count: 104"
                "Gaming Theme" -> "MAX PERF // BOOST ON"
                else -> "Echo Chronos Sync V1"
            }

            Text(
                text = statusText,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp,
                modifier = Modifier.offset(y = floatingOffset.dp / 2)
            )
        }
    }
}

fun getThemeBackdrops(themeName: String): List<Color> {
    return when (themeName) {
        "Neon Cyberpunk" -> listOf(CyberpunkPrimary, CyberpunkSecondary)
        "Matrix Green" -> listOf(MatrixPrimary, MatrixSecondary)
        "Glassmorphism" -> listOf(GlassPrimary, GlassAccent)
        "Luxury Gold" -> listOf(GoldPrimary, GoldAccent)
        "Futuristic Clock" -> listOf(FuturePrimary, FutureSecondary)
        "Galaxy Theme" -> listOf(GalaxyPrimary, GalaxySecondary)
        "Gaming Theme" -> listOf(GamingPrimary, GamingSecondary)
        else -> emptyList()
    }
}

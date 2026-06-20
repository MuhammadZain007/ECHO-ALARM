package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "Alarm",
    val repeatDays: String = "", // Comma-separated list e.g., "1,2,3,4,5" (1=Mon, 7=Sun), empty = one-time
    val snoozeDurationMinutes: Int = 5,
    val voiceType: String = "AI", // "AI", "RECORDED", "SYSTEM", "PRESET"
    val voiceText: String = "Rise and shine! A beautiful day is waiting for you.", // Custom text for AI Voice
    val voiceLanguage: String = "English", // "English", "Urdu", "Hindi", "Arabic"
    val voiceStyle: String = "Motivational Coach", // "Male", "Female", "Robotic", "Motivational Coach", "Executive"
    val voiceFilePath: String? = null, // Recording local path or uploaded file
    val challengeType: String = "NONE", // "NONE", "MATH", "SHAKE", "MEMORY", "TYPING", "CAPTCHA"
    val challengeDifficulty: String = "MEDIUM", // "EASY", "MEDIUM", "HARD"
    val isGradualVolume: Boolean = true,
    val isGradualVibration: Boolean = true,
    val isSunriseSimulation: Boolean = true,
    val alarmSoundUri: String = "notification_default", // fallback or default alarm audio ringtone
    val themeName: String = "Bold Typography"
) {
    fun getRepeatDaysList(): List<Int> {
        if (repeatDays.isBlank()) return emptyList()
        return repeatDays.split(",").mapNotNull { it.trim().toIntOrNull() }
    }
    
    fun isRepeating(): Boolean = repeatDays.isNotBlank()
    
    fun getFormattedTime(): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format("%02d:%02d %s", displayHour, minute, amPm)
    }

    fun getRepeatDaysShortText(): String {
        if (!isRepeating()) return "Once"
        val days = getRepeatDaysList()
        if (days.size == 7) return "Daily"
        if (days.size == 5 && !days.contains(6) && !days.contains(7)) return "Weekdays"
        if (days.size == 2 && days.contains(6) && days.contains(7)) return "Weekends"
        
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return days.sorted().joinToString(", ") { day ->
            if (day in 1..7) dayNames[day - 1] else ""
        }
    }
}

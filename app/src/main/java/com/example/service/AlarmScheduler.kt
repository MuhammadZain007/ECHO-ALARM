package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.Alarm
import java.util.Calendar

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun schedule(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if we can schedule exact alarms
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms. Falling back to inexact.")
                scheduleInexact(context, alarm, alarmManager)
                return
            }
        }
        
        scheduleExact(context, alarm, alarmManager)
    }

    private fun scheduleExact(context: Context, alarm: Alarm, alarmManager: AlarmManager) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateTriggerTime(alarm)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
        Log.d(TAG, "Scheduled alarm ${alarm.id} for time: $triggerTime")
    }

    private fun scheduleInexact(context: Context, alarm: Alarm, alarmManager: AlarmManager) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = calculateTriggerTime(alarm)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    fun cancel(context: Context, alarm: Alarm) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Canceled alarm ${alarm.id}")
        }
    }

    private fun calculateTriggerTime(alarm: Alarm): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()
        
        if (alarm.isRepeating()) {
            val repeatDays = alarm.getRepeatDaysList() // e.g. 1=Mon...7=Sun
            val todayDayOfWeek = translateCalendarDayOfWeek(now.get(Calendar.DAY_OF_WEEK))
            
            // Find the closest day in repeat days
            var daysUntilTrigger = -1
            for (i in 0..7) {
                val targetDayIndex = (todayDayOfWeek + i - 1) % 7 + 1
                if (repeatDays.contains(targetDayIndex)) {
                    if (i == 0) {
                        // It's today. Check if the time has already passed
                        if (calendar.after(now)) {
                            daysUntilTrigger = 0
                            break
                        }
                    } else {
                        daysUntilTrigger = i
                        break
                    }
                }
            }
            
            // If no day is found (which shouldn't happen if isRepeating is true), default to tomorrow
            if (daysUntilTrigger == -1) {
                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, daysUntilTrigger)
            }
        } else {
            // One-time alarm
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return calendar.timeInMillis
    }

    // Convert Calendar.DAY_OF_WEEK (Sun=1, Mon=2...Sat=7) to Mon=1, Tue=2...Sun=7
    private fun translateCalendarDayOfWeek(calendarDay: Int): Int {
        return when (calendarDay) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}

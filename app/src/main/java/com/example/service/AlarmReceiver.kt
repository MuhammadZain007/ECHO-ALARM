package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        Log.d("AlarmReceiver", "Received alarm triggers! ID: $alarmId, Label: $alarmLabel")
        
        if (alarmId != -1) {
            // Start the Alarm Service as a Foreground Service to play ringtone and vibrate reliably
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_LABEL", alarmLabel)
            }
            try {
                androidx.core.content.ContextCompat.startForegroundService(context, serviceIntent)
                Log.d("AlarmReceiver", "Started AlarmService successfully.")
            } catch (e: Exception) {
                Log.e("AlarmReceiver", "Failed to start AlarmService: ${e.message}")
            }

            // Launch main activity with ringer flag
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("RINGING_ALARM_ID", alarmId)
                putExtra("RINGING_ALARM_LABEL", alarmLabel)
            }
            try {
                context.startActivity(launchIntent)
            } catch (e: Exception) {
                Log.w("AlarmReceiver", "Could not launch MainActivity directly from background: ${e.message}")
            }
            
            // Also notify any active state instance (if MainActivity is already running,
            // we can handle this via custom broadcast or single-top intents in MainActivity)
            val updateIntent = Intent("com.example.echoalarm.ALARM_TRIGGERED").apply {
                putExtra("RINGING_ALARM_ID", alarmId)
            }
            context.sendBroadcast(updateIntent)
        }
    }
}

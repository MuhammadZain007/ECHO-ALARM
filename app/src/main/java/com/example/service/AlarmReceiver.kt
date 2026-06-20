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
            // Launch main activity with ringer flag
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("RINGING_ALARM_ID", alarmId)
                putExtra("RINGING_ALARM_LABEL", alarmLabel)
            }
            context.startActivity(launchIntent)
            
            // Also notify any active state instance (if MainActivity is already running,
            // we can handle this via custom broadcast or single-top intents in MainActivity)
            val updateIntent = Intent("com.example.echoalarm.ALARM_TRIGGERED").apply {
                putExtra("RINGING_ALARM_ID", alarmId)
            }
            context.sendBroadcast(updateIntent)
        }
    }
}

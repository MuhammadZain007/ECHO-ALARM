package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object NotificationHelper {
    private const val CHANNEL_ID = "ECHO_ALARM_NOTIFICATION_CHANNEL"
    private const val CHANNEL_NAME = "Echo Alarm Notifications"
    private const val CHANNEL_DESC = "Notifications displayed when an alarm triggers"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
            Log.d("NotificationHelper", "Notification channel created successfully.")
        }
    }

    fun showAlarmNotification(context: Context, alarmId: Int, alarmLabel: String) {
        // First ensure channel is created
        createNotificationChannel(context)

        // Set up the content intent to launch MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("RINGING_ALARM_ID", alarmId)
            putExtra("RINGING_ALARM_LABEL", alarmLabel)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Echo Alarm: $alarmLabel")
            .setContentText("It is time! Tap to snooze or dismiss.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) // Makes it show up over lock screen/heads-up
            .setOngoing(true) // Keeps it from being easily swiped away

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(alarmId, builder.build())
        Log.d("NotificationHelper", "Posted notification for Alarm ID: $alarmId")
    }

    fun cancelNotification(context: Context, alarmId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(alarmId)
        Log.d("NotificationHelper", "Canceled notification for Alarm ID: $alarmId")
    }
}

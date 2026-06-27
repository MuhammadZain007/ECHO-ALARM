package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class AlarmService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getIntExtra("ALARM_ID", 9999) ?: 9999
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Echo Alarm"
        Log.d("AlarmService", "AlarmService starting for Alarm ID: $alarmId ($alarmLabel)")

        // 1. Build and promote this service to Foreground immediately
        val notification = NotificationHelper.buildAlarmNotification(this, alarmId, alarmLabel)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(alarmId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
            } else {
                startForeground(alarmId, notification)
            }
            Log.d("AlarmService", "AlarmService promoted to foreground.")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start service as Foreground: ${e.message}")
        }

        // 2. Play ringtone and vibrate
        startSoundAndVibration()

        return START_NOT_STICKY
    }

    private fun startSoundAndVibration() {
        // Play ringtone (looping)
        try {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, ringtoneUri)
                setAudioStreamType(AudioManager.STREAM_ALARM)
                isLooping = true
                prepare()
                start()
            }
            Log.d("AlarmService", "Ringtone sound playback started successfully.")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to play default alarm ringtone: ${e.message}")
            try {
                mediaPlayer = MediaPlayer.create(applicationContext, android.provider.Settings.System.DEFAULT_RINGTONE_URI)?.apply {
                    isLooping = true
                    start()
                }
                Log.d("AlarmService", "Ringtone fallback sound playback started.")
            } catch (ex: Exception) {
                Log.e("AlarmService", "Fallback ringtone failed: ${ex.message}")
            }
        }

        // Vibrate (looping pattern)
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    val pattern = longArrayOf(0, 800, 800) // Vibrate 800ms, pause 800ms
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        v.vibrate(VibrationEffect.createWaveform(pattern, 0)) // 0 means loop from index 0
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(pattern, 0)
                    }
                    Log.d("AlarmService", "Vibration started.")
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start vibrator: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmService", "AlarmService being destroyed. Cleaning up ringtone and vibrator.")
        
        // Stop sound
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to stop media player: ${e.message}")
        }
        mediaPlayer = null

        // Stop vibration
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to stop vibration: ${e.message}")
        }
        vibrator = null
    }
}

package com.example.ui

import android.app.Application
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Alarm
import com.example.data.AlarmRepository
import com.example.data.AppDatabase
import com.example.data.VoiceNote
import com.example.service.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.Calendar
import java.util.Locale
import kotlin.math.sin

class EchoAlarmViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val database = AppDatabase.getDatabase(application)
    private val repository = AlarmRepository(database.alarmDao(), database.voiceNoteDao())

    private val _currentTheme = MutableStateFlow("Bold Typography")
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    // Alarm lists direct from Room database
    val alarms: StateFlow<List<Alarm>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Voice recordings lists direct from Room database
    val voiceNotes: StateFlow<List<VoiceNote>> = repository.allVoiceNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Settings States
    val timeFormat24h = MutableStateFlow(false)
    val appLanguage = MutableStateFlow("English")

    // Active Ringer properties
    private val _activeRingerAlarm = MutableStateFlow<Alarm?>(null)
    val activeRingerAlarm: StateFlow<Alarm?> = _activeRingerAlarm.asStateFlow()
    
    // Live Ringer progress
    val ringerVolume = MutableStateFlow(0.1f)
    val ringerVibrationIntensity = MutableStateFlow(0.1f)

    // TTS engine for AI voice transformations
    private var tts: TextToSpeech? = null
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // Sound recording state
    private var mediaRecorder: MediaRecorder? = null
    private var activeRecordingFile: File? = null
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    val recordingDuration = MutableStateFlow(0)
    private var recordingTimer: CountDownTimer? = null

    // Audio player for recorded sounds previewing
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlayingPreview = MutableStateFlow(false)
    val isPlayingPreview: StateFlow<Boolean> = _isPlayingPreview.asStateFlow()

    // Bedtime Sound Synthesis system
    private val _relaxingSoundPlaying = MutableStateFlow<String?>(null) // "Rain", "Ocean", "Forest", "White Noise", or null
    val relaxingSoundPlaying: StateFlow<String?> = _relaxingSoundPlaying.asStateFlow()
    private var whiteNoisePlayer: MediaPlayer? = null
    val bedtimeTimeRemaining = MutableStateFlow<Long>(0) // in MS
    private var bedtimeTimer: CountDownTimer? = null

    // Weather Integration mock state updated periodically matching hour
    val weatherTemp = MutableStateFlow(24)
    val weatherHumidity = MutableStateFlow(62)
    val weatherCondition = MutableStateFlow("Clear Sky") // Clear Sky, Rainy, Misty, Thunderstorm, Sunny, Sunset Gloom

    // Custom simulated state for e-sports or games challenge answers
    val challengeMathResult = MutableStateFlow(0)
    val challengeMathQuestion = MutableStateFlow("")
    val challengeShakeCount = MutableStateFlow(0)
    val challengeMemorySequence = MutableStateFlow<List<Int>>(emptyList())
    val challengeMemoryUserSequence = MutableStateFlow<List<Int>>(emptyList())
    val challengeTypingPrompt = MutableStateFlow("")
    val challengeTypingInput = MutableStateFlow("")
    val challengeCaptchaPrompt = MutableStateFlow("")
    val challengeCaptchaInput = MutableStateFlow("")

    val snoozeMinutes = MutableStateFlow(5)

    init {
        // Initialize TTS
        tts = TextToSpeech(application, this)
        
        // Setup simple automatic weather simulation based on actual hour
        updateWeatherBasedOnHour()
        
        // Start simple loop to vary temperatures slightly
        viewModelScope.launch {
            while (true) {
                delay(60000)
                updateWeatherBasedOnHour()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            _isTtsReady.value = true
            Log.d("EchoAlarmVM", "TextToSpeech initialized successfully")
        } else {
            Log.e("EchoAlarmVM", "TextToSpeech initialization failed")
        }
    }

    fun setTheme(theme: String) {
        _currentTheme.value = theme
    }

    // ----------------------------------------------------
    // Weather Updates
    // ----------------------------------------------------
    private fun updateWeatherBasedOnHour() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 6..11 -> {
                weatherTemp.value = 22
                weatherHumidity.value = 75
                weatherCondition.value = "Sunny Fog"
            }
            in 12..16 -> {
                weatherTemp.value = 28
                weatherHumidity.value = 45
                weatherCondition.value = "Bright Sunny"
            }
            in 17..19 -> {
                weatherTemp.value = 24
                weatherHumidity.value = 52
                weatherCondition.value = "Sunset Amber"
            }
            in 20..23 -> {
                weatherTemp.value = 18
                weatherHumidity.value = 65
                weatherCondition.value = "Moonlit Clear"
            }
            else -> {
                weatherTemp.value = 16
                weatherHumidity.value = 88
                weatherCondition.value = "Starry Cool"
            }
        }
    }

    // ----------------------------------------------------
    // Alarm CRUD + Scheduler Commands
    // ----------------------------------------------------
    fun addAlarm(
        hour: Int, 
        minute: Int, 
        label: String, 
        repeatDays: List<Int>, 
        voiceType: String,
        voiceText: String = "",
        voiceLanguage: String = "English",
        voiceStyle: String = "Motivational Coach",
        voiceFilePath: String? = null,
        challengeType: String = "NONE",
        challengeDifficulty: String = "MEDIUM",
        isGradualVolume: Boolean = true,
        themeName: String = "Bold Typography"
    ) {
        viewModelScope.launch {
            val repeatStr = repeatDays.sorted().joinToString(",")
            val newAlarm = Alarm(
                hour = hour,
                minute = minute,
                label = label,
                repeatDays = repeatStr,
                voiceType = voiceType,
                voiceText = voiceText,
                voiceLanguage = voiceLanguage,
                voiceStyle = voiceStyle,
                voiceFilePath = voiceFilePath,
                challengeType = challengeType,
                challengeDifficulty = challengeDifficulty,
                isGradualVolume = isGradualVolume,
                themeName = themeName
            )
            val dbId = repository.insertAlarm(newAlarm)
            val alarmWithId = newAlarm.copy(id = dbId.toInt())
            if (alarmWithId.isEnabled) {
                AlarmScheduler.schedule(getApplication(), alarmWithId)
            }
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                AlarmScheduler.schedule(getApplication(), updated)
            } else {
                AlarmScheduler.cancel(getApplication(), updated)
            }
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            AlarmScheduler.cancel(getApplication(), alarm)
            repository.deleteAlarm(alarm)
        }
    }

    fun editAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.updateAlarm(alarm)
            if (alarm.isEnabled) {
                AlarmScheduler.schedule(getApplication(), alarm)
            } else {
                AlarmScheduler.cancel(getApplication(), alarm)
            }
        }
    }

    // ----------------------------------------------------
    // AI Text-to-Speech Alarm Voice Transformation (Built-in + Custom Styles)
    // ----------------------------------------------------
    fun convertTextToAIStyle(
        text: String, 
        language: String, 
        style: String, 
        previewOnly: Boolean = true
    ) {
        if (!_isTtsReady.value || tts == null) {
            Log.w("EchoAlarmVM", "TTS engine not ready yet")
            return
        }

        // Apply Language
        val locale = when (language) {
            "Urdu" -> Locale("ur")
            "Hindi" -> Locale("hi")
            "Arabic" -> Locale("ar")
            else -> Locale.US
        }
        tts?.language = locale

        // Modulate style properties (Modulating pitch & speech rate generates distinctive sound characters)
        when (style) {
            "Male" -> {
                tts?.setPitch(0.7f)
                tts?.setSpeechRate(0.9f)
            }
            "Female" -> {
                tts?.setPitch(1.2f)
                tts?.setSpeechRate(1.0f)
            }
            "Robotic" -> {
                tts?.setPitch(0.5f)
                tts?.setSpeechRate(1.4f)
            }
            "Motivational Coach" -> {
                tts?.setPitch(1.1f)
                tts?.setSpeechRate(1.2f)
            }
            "Celebrity" -> { // Imitate deep cinema announcer tone
                tts?.setPitch(0.6f)
                tts?.setSpeechRate(0.85f)
            }
            else -> {
                tts?.setPitch(1.0f)
                tts?.setSpeechRate(1.0f)
            }
        }

        // Speaks the requested message
        if (previewOnly) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PreviewId")
        }
    }

    // ----------------------------------------------------
    // Sound Recording and Auditing Engine
    // ----------------------------------------------------
    fun startRecordingCustomVoice() {
        // Prepare local directory inside cache or files
        val outputDir = getApplication<Application>().cacheDir
        val audioFile = File(outputDir, "echo_rec_${System.currentTimeMillis()}.mp3")
        activeRecordingFile = audioFile

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(getApplication())
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile.absolutePath)
            try {
                prepare()
                start()
                _isRecording.value = true
                recordingDuration.value = 0
                
                // Track elapsed seconds
                recordingTimer = object : CountDownTimer(120000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        recordingDuration.value += 1
                    }
                    override fun onFinish() {}
                }.start()
                
                Log.d("EchoAlarmVM", "Recording started successfully targeting: ${audioFile.absolutePath}")
            } catch (e: Exception) {
                Log.e("EchoAlarmVM", "MediaRecorder failed to initialize: ${e.message}")
                _isRecording.value = false
            }
        }
    }

    fun stopRecordingCustomVoice(customTitle: String) {
        recordingTimer?.cancel()
        recordingTimer = null
        
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            Log.e("EchoAlarmVM", "MediaRecorder stop failed: ${e.message}")
        }
        mediaRecorder = null
        _isRecording.value = false

        val completedRec = activeRecordingFile
        if (completedRec != null && completedRec.exists()) {
            val duration = recordingDuration.value
            viewModelScope.launch {
                repository.insertVoiceNote(
                    VoiceNote(
                        title = if (customTitle.isNotBlank()) customTitle else "Voice recording ${System.currentTimeMillis()}",
                        filePath = completedRec.absolutePath,
                        durationSeconds = if (duration > 0) duration else 2
                    )
                )
                Log.d("EchoAlarmVM", "Custom Voice note saved successfully: ${completedRec.absolutePath}")
            }
        }
    }

    fun deleteVoiceNote(voiceNote: VoiceNote) {
        viewModelScope.launch {
            repository.deleteVoiceNote(voiceNote)
        }
    }

    fun playRecordedVoicePreview(filePath: String) {
        stopAnyMediaPlayers()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
                _isPlayingPreview.value = true
                setOnCompletionListener {
                    _isPlayingPreview.value = false
                    stopAnyMediaPlayers()
                }
            }
        } catch (e: Exception) {
            Log.e("EchoAlarmVM", "Failed to play recorded voice preview: ${e.message}")
        }
    }

    fun stopRecordedVoicePreview() {
        stopAnyMediaPlayers()
        _isPlayingPreview.value = false
    }

    private fun stopAnyMediaPlayers() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.e("EchoAlarmVM", "MediaPlayer cleanup failed: ${e.message}")
        }
        mediaPlayer = null
    }

    // ----------------------------------------------------
    // Bedtime Sound Loop / Sleep Tracking Analytics Simulated
    // ----------------------------------------------------
    fun toggleRelaxingSound(soundName: String, durationHours: Int) {
        if (_relaxingSoundPlaying.value == soundName) {
            // Already playing. Dismiss.
            stopRelaxingSound()
            return
        }

        stopRelaxingSound()
        _relaxingSoundPlaying.value = soundName

        // Configure counting down timers
        bedtimeTimer?.cancel()
        bedtimeTimeRemaining.value = durationHours * 3600L * 1000L
        bedtimeTimer = object : CountDownTimer(bedtimeTimeRemaining.value, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                bedtimeTimeRemaining.value = millisUntilFinished
            }
            override fun onFinish() {
                stopRelaxingSound()
            }
        }.start()

        // Synthesize immersive waveforms dynamically or use simulated loops
        // To respect runtime constraints, synth is represented on a clean, low-level tone
        // or a mock looping track so it doesn't fail on devices without audio clips loaded.
        playSynthesizedRelaxingSound(soundName)
    }

    fun stopRelaxingSound() {
        _relaxingSoundPlaying.value = null
        bedtimeTimer?.cancel()
        bedtimeTimer = null
        bedtimeTimeRemaining.value = 0
        whiteNoisePlayer?.let {
            try {
                if (it.isPlaying) {
                     it.stop()
                }
                it.release()
            } catch (p: Exception) {}
        }
        whiteNoisePlayer = null
    }

    private fun playSynthesizedRelaxingSound(soundName: String) {
        // Implement looping synthesized soundtrack using standard low pitch beeps or oscillator
        // Generates an immersive wave representation to simulate ambient noise offline!
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Synthesizing a soft looping frequency block matching "Ocean Waves" or "White Noise"
                // This utilizes standard Android sound system to prevent dependency crashes
                val sampleRate = 8000
                val durationSec = 3
                val numSamples = sampleRate * durationSec
                val sample = DoubleArray(numSamples)
                val generatedSnd = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    val frequency = when (soundName) {
                        "Ocean" -> 80.0 + sin(2.0 * Math.PI * i / sampleRate * 0.15) * 20.0 // Low surging frequency
                        "Forest" -> 440.0 + sin(2.0 * Math.PI * i / sampleRate * 2.5) * 50.0 // Birds chirping frequency
                        "Rain" -> 1500.0 * Math.random() // High frequency crackle rain
                        else -> 500.0 * Math.random() // Flat white noise
                    }
                    sample[i] = sin(2.0 * Math.PI * i / (sampleRate / frequency))
                    if (soundName == "White Noise" || soundName == "Rain") {
                        sample[i] = Math.random() * 2.0 - 1.0
                    }
                    // Scale amplitude
                    val valInt = (sample[i] * 32767).toInt()
                    val idx = i * 2
                    generatedSnd[idx] = (valInt and 0x00ff).toByte()
                    generatedSnd[idx + 1] = (valInt and 0xff00 ushr 8).toByte()
                }

                // Write sound out locally
                val cacheFile = File(getApplication<Application>().cacheDir, "temp_synth_${soundName.hashCode()}.wav")
                cacheFile.writeBytes(generatedSnd)

                withContext(Dispatchers.Main) {
                    if (_relaxingSoundPlaying.value == soundName) {
                        whiteNoisePlayer = MediaPlayer().apply {
                            setDataSource(cacheFile.absolutePath)
                            isLooping = true
                            prepare()
                            start()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("EchoAlarmVM", "Failed to compile synthesized Wave: ${e.message}")
            }
        }
    }

    // ----------------------------------------------------
    // SYSTEM RINGER CONTROLS: GRADUAL RISE, SNOOZE, DISMISS, CHALLENGES
    // ----------------------------------------------------
    fun triggerAlarmRinging(alarmId: Int) {
        viewModelScope.launch {
            val matchedAlarm = repository.getAlarmById(alarmId)
            if (matchedAlarm != null && matchedAlarm.isEnabled) {
                _activeRingerAlarm.value = matchedAlarm
                
                // Set the current global theme of the app to this alarm's theme for absolute consistency!
                _currentTheme.value = matchedAlarm.themeName
                
                // Initialize Challenge calculations
                initializeRingingChallenge(matchedAlarm)
                
                // Start gradual fade-in logic
                startGradualRiseLoops(matchedAlarm)
            }
        }
    }

    private fun initializeRingingChallenge(alarm: Alarm) {
        when (alarm.challengeType) {
            "MATH" -> {
                val valA = (12..45).random()
                val valB = (5..15).random()
                val ops = listOf("+", "-", "*").random()
                challengeMathQuestion.value = "$valA $ops $valB"
                challengeMathResult.value = when (ops) {
                    "+" -> valA + valB
                    "-" -> valA - valB
                    else -> valA * valB
                }
            }
            "SHAKE" -> {
                challengeShakeCount.value = 0
            }
            "MEMORY" -> {
                challengeMemorySequence.value = List(4) { (1..6).random() }
                challengeMemoryUserSequence.value = emptyList()
            }
            "TYPING" -> {
                val quotes = listOf(
                    "Success is not final, failure is not fatal.",
                    "Rise and shine, the early bird gets the prize.",
                    "The best way to predict your future is to create it.",
                    "Believe you can and you are halfway there."
                )
                challengeTypingPrompt.value = quotes.random()
                challengeTypingInput.value = ""
            }
            "CAPTCHA" -> {
                val chars = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
                challengeCaptchaPrompt.value = List(6) { chars.random() }.joinToString("")
                challengeCaptchaInput.value = ""
            }
            else -> {}
        }
    }

    private fun startGradualRiseLoops(alarm: Alarm) {
        ringerVolume.value = 0.1f
        ringerVibrationIntensity.value = 0.1f

        viewModelScope.launch {
            // Continually play voice synthesizer warnings looping
            while (_activeRingerAlarm.value?.id == alarm.id) {
                // If it is AI Text voice, speak aloud
                if (alarm.voiceType == "AI" && isTtsReady.value) {
                    convertTextToAIStyle(alarm.voiceText, alarm.voiceLanguage, alarm.voiceStyle, previewOnly = false)
                } else if (alarm.voiceType == "RECORDED" && alarm.voiceFilePath != null) {
                    playRecordedVoicePreview(alarm.voiceFilePath)
                } else {
                    // Fallback classic system buzzer simulation
                    tts?.speak("Wake up wakeup wakeup! It is ${alarm.getFormattedTime()}", TextToSpeech.QUEUE_FLUSH, null, "BuzzerId")
                }

                delay(12000) // Looping intervals of speech

                if (alarm.isGradualVolume && ringerVolume.value < 1.0f) {
                    ringerVolume.value += 0.15f
                    ringerVibrationIntensity.value += 0.15f
                }
            }
        }
    }

    fun testRingerTransition(alarm: Alarm) {
        _activeRingerAlarm.value = alarm
        _currentTheme.value = alarm.themeName
        initializeRingingChallenge(alarm)
        startGradualRiseLoops(alarm)
    }

    fun submitChallengeMath(answer: Int): Boolean {
        if (answer == challengeMathResult.value) {
            dismissActiveRinger()
            return true
        }
        return false
    }

    fun incrementShakeChallenge() {
        val count = challengeShakeCount.value + 1
        challengeShakeCount.value = count
        // Need 15 shakes to disable
        if (count >= 15) {
            dismissActiveRinger()
        }
    }

    fun submitMemorySequence(inputIndex: Int): Boolean {
        val target = challengeMemorySequence.value
        val current = challengeMemoryUserSequence.value + inputIndex
        challengeMemoryUserSequence.value = current

        // Validate index by index
        val targetSlice = target.take(current.size)
        if (current != targetSlice) {
            // Mistake. Reset!
            challengeMemoryUserSequence.value = emptyList()
            return false
        }

        if (current.size == target.size) {
            // Match complete!
            dismissActiveRinger()
            return true
        }
        return true
    }

    fun submitTypingChallenge(text: String): Boolean {
        challengeTypingInput.value = text
        if (text.trim().equals(challengeTypingPrompt.value.trim(), ignoreCase = true)) {
            dismissActiveRinger()
            return true
        }
        return false
    }

    fun submitCaptchaChallenge(text: String): Boolean {
        challengeCaptchaInput.value = text
        if (text.trim().equals(challengeCaptchaPrompt.value.trim(), ignoreCase = true)) {
            dismissActiveRinger()
            return true
        }
        return false
    }

    fun snoozeActiveRinger() {
        val currentAlarm = _activeRingerAlarm.value
        if (currentAlarm != null) {
            tts?.stop()
            stopAnyMediaPlayers()
            _activeRingerAlarm.value = null
            
            // Set simple alert notification to fire in selected minutes
            val snoozeAlarm = currentAlarm.copy(
                id = currentAlarm.id + 100000, // Safe offset
                hour = (currentAlarm.hour + (currentAlarm.minute + snoozeMinutes.value) / 60) % 24,
                minute = (currentAlarm.minute + snoozeMinutes.value) % 60,
                repeatDays = "", // Snooze doesn't repeat
                isEnabled = true,
                label = "Snoozed: ${currentAlarm.label}"
            )
            AlarmScheduler.schedule(getApplication(), snoozeAlarm)
        }
    }

    fun dismissActiveRinger() {
        val currentAlarm = _activeRingerAlarm.value
        if (currentAlarm != null) {
            tts?.stop()
            stopAnyMediaPlayers()
            _activeRingerAlarm.value = null
            
            // Handle day advancement if repeating alarm
            if (!currentAlarm.isRepeating()) {
                // Save state as disabled
                viewModelScope.launch {
                    val disabledAlarm = currentAlarm.copy(isEnabled = false)
                    repository.updateAlarm(disabledAlarm)
                }
            } else {
                // Keep active and schedule next day index
                AlarmScheduler.schedule(getApplication(), currentAlarm)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.shutdown()
        stopAnyMediaPlayers()
        stopRelaxingSound()
    }
}

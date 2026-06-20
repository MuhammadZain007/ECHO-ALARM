package com.example.data

import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val voiceNoteDao: VoiceNoteDao
) {
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    val allVoiceNotes: Flow<List<VoiceNote>> = voiceNoteDao.getAllVoiceNotes()

    suspend fun getAlarmById(id: Int): Alarm? = alarmDao.getAlarmById(id)

    suspend fun insertAlarm(alarm: Alarm): Long = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)

    suspend fun deleteAlarmById(id: Int) = alarmDao.deleteAlarmById(id)

    suspend fun insertVoiceNote(voiceNote: VoiceNote): Long = voiceNoteDao.insertVoiceNote(voiceNote)

    suspend fun deleteVoiceNote(voiceNote: VoiceNote) = voiceNoteDao.deleteVoiceNote(voiceNote)
    
    suspend fun deleteVoiceNoteById(id: Int) = voiceNoteDao.deleteVoiceNoteById(id)
}

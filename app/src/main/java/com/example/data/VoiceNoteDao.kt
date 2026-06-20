package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(voiceNote: VoiceNote): Long

    @Delete
    suspend fun deleteVoiceNote(voiceNote: VoiceNote)
    
    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteVoiceNoteById(id: Int)
}

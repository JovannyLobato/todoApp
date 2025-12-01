package com.example.todoapp.data

import androidx.room.*
import com.example.todoapp.model.MediaBlock
import com.example.todoapp.model.Note
import com.example.todoapp.model.NoteWithDetails
import com.example.todoapp.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // ... tus otras funciones existentes (insertNote, etc) ...
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaBlocks(mediaBlocks: List<MediaBlock>)

    // --- ESTA ES LA FUNCIÓN QUE FALTABA Y CAUSA EL ERROR EN EL REPOSITORIO ---
    // Debe devolver List<Long> para que funcione: val insertedReminderIds = ...
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>): List<Long>
    // -------------------------------------------------------------------------

    @Query("DELETE FROM mediablock WHERE noteId = :noteId")
    suspend fun deleteMediaBlocksByNoteId(noteId: Int)

    @Query("DELETE FROM reminder WHERE noteId = :noteId")
    suspend fun deleteRemindersByNoteId(noteId: Int)

    @Transaction
    @Query("SELECT * FROM notes")
    fun getAllNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteWithDetails(id: Int): NoteWithDetails?

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteCompletely(id: Int)

    @Query("UPDATE mediablock SET description = :description WHERE id = :id")
    suspend fun updateMediaBlockDescription(id: Int, description: String)

    @Query("SELECT * FROM reminder")
    suspend fun getAllReminders(): List<Reminder>
}
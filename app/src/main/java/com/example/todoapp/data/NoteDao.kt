package com.example.todoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import androidx.room.*
import com.example.todoapp.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Transaction
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotesWithDetails(): Flow<List<NoteWithDetails>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteWithDetails(id: Int): NoteWithDetails

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaBlocks(mediaBlocks: List<MediaBlock>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>)

    @Delete
    suspend fun deleteNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    // obtener solo notas (sin detalles)
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getNotes(): Flow<List<Note>>

    @Query("DELETE FROM mediablock WHERE noteId = :noteId")
    suspend fun deleteMediaBlocksByNoteId(noteId: Int)

    @Query("DELETE FROM reminder WHERE noteId = :noteId")
    suspend fun deleteRemindersByNoteId(noteId: Int)



}
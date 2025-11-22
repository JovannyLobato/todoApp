package com.example.todoapp.repository

import com.example.todoapp.data.NoteDao
import com.example.todoapp.model.*
import kotlinx.coroutines.flow.Flow

import com.example.todoapp.model.*

class NoteRepository(private val noteDao: NoteDao) {
    fun getAllNotes(): Flow<List<NoteWithDetails>> = noteDao.getAllNotesWithDetails()
    suspend fun getNote(id: Int) = noteDao.getNoteWithDetails(id)

    // === INSERTAR nota con sus detalles ===
    suspend fun insertNoteWithDetails(
        note: Note,
        media: List<MediaBlock>,
        reminders: List<Reminder>
    ) {
        val noteId = noteDao.insertNote(note).toInt()
        val mediaWithNoteId = media.map { it.copy(noteId = noteId) }
        val remindersWithNoteId = reminders.map { it.copy(noteId = noteId) }

        noteDao.insertMediaBlocks(mediaWithNoteId)
        noteDao.insertReminders(remindersWithNoteId)
    }

    // === ACTUALIZAR nota ===
    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }


    // === ELIMINAR nota ===
    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteWithDetails(noteId: Int) {
        noteDao.deleteNoteCompletely(noteId)
    }



    suspend fun getNoteWithDetails(id: Int): NoteWithDetails? {
        return noteDao.getNoteWithDetails(id)
    }


    suspend fun updateNoteWithDetails(
        note: Note,
        media: List<MediaBlock>,
        reminders: List<Reminder>
    ) {
        val noteId = note.id ?: return

        // actualizar la nota
        noteDao.updateNote(note)

        // eliminar media y reminders viejos
        noteDao.deleteMediaBlocksByNoteId(noteId)
        noteDao.deleteRemindersByNoteId(noteId)

        // insertar los nuevos con el noteId correcto
        val mediaWithNoteId = media.map { it.copy(noteId = noteId) }
        val remindersWithNoteId = reminders.map { it.copy(noteId = noteId) }

        noteDao.insertMediaBlocks(mediaWithNoteId)
        noteDao.insertReminders(remindersWithNoteId)
    }
    suspend fun updateDescription(blockId: Int, newDescription: String) {
        noteDao.updateMediaBlockDescription(blockId, newDescription)
    }



    companion object
}
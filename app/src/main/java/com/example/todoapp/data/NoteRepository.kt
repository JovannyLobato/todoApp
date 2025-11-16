package com.example.todoapp.data
/*
import android.provider.ContactsContract
import com.example.todoapp.model.NoteWithDetails

import com.example.todoapp.model.Note as AppNote

import kotlinx.coroutines.flow.Flow
import com.example.todoapp.model.*

class NoteRepository(private val noteDao: NoteDao) {

    // Flow de notas con detalles (recomendado para usar en UI/Compose)
    fun getAllNotesWithDetails(): Flow<List<NoteWithDetails>> = noteDao.getAllNotesWithDetails()

    suspend fun getNoteWithDetails(id: Int): NoteWithDetails? = noteDao.getNoteWithDetails(id)

    // Inserta nota y sus detalles (media + reminders)
    suspend fun insertNoteWithDetails(note: ContactsContract.CommonDataKinds.Note, mediaBlocks: List<MediaBlock>, reminders: List<Reminder>) {
        val newId = noteDao.insertNote(note).toInt()
        // asignar noteId a cada media/reminder y guardarlos
        if (mediaBlocks.isNotEmpty()) {
            val mediaWithNoteId = mediaBlocks.map { it.copy(noteId = newId) }
            noteDao.insertMediaBlocks(mediaWithNoteId)
        }
        if (reminders.isNotEmpty()) {
            val remindersWithNoteId = reminders.map { it.copy(noteId = newId) }
            noteDao.insertReminders(remindersWithNoteId)
        }
    }

    // Operaciones por separado si las necesitas
    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
}
*/
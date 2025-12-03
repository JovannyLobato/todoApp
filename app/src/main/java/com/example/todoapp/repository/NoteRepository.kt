package com.example.todoapp.repository

import android.content.Context
import com.example.todoapp.data.NoteDao
import com.example.todoapp.model.*
import com.example.todoapp.util.AlarmScheduler
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val applicationContext: Context
) {
    fun getAllNotes(): Flow<List<NoteWithDetails>> = noteDao.getAllNotesWithDetails()

    suspend fun getNote(id: Int) = noteDao.getNoteWithDetails(id)

    suspend fun getNoteWithDetails(id: Int): NoteWithDetails? {
        return noteDao.getNoteWithDetails(id)
    }

    // === INSERTAR nota con sus detalles ===
    suspend fun insertNoteWithDetails(
        note: Note,
        media: List<MediaBlock>,
        reminders: List<Reminder>
    ) {
        // Insertar nota y obtener ID
        val noteId = noteDao.insertNote(note).toInt()

        // Asignar el ID de la nota a los hijos
        val mediaWithNoteId = media.map { it.copy(noteId = noteId) }
        val remindersWithNoteId = reminders.map { it.copy(noteId = noteId) }

        // Insertar bloques multimedia
        noteDao.insertMediaBlocks(mediaWithNoteId)

        // Insertar recordatorios y CAPTURAR los IDs generados
        val insertedReminderIds = noteDao.insertReminders(remindersWithNoteId)

        // Programar alarmas usando los IDs reales de la base de datos
        val noteTitle = note.title.ifEmpty { "Nueva Nota/Tarea" }

        insertedReminderIds.zip(remindersWithNoteId) { id, reminder ->
            if (id > 0) {
                AlarmScheduler.scheduleReminder(
                    applicationContext,
                    reminder.copy(id = id.toInt()), // Usamos el ID real de la BD
                    noteTitle
                )
            }
        }
    }

    suspend fun updateNoteWithDetails(
        note: Note,
        media: List<MediaBlock>,
        reminders: List<Reminder>
    ) {
        val noteId = note.id ?: return

        // Cancelar alarmas viejas antes de limpiar la BD
        // Obtenemos la versión actual de la base de datos para saber qué alarmas cancelar
        val oldData = noteDao.getNoteWithDetails(noteId)
        oldData?.reminders?.forEach { oldReminder ->
            AlarmScheduler.cancelReminder(applicationContext, oldReminder.id)
        }

        // 2. Actualizar la nota base
        noteDao.updateNote(note)

        // 3. Eliminar media y reminders viejos de la BD
        noteDao.deleteMediaBlocksByNoteId(noteId)
        noteDao.deleteRemindersByNoteId(noteId)

        // 4. Preparar nuevos datos con el noteId correcto
        val mediaWithNoteId = media.map { it.copy(noteId = noteId) }
        val remindersWithNoteId = reminders.map { it.copy(noteId = noteId) }


        noteDao.insertMediaBlocks(mediaWithNoteId)
        val insertedReminderIds = noteDao.insertReminders(remindersWithNoteId)


        val noteTitle = note.title.ifEmpty { "Nota/Tarea sin título" }

        insertedReminderIds.zip(remindersWithNoteId) { id, reminder ->
            if (id > 0) {
                AlarmScheduler.scheduleReminder(
                    applicationContext,
                    reminder.copy(id = id.toInt()),
                    noteTitle
                )
            }
        }
    }

    suspend fun deleteNote(note: Note) {

        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteWithDetails(noteId: Int) {

        val oldData = noteDao.getNoteWithDetails(noteId)
        oldData?.reminders?.forEach { oldReminder ->
            AlarmScheduler.cancelReminder(applicationContext, oldReminder.id)
        }

        noteDao.deleteNoteCompletely(noteId)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun updateDescription(blockId: Int, newDescription: String) {
        noteDao.updateMediaBlockDescription(blockId, newDescription)
    }

    companion object
}
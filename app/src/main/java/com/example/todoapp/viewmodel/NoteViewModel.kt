package com.example.todoapp.viewmodel

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.model.*
import com.example.todoapp.repository.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class AddEditUiState(
    val title: String = "",
    val isTask: Boolean = false,
    val dueDateTimestamp: Long? = null,
    val mediaBlocks: List<MediaBlock> = emptyList(),
    val reminders: List<Reminder> = emptyList(),

    // Variables movidas desde la UI:
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showReminderDatePicker: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showBlockMediaDeleteDialog: Boolean = false,
    val showNotificationPermissionDeniedDialog: Boolean = false,
    val showVideoPermissionDeniedDialog: Boolean = false,
    val showVideoSheet: Boolean = false, // Ya existía
    val showAudioSheet: Boolean = false,
    val showImageSheet: Boolean = false,
    val showFullImage: Boolean = false,

    // Datos temporales movidos desde la UI:
    val reminderBaseDateMillis: Long? = null,
    val blockToDelete: MediaBlock? = null,
    val fullImageUri: String? = null,
    val tempPhotoUri: Uri? = null, // Para la cámara de fotos

    // Lógica interna de video
    val requestVideoPermission: Boolean = false,
    val videoUri: Uri? = null,
    val reminderToEdit: Reminder? = null,
    val showAudioPermissionDeniedDialog: Boolean = false
)

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private var pendingVideoUri: Uri? = null

    // --- SETTERS SIMPLES PARA LAS NUEVAS VARIABLES ---

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onIsTaskChange(value: Boolean) {
        _uiState.update { it.copy(isTask = value) }
    }

    fun onDueDateChange(timestamp: Long?) {
        _uiState.update { it.copy(dueDateTimestamp = timestamp) }
    }

    // Control de diálogos y sheets
    fun setShowDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }

    fun setShowTimePicker(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun setShowReminderDatePicker(show: Boolean) {
        _uiState.update { it.copy(showReminderDatePicker = show) }
    }

    fun setShowDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun setShowBlockMediaDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showBlockMediaDeleteDialog = show) }
    }

    fun setBlockToDelete(block: MediaBlock?) {
        _uiState.update { it.copy(blockToDelete = block) }
    }

    fun setShowNotificationPermissionDeniedDialog(show: Boolean) {
        _uiState.update { it.copy(showNotificationPermissionDeniedDialog = show) }
    }

    fun setShowVideoPermissionDeniedDialog(show: Boolean) {
        _uiState.update { it.copy(showVideoPermissionDeniedDialog = show) }
    }

    fun setShowVideoSheet(show: Boolean) {
        _uiState.update { it.copy(showVideoSheet = show) }
    }

    fun setShowAudioSheet(show: Boolean) {
        _uiState.update { it.copy(showAudioSheet = show) }
    }

    fun setShowImageSheet(show: Boolean) {
        _uiState.update { it.copy(showImageSheet = show) }
    }

    fun setShowFullImage(show: Boolean, uri: String? = null) {
        _uiState.update { it.copy(showFullImage = show, fullImageUri = uri) }
    }

    // Datos temporales
    fun setReminderBaseDateMillis(timestamp: Long?) {
        _uiState.update { it.copy(reminderBaseDateMillis = timestamp) }
    }

    fun setTempPhotoUri(uri: Uri?) {
        _uiState.update { it.copy(tempPhotoUri = uri) }
    }

    // ======== Lógica de negocio existente (Recordatorios, Bloques, Guardar) ========

    fun addReminder(reminderTime: Long) {
        val newReminder = Reminder(id = 0, noteId = 0, reminderTime = reminderTime)
        _uiState.update { it.copy(reminders = it.reminders + newReminder) }
    }

    fun removeReminder(reminder: Reminder) {
        _uiState.update { it.copy(reminders = it.reminders.filter { r -> r != reminder }) }
    }

    fun addMediaBlock(type: MediaType, content: String? = null, description: String? = null) {
        val order = _uiState.value.mediaBlocks.size
        val newBlock = MediaBlock(
            id = UUID.randomUUID().mostSignificantBits.toInt(),
            noteId = 0,
            type = type,
            content = content,
            description = description,
            order = order
        )
        _uiState.update { it.copy(mediaBlocks = it.mediaBlocks + newBlock) }
    }

    fun updateBlockDescription(blockId: Int, newDescription: String) {
        _uiState.update { state ->
            state.copy(mediaBlocks = state.mediaBlocks.map {
                if (it.id == blockId) it.copy(description = newDescription) else it
            })
        }
    }

    fun removeMediaBlock(blockId: Int) {
        _uiState.update { it.copy(mediaBlocks = it.mediaBlocks.filter { b -> b.id != blockId }) }
    }

    fun resetUiState() {
        _uiState.value = AddEditUiState()
    }

    fun loadNoteDetails(noteWithDetails: NoteWithDetails?) {
        if (noteWithDetails != null) {
            _uiState.value = AddEditUiState(
                title = noteWithDetails.note.title,
                isTask = noteWithDetails.note.isTask,
                dueDateTimestamp = noteWithDetails.note.dueDateTimestamp,
                mediaBlocks = noteWithDetails.mediaBlocks.sortedBy { it.order },
                reminders = noteWithDetails.reminders
            )
        }
    }

    // ... (Tus funciones de base de datos saveNote, deleteNoteWithDetails, etc. quedan igual) ...
    fun saveNote(isEditing: Boolean, noteId: Int? = null) {
        viewModelScope.launch {
            val idValue = noteId ?: -1
            val note = Note(
                id = if (idValue == -1) 0 else idValue,
                title = _uiState.value.title,
                isTask = _uiState.value.isTask,
                dueDateTimestamp = _uiState.value.dueDateTimestamp
            )
            if (noteId == -1) {
                repository.insertNoteWithDetails(note, _uiState.value.mediaBlocks, _uiState.value.reminders)
            } else {
                repository.updateNoteWithDetails(note, _uiState.value.mediaBlocks, _uiState.value.reminders)
            }
        }
    }

    fun deleteNoteWithDetails(noteId: Int) {
        viewModelScope.launch { repository.deleteNoteWithDetails(noteId) }
    }

    // ======== Manejo de Archivos (Video y Foto) ========

    fun prepareVideoUri(context: Context): Uri? {
        val videoFile = File(context.externalCacheDir, "video_${System.currentTimeMillis()}.mp4")
        videoFile.createNewFile()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", videoFile)
    }

    // Nueva función para preparar la URI de la foto desde el ViewModel
    fun prepareImageUri(context: Context): Uri? {
        val file = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        file.createNewFile()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    fun onVideoCaptured(success: Boolean) {
        if (success && pendingVideoUri != null) {
            addMediaBlock(MediaType.VIDEO, pendingVideoUri.toString())
        }
        pendingVideoUri = null
    }

    fun setPendingVideoUri(uri: Uri) {
        pendingVideoUri = uri
    }

    fun onVideoSelected(context: Context, uri: Uri?) {
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) { e.printStackTrace() }
            addMediaBlock(MediaType.VIDEO, it.toString())
        }
    }

    suspend fun getNoteWithDetails(id: Int): NoteWithDetails? {
        return repository.getNoteWithDetails(id)
    }

    // Flow para la lista principal
    val allNotes: StateFlow<List<NoteWithDetails>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    // Seleccionar recordatorio a editar
    fun setReminderToEdit(reminder: Reminder?) {
        _uiState.update { it.copy(reminderToEdit = reminder) }
    }

    // Guardar los cambios del recordatorio
    fun updateReminder(oldReminder: Reminder, newTime: Long) {
        _uiState.update { state ->
            val updatedReminders = state.reminders.map {
                // Si es el recordatorio viejo, creamos una copia con la nueva hora
                if (it == oldReminder) it.copy(reminderTime = newTime) else it
            }
            // Guardamos la lista actualizada y limpiamos la variable de edición
            state.copy(reminders = updatedReminders, reminderToEdit = null)
        }
    }

    fun setShowAudioPermissionDeniedDialog(show: Boolean) {
        _uiState.update { it.copy(showAudioPermissionDeniedDialog = show) }
    }
}
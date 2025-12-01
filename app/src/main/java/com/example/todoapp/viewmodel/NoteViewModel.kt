package com.example.todoapp.viewmodel

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.todoapp.model.*


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import com.example.todoapp.repository.NoteRepository
import com.example.todoapp.requiredVideoPermissions
import java.io.File
import java.util.UUID

private var nextBlockId = 1

data class AddEditUiState(
    val title: String = "",
    val isTask: Boolean = false,
    val dueDateTimestamp: Long? = null,
    val mediaBlocks: List<MediaBlock> = emptyList(),
    // val mediaBlocks: MutableList<MediaBlock> = mutableListOf(),
    val reminders: List<Reminder> = emptyList(),
    val showDatePicker: Boolean = false,
    val showVideoSheet: Boolean = false,
    val showVideoPermissionDeniedDialog: Boolean = false,
    val requestVideoPermission: Boolean = false,
    val launchCamera: Boolean = false,
    val videoUri: Uri? = null,
    val permissionAttempts: Int = 0,
)


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    private var pendingVideoUri: Uri? = null



    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onIsTaskChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(isTask = value)
    }

    fun onDueDateChange(timestamp: Long?) {
        _uiState.value = _uiState.value.copy(dueDateTimestamp = timestamp)
    }

    fun setShowDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    // ======== Manejo de Recordatorios ========
    fun addReminder(reminderTime: Long) {
        val newReminder = Reminder(
            id = 0,
            noteId = 0,
            reminderTime = reminderTime
        )

        _uiState.update { current ->
            current.copy(
                reminders = current.reminders + newReminder
            )
        }
    }

    fun removeReminder(reminder: Reminder) {
        _uiState.update { current ->
            current.copy(
                reminders = current.reminders.filter { it != reminder }
            )
        }
    }

    // ======== Manejo de bloques de contenido ========
    fun addMediaBlock(
        type: MediaType,
        content: String? = null,
        description: String? = null
    ) {
        val order = _uiState.value.mediaBlocks.size
        val newBlock = MediaBlock(
            id = UUID.randomUUID().mostSignificantBits.toInt(),
            noteId = 0,
            type = type,
            content = content,
            description = description,
            order = order
        )

        _uiState.update { current ->
            current.copy(
                mediaBlocks = current.mediaBlocks + newBlock
            )
        }
    }

    fun updateMediaBlock(index: Int, content: String?, description: String?) {
        // crea copia mutable
        val updatedList = _uiState.value.mediaBlocks.toMutableList()

        // editar el elemento
        val oldBlock = updatedList[index]
        updatedList[index] = oldBlock.copy(
            content = content ?: oldBlock.content,
            description = description ?: oldBlock.description
        )

        // actualizar el estado
        _uiState.value = _uiState.value.copy(mediaBlocks = updatedList)
    }

    fun setShowVideoPermissionDenied(show: Boolean) {
        _uiState.update { it.copy(showVideoPermissionDeniedDialog = show) }
    }

    fun removeMediaBlock(blockId: Int) {
        _uiState.update { current ->
            current.copy(
                mediaBlocks = current.mediaBlocks.filter { it.id != blockId }
            )
        }
    }




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
                // Crear nueva nota
                repository.insertNoteWithDetails(note, _uiState.value.mediaBlocks, _uiState.value.reminders)
            } else {
                // Actualizar nota existente
                repository.updateNoteWithDetails(note, _uiState.value.mediaBlocks, _uiState.value.reminders)
            }

        }
    }

    fun resetUiState() {
        _uiState.value = AddEditUiState()
    }





    // Exponer Flow como StateFlow para Compose
    private val _allNotes = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val allNotes: StateFlow<List<NoteWithDetails>> = _allNotes

    // Métodos para UI que llaman a repository dentro de corutinas
    fun insertNoteWithDetails(note: Note, media: List<MediaBlock> = emptyList(), reminders: List<Reminder> = emptyList()) {
        viewModelScope.launch {
            repository.insertNoteWithDetails(note, media, reminders)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }



    // guarda la descripcion:
    fun updateBlockDescription(blockId: Int, newDescription: String) {
        _uiState.update { state ->
            state.copy(
                mediaBlocks = state.mediaBlocks.map { block ->
                    if (block.id == blockId)
                        block.copy(description = newDescription)
                    else block
                }
            )
        }
    }


    fun deleteNoteWithDetails(noteId: Int) {
        viewModelScope.launch {
            repository.deleteNoteWithDetails(noteId)
        }
    }



    fun loadNoteDetails(noteWithDetails: NoteWithDetails?) {
        if (noteWithDetails != null) {
            _uiState.value = AddEditUiState(
                title = noteWithDetails.note.title,
                isTask = noteWithDetails.note.isTask,
                dueDateTimestamp = noteWithDetails.note.dueDateTimestamp,
                mediaBlocks = noteWithDetails.mediaBlocks.toMutableList().sortedBy { it.order } ,
                reminders = noteWithDetails.reminders.toMutableList()
            )
        }
    }




    fun prepareVideoUri(context: Context): Uri? {
        val videoFile = File(
            context.externalCacheDir,
            "video_${System.currentTimeMillis()}.mp4"
        )
        videoFile.createNewFile()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            videoFile
        )
    }


    fun onVideoCaptured(success: Boolean) {
        Log.d("PERMISSION1", "Success = $success, pendingUri = $pendingVideoUri")
        if (success && pendingVideoUri != null) {
            addMediaBlock(MediaType.VIDEO, pendingVideoUri.toString())
            Log.d("PERMISSION1", "video creado")
        }
        else{
            Log.d("PERMISSION1", "no se creo el mediablock")
        }
        pendingVideoUri = null
    }

    fun onVideoSelected(context: Context, uri: Uri?) {
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            addMediaBlock(MediaType.VIDEO, it.toString())
        }
    }

    fun setShowVideoSheet(show: Boolean) {
        _uiState.update { it.copy(showVideoSheet = show) }
    }

    fun setShowVideoPermissionDeniedDialog(show: Boolean) {
        _uiState.update { it.copy(showVideoPermissionDeniedDialog = show) }
    }


    fun setPendingVideoUri(uri: Uri) {
        pendingVideoUri = uri
    }



    suspend fun getNoteWithDetails(id: Int): NoteWithDetails? {
        return repository.getNoteWithDetails(id)
    }
}


package com.example.todoapp.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.todoapp.model.*


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import com.example.todoapp.repository.NoteRepository
import java.util.UUID

private var nextBlockId = 1

data class AddEditUiState(
    val title: String = "",
    val isTask: Boolean = false,
    val dueDateTimestamp: Long? = null,
    val mediaBlocks: List<MediaBlock> = emptyList(),
    // val mediaBlocks: MutableList<MediaBlock> = mutableListOf(),
    val reminders: List<Reminder> = emptyList(),
    val showDatePicker: Boolean = false
)


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    // ======== Manejadores de campos ========
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

    fun removeMediaBlock(index: Int) {
        _uiState.update { current ->
            current.copy(
                mediaBlocks = current.mediaBlocks.toMutableList().apply {
                    removeAt(index)
                }
            )
        }
    }


    // ======== Guardar Nota Completa ========
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
                mediaBlocks = noteWithDetails.mediaBlocks.toMutableList(),
                reminders = noteWithDetails.reminders.toMutableList()
            )
        }
    }

    suspend fun getNoteWithDetails(id: Int): NoteWithDetails? {
        return repository.getNoteWithDetails(id)
    }
}


package com.example.todoapp.viewmodel

/*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.model.Note
import com.example.todoapp.data.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NoteUiState(
    val title: String = "",
    val description: String = "",
    var imageUri: String? = null,
    val isTask: Boolean = false,
    val dueDateTimestamp: Long? = null,
    val showDatePicker: Boolean = false
)


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState

    // Metodos para actualizar el estado:
    fun onTitleChange(newTitle: String) {
        _uiState.value = _uiState.value.copy(title = newTitle)
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun onImageUriChange(newUri: String?) {
        _uiState.value = _uiState.value.copy(imageUri = newUri)
    }

    fun onIsTaskChange(newIsTask: Boolean) {
        _uiState.value = _uiState.value.copy(isTask = newIsTask)
    }

    fun onDueDateChange(newTimestamp: Long?) {
        _uiState.value = _uiState.value.copy(dueDateTimestamp = newTimestamp)
    }

    fun setShowDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDatePicker = show)
    }

    // Cargar datos iniciales al editar
    fun loadNoteData(
        title: String,
        description: String,
        imageUri: String?,
        isTask: Boolean,
        dueDateTimestamp: Long?
    ) {
        _uiState.value = NoteUiState(
            title = title,
            description = description,
            imageUri = imageUri,
            isTask = isTask,
            dueDateTimestamp = dueDateTimestamp
        )
    }

    // Guardar / actualizar nota
    fun saveNote(id: Int?, isEditing: Boolean) {
        viewModelScope.launch {
            val note = Note(
                // id = if (isEditing) id else 0,
                id = if (isEditing) id ?: 0 else 0,
                title = _uiState.value.title,
                description = _uiState.value.description,
                imageUri = _uiState.value.imageUri,
                isTask = _uiState.value.isTask,
                dueDateTimestamp = _uiState.value.dueDateTimestamp
            )
            repository.insert(note)
        }
    }

    fun addNote(
        title: String,
        description: String,
        imageUri: String?,
        isTask: Boolean,
        dueDateTimestamp: Long? = null
    ) {
        viewModelScope.launch {
            repository.insert(
                Note(
                    title = title,
                    description = description,
                    imageUri = imageUri,
                    isTask = isTask,
                    dueDateTimestamp = dueDateTimestamp
                )
            )
        }
    }

    fun updateNote(
        id: Int,
        newTitle: String,
        newDescription: String,
        imageUri: String?,
        isTask: Boolean,
        dueDateTimestamp: Long?
    ) {
        viewModelScope.launch {
            repository.insert(
                Note(
                    id = id,
                    title = newTitle,
                    description = newDescription,
                    imageUri = imageUri,
                    isTask = isTask,
                    dueDateTimestamp = dueDateTimestamp
                )
            )
        }
    }

    fun getAllNotes(): Flow<List<Note>> = repository.getAllNotes()
}
*/



import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.todoapp.model.*


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.*
import com.example.todoapp.repository.NoteRepository


data class AddEditUiState(
    val title: String = "",
    val isTask: Boolean = false,
    val dueDateTimestamp: Long? = null,
    val mediaBlocks: MutableList<MediaBlock> = mutableListOf(),
    val reminders: MutableList<Reminder> = mutableListOf(),
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
    fun addMediaBlock(type: MediaType, content: String? = null, description: String? = null) {
        val order = _uiState.value.mediaBlocks.size
        val newBlock = MediaBlock(0, 0, type, content, description, order)

        _uiState.update { current ->
            current.copy(
                mediaBlocks = current.mediaBlocks.toMutableList().apply {
                    add(newBlock)
                }
            )
        }
    }

    fun updateMediaBlock(index: Int, content: String?, description: String?) {
        _uiState.value.mediaBlocks[index] =
            _uiState.value.mediaBlocks[index].copy(content = content, description = description)
        _uiState.value = _uiState.value.copy(mediaBlocks = _uiState.value.mediaBlocks)
    }
    /*
    fun removeMediaBlock(index: Int) {
        _uiState.value.mediaBlocks.removeAt(index)
        _uiState.value = _uiState.value.copy(mediaBlocks = _uiState.value.mediaBlocks)
    }
     */
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

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
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


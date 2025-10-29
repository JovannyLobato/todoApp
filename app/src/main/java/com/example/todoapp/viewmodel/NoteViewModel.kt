package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Note
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

    // 🔹 Cargar datos iniciales al editar
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

    // 🔹 Guardar / actualizar nota
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
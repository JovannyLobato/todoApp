package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Note
import com.example.todoapp.data.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch


class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

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
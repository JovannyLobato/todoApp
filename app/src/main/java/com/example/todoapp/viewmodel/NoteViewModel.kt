package com.example.todoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Note
import com.example.todoapp.data.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    fun getAllNotes(): Flow<List<Note>> = repository.getAllNotes()

    fun addNote(title: String, description: String, imageUri: String?, isTask: Boolean = false) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                description = description,
                imageUri = imageUri,
                isTask = isTask
            )
            repository.insert(note)
        }
    }
}
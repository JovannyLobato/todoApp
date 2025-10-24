package com.example.todoapp.data

import kotlinx.coroutines.flow.Flow

import com.example.todoapp.data.Note as AppNote

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insert(note: AppNote) = noteDao.insert(note)
    fun getAllNotes(): Flow<List<AppNote>> = noteDao.getAllNotes()
}
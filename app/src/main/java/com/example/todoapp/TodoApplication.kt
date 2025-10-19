package com.example.todoapp

import android.app.Application
import androidx.room.Room
import com.example.todoapp.data.NoteDatabase
import com.example.todoapp.data.NoteRepository

class TodoApplication : Application() {
    lateinit var database: NoteDatabase
    lateinit var repository: NoteRepository

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            NoteDatabase::class.java,
            "notes_db"
        ).build()
        repository = NoteRepository(database.noteDao())
    }
}

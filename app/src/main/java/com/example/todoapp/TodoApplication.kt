package com.example.todoapp

import android.app.Application
import androidx.room.Room
import com.example.todoapp.data.AppDatabase
import com.example.todoapp.repository.NoteRepository

class TodoApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository: NoteRepository by lazy {
        // Ahora pasamos applicationContext como segundo parámetro
        NoteRepository(database.noteDao(), applicationContext)
    }
}


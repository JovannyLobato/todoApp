/*
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
*/

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
        // CORRECCIÓN AQUÍ:
        // Ahora pasamos applicationContext como segundo parámetro
        NoteRepository(database.noteDao(), applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(applicationContext)
    }   
}


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
import kotlin.getValue

class TodoApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "notes_db" // nombre del archivo de base de datos
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    val repository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }
}

package com.example.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

// CAMBIO: Incrementa la versión de 1 a 2.
@Database(entities = [Note::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
package com.example.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [Note::class], version = 3, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
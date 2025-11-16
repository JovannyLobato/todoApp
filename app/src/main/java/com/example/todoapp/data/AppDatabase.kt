package com.example.todoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.model.Note
import com.example.todoapp.model.MediaBlock
import com.example.todoapp.model.Reminder

@Database(
    entities = [Note::class, MediaBlock::class, Reminder::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun mediaBlockDao(): MediaBlockDao
    abstract fun reminderDao(): ReminderDao
}

package com.example.todoapp.model

import androidx.room.*

@Entity(
    tableName = "reminder",
    foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("noteId")]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val reminderTime: Long
)

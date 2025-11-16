package com.example.todoapp.model

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithDetails(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val mediaBlocks: List<MediaBlock>,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId"
    )
    val reminders: List<Reminder>
)
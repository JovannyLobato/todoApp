package com.example.todoapp.model

import androidx.room.*

@Entity(
    tableName = "mediablock",
    foreignKeys = [ForeignKey(
        entity = Note::class,
        parentColumns = ["id"],
        childColumns = ["noteId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("noteId")]
)
data class MediaBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noteId: Int,
    val type: com.example.todoapp.model.MediaType,
    val content: String?,
    val description: String?,
    val order: Int
)
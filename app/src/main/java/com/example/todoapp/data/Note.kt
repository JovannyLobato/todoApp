package com.example.todoapp.data // ⬅️ DEBE SER ESTE PAQUETE

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUri: String?,
    // **CAMBIO A AGREGAR:** Campo para distinguir entre Nota y Tarea
    val isTask: Boolean = false // Por defecto es una Nota
)
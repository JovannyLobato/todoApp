package com.example.todoapp.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.todoapp.model.Reminder

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder)

    @Query("DELETE FROM reminder WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Int)
}

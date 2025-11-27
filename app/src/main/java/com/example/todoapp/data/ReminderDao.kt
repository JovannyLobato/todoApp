package com.example.todoapp.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.todoapp.model.Reminder

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<Reminder>): List<Long>

    @Query("DELETE FROM reminder WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Int)
}

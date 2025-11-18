package com.example.todoapp.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.todoapp.model.MediaBlock
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Dao
interface MediaBlockDao {
    @Insert
    suspend fun insert(mediaBlock: MediaBlock)

    @Query("DELETE FROM mediablock WHERE noteId = :noteId")
    suspend fun deleteByNoteId(noteId: Int)




}

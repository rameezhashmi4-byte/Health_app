package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pushprime.model.QuickSessionLog
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickSessionDao {
    @Insert
    suspend fun insert(log: QuickSessionLog): Long

    @Query("SELECT * FROM quick_session_logs ORDER BY dateTime DESC")
    fun getAllLogs(): Flow<List<QuickSessionLog>>

    @Query("SELECT * FROM quick_session_logs WHERE id = :id")
    suspend fun getById(id: Long): QuickSessionLog?
}

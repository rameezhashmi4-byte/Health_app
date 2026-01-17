package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.PullupSession
import kotlinx.coroutines.flow.Flow

@Dao
interface PullupSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PullupSession)

    @Query("SELECT * FROM pullup_sessions ORDER BY dateTime DESC")
    fun getAllSessions(): Flow<List<PullupSession>>

    @Query("SELECT * FROM pullup_sessions ORDER BY dateTime DESC")
    suspend fun getAllSessionsOnce(): List<PullupSession>

    @Query("SELECT * FROM pullup_sessions WHERE dateTime BETWEEN :start AND :end ORDER BY dateTime DESC")
    fun getSessionsForRange(start: Long, end: Long): Flow<List<PullupSession>>

    @Query("SELECT * FROM pullup_sessions WHERE dateTime BETWEEN :start AND :end ORDER BY dateTime DESC")
    suspend fun getSessionsForRangeOnce(start: Long, end: Long): List<PullupSession>

    @Query("SELECT MAX(dateTime) FROM pullup_sessions")
    suspend fun getLastSessionTime(): Long?
}

package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pushprime.model.SessionStatus
import com.pushprime.model.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: WorkoutSession): Long

    @Update
    suspend fun update(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE sessionId = :sessionId")
    suspend fun getBySessionId(sessionId: String): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND status = :status ORDER BY startedAt DESC")
    fun getActiveSessionsByUser(userId: String, status: String = SessionStatus.ACTIVE.name): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND planId = :planId AND status = :status ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSessionForPlan(userId: String, planId: Long, status: String = SessionStatus.ACTIVE.name): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getAllSessionsByUser(userId: String): Flow<List<WorkoutSession>>

    @Query("DELETE FROM workout_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE workout_sessions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
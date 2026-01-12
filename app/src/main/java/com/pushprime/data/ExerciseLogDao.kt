package com.pushprime.data

import androidx.room.*
import com.pushprime.model.ExerciseLog
import kotlinx.coroutines.flow.Flow

/**
 * Exercise Log DAO
 * Data Access Object for ExerciseLog entity
 */
@Dao
interface ExerciseLogDao {
    /**
     * Insert a new exercise log
     */
    @Insert
    suspend fun insert(exerciseLog: ExerciseLog): Long
    
    /**
     * Update an existing exercise log
     */
    @Update
    suspend fun update(exerciseLog: ExerciseLog)
    
    /**
     * Delete an exercise log
     */
    @Delete
    suspend fun delete(exerciseLog: ExerciseLog)
    
    /**
     * Get all exercise logs
     */
    @Query("SELECT * FROM exercise_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ExerciseLog>>
    
    /**
     * Get logs for a specific exercise
     */
    @Query("SELECT * FROM exercise_logs WHERE exerciseName = :exerciseName ORDER BY timestamp DESC")
    fun getLogsByExercise(exerciseName: String): Flow<List<ExerciseLog>>
    
    /**
     * Get logs for today
     */
    @Query("SELECT * FROM exercise_logs WHERE date = :date ORDER BY timestamp DESC")
    fun getTodayLogs(date: String): Flow<List<ExerciseLog>>
    
    /**
     * Get logs for a date range (for weekly/monthly stats)
     */
    @Query("SELECT * FROM exercise_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getLogsByDateRange(startDate: String, endDate: String): Flow<List<ExerciseLog>>
    
    /**
     * Get total reps/duration for an exercise today
     */
    @Query("SELECT SUM(repsOrDuration) FROM exercise_logs WHERE exerciseName = :exerciseName AND date = :date")
    suspend fun getTodayTotal(exerciseName: String, date: String): Int?
    
    /**
     * Get total reps/duration for an exercise this week
     */
    @Query("SELECT SUM(repsOrDuration) FROM exercise_logs WHERE exerciseName = :exerciseName AND date BETWEEN :startDate AND :endDate")
    suspend fun getWeekTotal(exerciseName: String, startDate: String, endDate: String): Int?
    
    /**
     * Get last N sessions for an exercise
     */
    @Query("SELECT * FROM exercise_logs WHERE exerciseName = :exerciseName ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLastSessions(exerciseName: String, limit: Int): List<ExerciseLog>
    
    /**
     * Get daily summary (total per exercise for a date)
     * Returns map of exercise name to total
     */
    @Query("SELECT exerciseName, SUM(repsOrDuration) as total FROM exercise_logs WHERE date = :date GROUP BY exerciseName")
    suspend fun getDailySummary(date: String): List<DailySummaryResult>
}

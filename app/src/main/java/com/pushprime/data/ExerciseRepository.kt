package com.pushprime.data

import android.content.Context
import com.pushprime.data.DailySummaryResult
import com.pushprime.model.ExerciseLog
import com.pushprime.model.ExerciseType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

/**
 * Exercise Repository
 * Provides clean API for exercise log operations
 * Future: Can add Firebase sync here
 */
class ExerciseRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    val dao = db.exerciseLogDao() // Made public for MetricsScreen
    
    /**
     * Get all exercise logs
     */
    fun getAllLogs(): Flow<List<ExerciseLog>> = dao.getAllLogs()
    
    /**
     * Get logs for a specific exercise
     */
    fun getLogsByExercise(exerciseName: String): Flow<List<ExerciseLog>> = 
        dao.getLogsByExercise(exerciseName)
    
    /**
     * Get today's logs
     */
    fun getTodayLogs(): Flow<List<ExerciseLog>> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dao.getTodayLogs(today)
    }
    
    /**
     * Get logs for last 7 days
     */
    fun getWeeklyLogs(): Flow<List<ExerciseLog>> {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return dao.getLogsByDateRange(startDate, endDate)
    }
    
    /**
     * Insert a new exercise log
     */
    suspend fun insertLog(exerciseLog: ExerciseLog): Long {
        return dao.insert(exerciseLog)
    }
    
    /**
     * Get today's total for an exercise
     */
    suspend fun getTodayTotal(exerciseName: String): Int {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return dao.getTodayTotal(exerciseName, today) ?: 0
    }
    
    /**
     * Get week's total for an exercise
     */
    suspend fun getWeekTotal(exerciseName: String): Int {
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return dao.getWeekTotal(exerciseName, startDate, endDate) ?: 0
    }
    
    /**
     * Get last N sessions for an exercise
     */
    suspend fun getLastSessions(exerciseName: String, limit: Int = 5): List<ExerciseLog> {
        return dao.getLastSessions(exerciseName, limit)
    }
    
    /**
     * Get daily summary (all exercises for today)
     */
    suspend fun getDailySummary(): Map<String, Int> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val summary = dao.getDailySummary(today)
        return summary.associate { it.exerciseName to it.total.toInt() }
    }
    
    /**
     * Get all supported exercises
     */
    fun getSupportedExercises(): List<ExerciseType> {
        return ExerciseType.values().toList()
    }
}

package com.pushprime.data

import androidx.room.*
import com.pushprime.model.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * Session DAO
 * Data Access Object for SessionEntity
 */
@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity): Long
    
    @Update
    suspend fun update(session: SessionEntity)
    
    @Delete
    suspend fun delete(session: SessionEntity)
    
    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?
    
    @Query("SELECT * FROM sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsByDate(date: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    fun getSessionsByDateRange(startDate: String, endDate: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE activityType = :activityType ORDER BY startTime DESC")
    fun getSessionsByActivityType(activityType: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE sportType = :sportType ORDER BY startTime DESC")
    fun getSessionsBySportType(sportType: String): Flow<List<SessionEntity>>
    
    @Query("SELECT * FROM sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?
    
    @Query("SELECT COUNT(*) FROM sessions WHERE date = :date")
    suspend fun getSessionCountForDate(date: String): Int
    
    @Query("SELECT COUNT(*) FROM sessions WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getSessionCountForRange(startDate: String, endDate: String): Int
    
    @Query("SELECT SUM(totalSeconds) FROM sessions WHERE date BETWEEN :startDate AND :endDate AND totalSeconds IS NOT NULL")
    suspend fun getTotalMinutesForRange(startDate: String, endDate: String): Int?
    
    // Weekly aggregation
    @Query("""
        SELECT date, COUNT(*) as count, SUM(totalSeconds) as totalSeconds
        FROM sessions 
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getWeeklyAggregation(startDate: String, endDate: String): List<WeeklyAggregationResult>
    
    // Monthly aggregation by activity type
    @Query("""
        SELECT activityType, COUNT(*) as count, SUM(totalSeconds) as totalSeconds
        FROM sessions 
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY activityType
    """)
    suspend fun getMonthlyAggregationByType(startDate: String, endDate: String): List<MonthlyAggregationResult>
}

/**
 * Weekly aggregation result
 */
data class WeeklyAggregationResult(
    val date: String,
    val count: Int,
    val totalSeconds: Int?
)

/**
 * Monthly aggregation result
 */
data class MonthlyAggregationResult(
    val activityType: String,
    val count: Int,
    val totalSeconds: Int?
)

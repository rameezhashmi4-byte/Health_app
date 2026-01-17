package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.DailyStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStatusDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(status: DailyStatusEntity)

    @Query("SELECT * FROM daily_statuses WHERE date = :date LIMIT 1")
    suspend fun getStatusForDate(date: String): DailyStatusEntity?

    @Query("SELECT * FROM daily_statuses WHERE date = :date LIMIT 1")
    fun observeStatusForDate(date: String): Flow<DailyStatusEntity?>

    @Query(
        "SELECT * FROM daily_statuses WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC"
    )
    fun observeStatusesBetween(startDate: String, endDate: String): Flow<List<DailyStatusEntity>>

    @Query(
        "SELECT COUNT(*) FROM daily_statuses WHERE status = :status AND date BETWEEN :startDate AND :endDate"
    )
    suspend fun countStatusBetween(status: String, startDate: String, endDate: String): Int
}

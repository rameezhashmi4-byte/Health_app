package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.NutritionEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: NutritionEntry)

    @Query("SELECT * FROM nutrition_entries WHERE date = :date ORDER BY createdAt DESC")
    fun getEntriesForDate(date: String): Flow<List<NutritionEntry>>

    @Query("SELECT * FROM nutrition_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<NutritionEntry>>

    @Query("SELECT * FROM nutrition_entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<NutritionEntry>>

    @Query("SELECT * FROM nutrition_entries ORDER BY createdAt DESC")
    suspend fun getAllEntriesOnce(): List<NutritionEntry>

    @Query("SELECT * FROM nutrition_entries WHERE date = :date ORDER BY createdAt DESC")
    suspend fun getEntriesForDateOnce(date: String): List<NutritionEntry>

    @Query("SELECT * FROM nutrition_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getEntriesForDateRangeOnce(startDate: String, endDate: String): List<NutritionEntry>
}

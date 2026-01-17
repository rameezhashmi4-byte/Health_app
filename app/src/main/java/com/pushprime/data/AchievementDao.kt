package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pushprime.model.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY type, threshold")
    fun observeAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getById(id: String): AchievementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(achievements: List<AchievementEntity>)
}

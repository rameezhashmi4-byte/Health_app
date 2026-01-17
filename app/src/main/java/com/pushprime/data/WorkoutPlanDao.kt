package com.pushprime.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pushprime.model.GeneratedWorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {
    @Insert
    suspend fun insert(plan: GeneratedWorkoutPlanEntity): Long

    @Update
    suspend fun update(plan: GeneratedWorkoutPlanEntity)

    @Query("SELECT * FROM generated_workout_plans WHERE id = :id")
    suspend fun getById(id: Long): GeneratedWorkoutPlanEntity?

    @Query("SELECT * FROM generated_workout_plans WHERE isSaved = 1 ORDER BY updatedAt DESC")
    fun getSavedPlans(): Flow<List<GeneratedWorkoutPlanEntity>>

    @Query("DELETE FROM generated_workout_plans WHERE isSaved = 0")
    suspend fun deleteDrafts()

    @Query("DELETE FROM generated_workout_plans WHERE id = :id")
    suspend fun deleteById(id: Long)
}

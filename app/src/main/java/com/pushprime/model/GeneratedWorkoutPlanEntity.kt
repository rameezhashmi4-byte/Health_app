package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_workout_plans")
data class GeneratedWorkoutPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalDurationMinutes: Int,
    val goal: String,
    val timeMinutes: Int,
    val equipment: String,
    val focus: String?,
    val style: String?,
    val planJson: String,
    val exerciseSignature: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false
)

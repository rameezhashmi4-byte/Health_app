package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val threshold: Int,
    val progress: Int,
    val unlocked: Boolean,
    val unlockedAt: Long?,
    val icon: String
)

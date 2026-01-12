package com.pushprime.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Set Entity
 * Individual sets within a gym session
 */
@Entity(
    tableName = "sets",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val setNumber: Int,
    val reps: Int? = null,
    val durationSeconds: Int? = null, // For time-based sets
    val restSeconds: Int? = null,
    val weight: Float? = null, // Optional weight in kg
    val notes: String? = null
)

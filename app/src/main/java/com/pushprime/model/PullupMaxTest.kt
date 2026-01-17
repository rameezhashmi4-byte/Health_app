package com.pushprime.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "pullup_max_tests")
data class PullupMaxTest(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val dateTime: Long = System.currentTimeMillis(),
    val maxReps: Int = 0,
    val formRating: Int? = null
)

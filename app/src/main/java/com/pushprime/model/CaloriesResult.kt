package com.pushprime.model

/**
 * Daily calorie target with optional macro guidance.
 */
data class CaloriesResult(
    val dailyCalories: Int,
    val proteinGrams: Int? = null,
    val carbGrams: Int? = null,
    val fatGrams: Int? = null
)

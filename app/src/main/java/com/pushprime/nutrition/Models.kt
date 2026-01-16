package com.pushprime.nutrition

enum class GoalType {
    CUT,
    MAINTAIN,
    BULK
}

enum class DayType {
    TRAINING,
    REST
}

enum class Sex {
    MALE,
    FEMALE
}

data class CaloriesInput(
    val age: Int,
    val heightCm: Double,
    val weightKg: Double,
    val sex: Sex,
    val activityMultiplier: Double,
    val goal: GoalType
)

data class CaloriesResult(
    val maintenanceCalories: Int,
    val targetCalories: Int,
    val goal: GoalType,
    val explanations: List<String>
)

data class MacroResult(
    val proteinGrams: Int,
    val carbGrams: Int,
    val fatGrams: Int,
    val totalCalories: Int,
    val explanations: List<String>
)

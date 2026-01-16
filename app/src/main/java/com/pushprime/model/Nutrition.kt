package com.pushprime.model

data class NutritionSettings(
    val goal: NutritionGoal = NutritionGoal.MAINTAIN,
    val region: String = "Global",
    val isHalal: Boolean = false,
    val isVeggie: Boolean = false,
    val isBudget: Boolean = false,
    val restaurantMode: Boolean = false
)

enum class NutritionGoal {
    CUT, MAINTAIN, BULK
}

data class Macros(
    val protein: Int,
    val carbs: Int,
    val fats: Int,
    val calories: Int
)

data class MealSuggestion(
    val id: String,
    val name: String,
    val calories: Int,
    val macros: Macros,
    val swapOptions: List<String> = emptyList()
)

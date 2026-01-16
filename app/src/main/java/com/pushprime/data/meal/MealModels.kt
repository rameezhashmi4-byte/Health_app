package com.pushprime.data.meal

data class MealPreferences(
    val excludedIngredients: Set<String> = emptySet(),
    val preferredProteins: Set<String> = emptySet(),
    val preferredCarbs: Set<String> = emptySet(),
    val preferredFats: Set<String> = emptySet(),
    val vegetarian: Boolean = false
)

data class MealPlan(
    val region: Region,
    val targetCalories: Int,
    val meals: List<PlannedMeal>,
    val swapSuggestions: List<SwapSuggestion>,
    val restaurantModeSuggestions: List<String>
)

data class PlannedMeal(
    val mealType: MealType,
    val title: String,
    val portionGuidance: String,
    val targetCalories: Int,
    val components: List<String>
)

data class SwapSuggestion(
    val dish: String,
    val swapWith: String,
    val reason: String
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK
}

enum class Region(val assetFileName: String, val displayName: String) {
    UK("uk.json", "UK"),
    SOUTH_ASIA("south_asia.json", "South Asia"),
    MIDDLE_EAST("middle_east.json", "Middle East"),
    MEDITERRANEAN("mediterranean.json", "Mediterranean")
}

package com.pushprime.data.meal

data class RegionTemplate(
    val region: String,
    val proteinOptions: List<String>,
    val carbOptions: List<String>,
    val fatOptions: List<String>,
    val commonMeals: List<CommonMealTemplate>,
    val restaurantMode: List<String>,
    val swapRules: List<SwapRule>
)

data class CommonMealTemplate(
    val name: String,
    val mealType: MealType,
    val portionGuidance: String,
    val description: String? = null
)

data class SwapRule(
    val dish: String,
    val swapWith: String,
    val reason: String
)

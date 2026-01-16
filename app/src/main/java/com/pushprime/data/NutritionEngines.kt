package com.pushprime.data

import com.pushprime.model.Macros
import com.pushprime.model.NutritionGoal
import com.pushprime.model.NutritionSettings
import com.pushprime.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalorieEngine @Inject constructor() {
    fun calculateTarget(user: User?, settings: NutritionSettings): Int {
        val base = if (user?.gender == User.Gender.MALE) 2500 else 2000
        return when (settings.goal) {
            NutritionGoal.CUT -> base - 500
            NutritionGoal.MAINTAIN -> base
            NutritionGoal.BULK -> base + 500
        }
    }
}

@Singleton
class MacroEngine @Inject constructor() {
    fun calculateMacros(calories: Int, settings: NutritionSettings): Macros {
        return when (settings.goal) {
            NutritionGoal.CUT -> {
                // High protein for cutting
                Macros(
                    protein = (calories * 0.4 / 4).toInt(),
                    carbs = (calories * 0.3 / 4).toInt(),
                    fats = (calories * 0.3 / 9).toInt(),
                    calories = calories
                )
            }
            NutritionGoal.MAINTAIN -> {
                // Balanced
                Macros(
                    protein = (calories * 0.3 / 4).toInt(),
                    carbs = (calories * 0.4 / 4).toInt(),
                    fats = (calories * 0.3 / 9).toInt(),
                    calories = calories
                )
            }
            NutritionGoal.BULK -> {
                // High carb for bulk
                Macros(
                    protein = (calories * 0.25 / 4).toInt(),
                    carbs = (calories * 0.5 / 4).toInt(),
                    fats = (calories * 0.25 / 9).toInt(),
                    calories = calories
                )
            }
        }
    }
}

@Singleton
class MealPlanGenerator @Inject constructor() {
    fun generateSuggestions(settings: NutritionSettings): List<com.pushprime.model.MealSuggestion> {
        val baseMeals = listOf(
            com.pushprime.model.MealSuggestion(
                "1", "Grilled Chicken Salad", 450, 
                Macros(40, 10, 25, 450), listOf("Grilled Tofu", "Roasted Chickpeas")
            ),
            com.pushprime.model.MealSuggestion(
                "2", "Beef & Broccoli Stir-fry", 600, 
                Macros(35, 45, 30, 600), listOf("Prawns", "Tempeh")
            ),
            com.pushprime.model.MealSuggestion(
                "3", "Salmon with Quinoa", 550, 
                Macros(30, 40, 28, 550), listOf("Sea Bass", "Lentils")
            )
        )

        return baseMeals.map { meal ->
            var name = meal.name
            if (settings.isVeggie) {
                name = when(meal.id) {
                    "1" -> "Grilled Tofu Salad"
                    "2" -> "Tempeh & Broccoli Stir-fry"
                    "3" -> "Roasted Cauliflower Quinoa"
                    else -> name
                }
            }
            if (settings.restaurantMode) {
                name = "Restaurant: $name"
            }
            meal.copy(name = name)
        }
    }
}

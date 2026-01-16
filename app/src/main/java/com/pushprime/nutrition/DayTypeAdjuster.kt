package com.pushprime.nutrition

import kotlin.math.roundToInt

class DayTypeAdjuster {
    fun adjustForDayType(
        base: MacroResult,
        dayType: DayType
    ): MacroResult {
        val explanations = base.explanations.toMutableList()
        val proteinCalories = base.proteinGrams * 4
        val currentCarbCalories = base.carbGrams * 4
        val currentFatCalories = base.fatGrams * 9

        val targetCarbCalories = when (dayType) {
            DayType.TRAINING -> (currentCarbCalories * 1.1).roundToInt()
            DayType.REST -> (currentCarbCalories * 0.85).roundToInt()
        }

        val remainingForFat = base.totalCalories - proteinCalories - targetCarbCalories
        val adjustedFatGrams = NutritionValidation.nonNegative(remainingForFat / 9.0)
        val adjustedCarbGrams = NutritionValidation.nonNegative(targetCarbCalories / 4.0)

        explanations.add(
            when (dayType) {
                DayType.TRAINING -> "Training day: carbs +10%, fats adjusted."
                DayType.REST -> "Rest day: carbs -15%, fats adjusted."
            }
        )

        return MacroResult(
            proteinGrams = base.proteinGrams,
            carbGrams = adjustedCarbGrams,
            fatGrams = adjustedFatGrams,
            totalCalories = base.totalCalories,
            explanations = explanations
        )
    }
}

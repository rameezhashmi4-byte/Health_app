package com.pushprime.nutrition

import kotlin.math.roundToInt

class MacroEngine {
    fun calculate(targetCalories: Int, weightKg: Double): MacroResult {
        val explanations = mutableListOf<String>()

        val proteinGrams = (weightKg * 2.0).roundToInt()
        val fatGrams = (weightKg * 0.8).roundToInt()
        val proteinCalories = proteinGrams * 4
        val fatCalories = fatGrams * 9

        explanations.add("Protein set to 2.0 g/kg: $proteinGrams g.")
        explanations.add("Fat set to 0.8 g/kg: $fatGrams g.")

        val remainingCalories = targetCalories - proteinCalories - fatCalories
        val carbGrams = NutritionValidation.nonNegative(remainingCalories / 4.0)
        explanations.add("Carbs fill remaining calories: $carbGrams g.")

        return MacroResult(
            proteinGrams = proteinGrams,
            carbGrams = carbGrams,
            fatGrams = fatGrams,
            totalCalories = targetCalories,
            explanations = explanations
        )
    }
}

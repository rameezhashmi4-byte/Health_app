package com.pushprime.nutrition

import kotlin.math.roundToInt

class CalorieEngine {
    fun calculate(input: CaloriesInput): CaloriesResult {
        val explanations = mutableListOf<String>()
        val bmr = when (input.sex) {
            Sex.MALE -> 10 * input.weightKg + 6.25 * input.heightCm - 5 * input.age + 5
            Sex.FEMALE -> 10 * input.weightKg + 6.25 * input.heightCm - 5 * input.age - 161
        }
        explanations.add("BMR calculated using Mifflin-St Jeor: ${bmr.roundToInt()} kcal.")

        val maintenance = bmr * input.activityMultiplier
        explanations.add("Maintenance calories with activity multiplier (${input.activityMultiplier}): ${maintenance.roundToInt()} kcal.")

        val target = when (input.goal) {
            GoalType.CUT -> maintenance * 0.85
            GoalType.MAINTAIN -> maintenance
            GoalType.BULK -> maintenance * 1.1
        }
        explanations.add(
            "Goal adjustment (${input.goal.name.lowercase()}): ${target.roundToInt()} kcal."
        )

        val (targetClamped, clampNotes) = NutritionValidation.clampCalories(target, input.sex)
        explanations.addAll(clampNotes)

        return CaloriesResult(
            maintenanceCalories = maintenance.roundToInt(),
            targetCalories = targetClamped,
            goal = input.goal,
            explanations = explanations
        )
    }
}

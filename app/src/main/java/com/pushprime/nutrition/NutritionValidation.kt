package com.pushprime.nutrition

import kotlin.math.roundToInt

object NutritionValidation {
    private const val MIN_FEMALE = 1200
    private const val MIN_MALE = 1500
    private const val MAX_CALORIES = 4500

    fun clampCalories(calories: Double, sex: Sex): Pair<Int, List<String>> {
        val explanations = mutableListOf<String>()
        val minCalories = if (sex == Sex.MALE) MIN_MALE else MIN_FEMALE
        var value = calories.roundToInt()
        if (value < minCalories) {
            value = minCalories
            explanations.add("Applied minimum calories for safety ($minCalories kcal).")
        }
        if (value > MAX_CALORIES) {
            value = MAX_CALORIES
            explanations.add("Applied maximum calories cap ($MAX_CALORIES kcal).")
        }
        return value to explanations
    }

    fun nonNegative(value: Double): Int {
        return if (value < 0) 0 else value.roundToInt()
    }
}

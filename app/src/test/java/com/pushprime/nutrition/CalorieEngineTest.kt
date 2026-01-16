package com.pushprime.nutrition

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalorieEngineTest {
    @Test
    fun calculatesMaintenanceAndTarget() {
        val engine = CalorieEngine()
        val input = CaloriesInput(
            age = 30,
            heightCm = 180.0,
            weightKg = 80.0,
            sex = Sex.MALE,
            activityMultiplier = 1.55,
            goal = GoalType.MAINTAIN
        )

        val result = engine.calculate(input)
        assertEquals(2759, result.maintenanceCalories)
        assertEquals(2759, result.targetCalories)
        assertTrue(result.explanations.isNotEmpty())
    }

    @Test
    fun appliesMinimumCalories() {
        val engine = CalorieEngine()
        val input = CaloriesInput(
            age = 60,
            heightCm = 150.0,
            weightKg = 40.0,
            sex = Sex.FEMALE,
            activityMultiplier = 1.2,
            goal = GoalType.CUT
        )

        val result = engine.calculate(input)
        assertEquals(1200, result.targetCalories)
        assertTrue(result.explanations.any { it.contains("minimum") })
    }
}

package com.pushprime.nutrition

import org.junit.Assert.assertEquals
import org.junit.Test

class DayTypeAdjusterTest {
    @Test
    fun trainingDayAdjustsCarbsUp() {
        val base = MacroResult(
            proteinGrams = 160,
            carbGrams = 196,
            fatGrams = 64,
            totalCalories = 2000,
            explanations = emptyList()
        )
        val adjuster = DayTypeAdjuster()
        val result = adjuster.adjustForDayType(base, DayType.TRAINING)

        assertEquals(160, result.proteinGrams)
        assertEquals(216, result.carbGrams)
        assertEquals(55, result.fatGrams)
    }
}

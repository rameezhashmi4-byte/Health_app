package com.pushprime.nutrition

import org.junit.Assert.assertEquals
import org.junit.Test

class MacroEngineTest {
    @Test
    fun calculatesProteinFirstSplit() {
        val engine = MacroEngine()
        val result = engine.calculate(targetCalories = 2000, weightKg = 80.0)

        assertEquals(160, result.proteinGrams)
        assertEquals(64, result.fatGrams)
        assertEquals(196, result.carbGrams)
    }
}

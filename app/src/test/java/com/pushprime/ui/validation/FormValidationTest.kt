package com.pushprime.ui.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidationTest {
    @Test
    fun isBlank_trimsWhitespace() {
        assertTrue(FormValidation.isBlank("   "))
        assertFalse(FormValidation.isBlank(" a "))
    }

    @Test
    fun hasMinLength_trimsInput() {
        assertTrue(FormValidation.hasMinLength(" 12345 ", 5))
        assertFalse(FormValidation.hasMinLength(" 1234 ", 5))
    }

    @Test
    fun trim_removesWhitespace() {
        assertEquals("ramboost", FormValidation.trim(" ramboost "))
    }
}

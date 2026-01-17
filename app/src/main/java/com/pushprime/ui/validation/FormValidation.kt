package com.pushprime.ui.validation

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

object FormValidation {
    fun isBlank(value: String): Boolean = value.trim().isEmpty()

    fun hasMinLength(value: String, minLength: Int): Boolean {
        return value.trim().length >= minLength
    }

    fun trim(value: String): String = value.trim()
}

@Stable
class FormValidationState(
    attemptedSubmit: Boolean = false,
    touchedFields: Set<String> = emptySet()
) {
    var attemptedSubmit by mutableStateOf(attemptedSubmit)
        private set

    var touchedFields by mutableStateOf(touchedFields)
        private set

    fun shouldShowError(fieldKey: String): Boolean {
        return attemptedSubmit || touchedFields.contains(fieldKey)
    }

    fun markTouched(fieldKey: String) {
        if (!touchedFields.contains(fieldKey)) {
            touchedFields = touchedFields + fieldKey
        }
    }

    fun markSubmitAttempt() {
        attemptedSubmit = true
    }

    fun reset() {
        attemptedSubmit = false
        touchedFields = emptySet()
    }
}

@Composable
fun rememberFormValidationState(): FormValidationState {
    return remember { FormValidationState() }
}

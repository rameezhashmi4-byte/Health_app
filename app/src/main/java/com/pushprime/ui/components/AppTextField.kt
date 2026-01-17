package com.pushprime.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

private val AppTextFieldShape = RoundedCornerShape(16.dp)
private val AppTextFieldMinHeight = 52.dp
private val AppTextFieldPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    placeholder: String? = null,
    required: Boolean = false,
    trimValidation: Boolean = true,
    errorText: String? = null,
    helperText: String? = null,
    showError: Boolean? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    onFocusChanged: ((FocusState) -> Unit)? = null
) {
    var touched by rememberSaveable { mutableStateOf(false) }
    var hadFocus by remember { mutableStateOf(false) }
    val trimmedValue = if (trimValidation) value.trim() else value
    val requiredError = if (required && trimmedValue.isEmpty()) "Required" else null
    val resolvedError = errorText ?: requiredError
    val shouldShowError = resolvedError != null && (showError ?: touched)
    val labelText = if (required && label.isNotBlank()) "$label *" else label

    val focusModifier = Modifier.onFocusChanged { state ->
        if (state.isFocused) {
            hadFocus = true
        } else if (hadFocus) {
            touched = true
        }
        onFocusChanged?.invoke(state)
    }
    val focusRequesterModifier = focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val subtlePrimary = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = fieldModifier
                .fillMaxWidth()
                .heightIn(min = AppTextFieldMinHeight)
                .then(focusRequesterModifier)
                .then(focusModifier),
            label = if (labelText.isNotBlank()) {
                { Text(text = labelText, style = MaterialTheme.typography.labelLarge) }
            } else null,
            placeholder = placeholder?.let {
                { Text(text = it, style = MaterialTheme.typography.bodyLarge, color = placeholderColor) }
            },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = singleLine,
            enabled = enabled,
            readOnly = readOnly,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = shouldShowError,
            shape = AppTextFieldShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = subtlePrimary,
                unfocusedBorderColor = borderColor,
                disabledBorderColor = borderColor.copy(alpha = 0.4f),
                errorBorderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                errorLabelColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                errorTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = containerColor,
                unfocusedContainerColor = containerColor,
                disabledContainerColor = containerColor.copy(alpha = 0.6f),
                errorContainerColor = containerColor
            )
        )
        if (shouldShowError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = resolvedError.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else if (helperText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

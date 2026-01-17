package com.pushprime.ui.screens.profile_setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.SexOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onFinished: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()

    var showErrors by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Step ${step + 1} of 4",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when (step) {
            0 -> WelcomeStep(onContinue = { viewModel.nextStep() })
            1 -> BasicDetailsStep(
                onBack = { viewModel.prevStep() },
                onContinue = { fullName, goal, experience ->
                    viewModel.fullName = fullName
                    viewModel.goal = goal
                    viewModel.experience = experience
                    viewModel.nextStep()
                }
            )
            2 -> BodyStatsStep(
                viewModel = viewModel,
                showErrors = showErrors,
                onBack = { viewModel.prevStep() },
                onContinue = {
                    showErrors = true
                    if ((viewModel.weightKg ?: 0.0) > 0 && (viewModel.heightCm ?: 0.0) > 0) {
                        viewModel.nextStep()
                        showErrors = false
                    }
                }
            )
            3 -> PermissionsStep(
                viewModel = viewModel,
                isSaving = isSaving,
                onBack = { viewModel.prevStep() },
                onFinish = {
                    viewModel.saveProfile(onFinished)
                }
            )
        }

        if (error != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Text(
        text = "Welcome to RAMBOOST",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Let’s set up your profile in 30 seconds.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Button(
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text("Continue")
    }
}

@Composable
private fun BasicDetailsStep(
    onBack: () -> Unit,
    onContinue: (String, FitnessGoal, ExperienceLevel) -> Unit
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var selectedGoal by rememberSaveable { mutableStateOf<FitnessGoal?>(null) }
    var selectedExperience by rememberSaveable { mutableStateOf<ExperienceLevel?>(null) }
    val isFullNameValid = fullName.trim().isNotEmpty()
    val shouldShowNameError = fullName.isNotEmpty() && !isFullNameValid
    val isFormValid = isFullNameValid && selectedGoal != null && selectedExperience != null

    Text(
        text = "Basic Details",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        isError = shouldShowNameError
    )
    GoalSelector(
        selected = selectedGoal,
        onSelected = { selectedGoal = it }
    )
    ExperienceSelector(
        selected = selectedExperience,
        onSelected = { selectedExperience = it }
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
        Button(
            onClick = {
                onContinue(
                    fullName.trim(),
                    selectedGoal ?: return@Button,
                    selectedExperience ?: return@Button
                )
            },
            enabled = isFormValid,
            modifier = Modifier.weight(1f)
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun BodyStatsStep(
    viewModel: ProfileSetupViewModel,
    showErrors: Boolean,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    Text(
        text = "Body Stats",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    OutlinedTextField(
        value = viewModel.weightKg?.toString().orEmpty(),
        onValueChange = { viewModel.weightKg = it.toDoubleOrNull() },
        label = { Text("Weight (kg)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = showErrors && (viewModel.weightKg ?: 0.0) <= 0
    )
    OutlinedTextField(
        value = viewModel.heightCm?.toString().orEmpty(),
        onValueChange = { viewModel.heightCm = it.toDoubleOrNull() },
        label = { Text("Height (cm)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        isError = showErrors && (viewModel.heightCm ?: 0.0) <= 0
    )
    OutlinedTextField(
        value = viewModel.age?.toString().orEmpty(),
        onValueChange = { viewModel.age = it.toIntOrNull() },
        label = { Text("Age (optional)") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
    SexSelector(
        selected = viewModel.sex,
        onSelected = { viewModel.sex = it }
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
        Button(onClick = onContinue, modifier = Modifier.weight(1f)) { Text("Continue") }
    }
}

@Composable
private fun PermissionsStep(
    viewModel: ProfileSetupViewModel,
    isSaving: Boolean,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Text(
        text = "Allow step tracking?",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "RAMBOOST can use your phone’s step data to track progress automatically.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Button(
        onClick = {
            viewModel.requestStepTrackingPermission {
                viewModel.toggleStepTracking(true)
                onFinish()
            }
        },
        enabled = !isSaving,
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) {
        Text("Enable step tracking")
    }
    TextButton(
        onClick = {
            viewModel.toggleStepTracking(false)
            onFinish()
        },
        enabled = !isSaving
    ) {
        Text("Not now")
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), enabled = !isSaving) {
        Text("Back")
    }
}

@Composable
private fun GoalSelector(
    selected: FitnessGoal?,
    onSelected: (FitnessGoal) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Fitness Goal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        FitnessGoal.values().forEach { goal ->
            SelectionRow(
                label = goal.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                selected = goal == selected,
                onClick = { onSelected(goal) }
            )
        }
    }
}

@Composable
private fun ExperienceSelector(
    selected: ExperienceLevel?,
    onSelected: (ExperienceLevel) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Experience Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        ExperienceLevel.values().forEach { level ->
            SelectionRow(
                label = level.name.lowercase().replaceFirstChar { it.uppercase() },
                selected = level == selected,
                onClick = { onSelected(level) }
            )
        }
    }
}

@Composable
private fun SexSelector(
    selected: SexOption?,
    onSelected: (SexOption?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Sex (optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        SexOption.values().forEach { sex ->
            SelectionRow(
                label = sex.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                selected = sex == selected,
                onClick = { onSelected(sex) }
            )
        }
        TextButton(onClick = { onSelected(null) }) {
            Text("Prefer not to say")
        }
    }
}

@Composable
private fun SelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (selected) {
                androidx.compose.material3.Icon(Icons.Default.Check, contentDescription = null)
            }
        }
    }
}

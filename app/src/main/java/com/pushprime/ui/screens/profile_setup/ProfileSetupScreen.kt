package com.pushprime.ui.screens.profile_setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pushprime.ui.components.AppPrimaryButton
import com.pushprime.ui.components.AppSecondaryButton
import com.pushprime.ui.components.AppSelectionCard
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppTextButton
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.theme.AppSpacing
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.SexOption
import com.pushprime.ui.validation.FormValidation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onFinished: () -> Unit
) {
    val step by viewModel.step.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val error by viewModel.error.collectAsState()

    PremiumFadeSlideIn(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            Text(
                text = "Step ${step + 1} of 4",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
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
                        onBack = { viewModel.prevStep() },
                        onContinue = { viewModel.nextStep() }
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
            }

            if (error != null) {
                AppCard(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentPadding = PaddingValues(AppSpacing.md)
                ) {
                    Text(
                        text = error ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Welcome to RAMBOOST",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Let’s set up your profile in 30 seconds.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        AppPrimaryButton(
            text = "Continue",
            onClick = onContinue
        )
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
    val isFullNameValid = !FormValidation.isBlank(fullName)
    val isFormValid = isFullNameValid && selectedGoal != null && selectedExperience != null

    Column(modifier = Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            Text(
                text = "Basic Details",
                style = MaterialTheme.typography.headlineMedium
            )
            AppTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                modifier = Modifier.fillMaxWidth(),
                fieldModifier = Modifier.testTag("profile_setup_full_name"),
                required = true
            )
            GoalSelector(
                selected = selectedGoal,
                onSelected = { selectedGoal = it }
            )
            ExperienceSelector(
                selected = selectedExperience,
                onSelected = { selectedExperience = it }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            AppSecondaryButton(text = "Back", modifier = Modifier.weight(1f), onClick = onBack)
            AppPrimaryButton(
                text = "Continue",
                enabled = isFormValid,
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile_setup_continue"),
                onClick = {
                    if (isFormValid) {
                        onContinue(
                            FormValidation.trim(fullName),
                            selectedGoal ?: return@AppPrimaryButton,
                            selectedExperience ?: return@AppPrimaryButton
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun BodyStatsStep(
    viewModel: ProfileSetupViewModel,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var weightInput by rememberSaveable(viewModel.weightKg) {
        mutableStateOf(viewModel.weightKg?.toString().orEmpty())
    }
    var heightInput by rememberSaveable(viewModel.heightCm) {
        mutableStateOf(viewModel.heightCm?.toString().orEmpty())
    }
    var ageInput by rememberSaveable(viewModel.age) {
        mutableStateOf(viewModel.age?.toString().orEmpty())
    }

    val isWeightValid = (viewModel.weightKg ?: 0.0) > 0
    val isHeightValid = (viewModel.heightCm ?: 0.0) > 0
    val weightError = if (!isWeightValid) "Enter a valid weight" else null
    val heightError = if (!isHeightValid) "Enter a valid height" else null
    val isFormValid = isWeightValid && isHeightValid

    Column(modifier = Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Body Stats",
                style = MaterialTheme.typography.headlineMedium
            )
            AppTextField(
                value = weightInput,
                onValueChange = {
                    weightInput = it
                    viewModel.weightKg = it.toDoubleOrNull()
                },
                label = "Weight (kg)",
                modifier = Modifier.fillMaxWidth(),
                required = true,
                errorText = weightError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            AppTextField(
                value = heightInput,
                onValueChange = {
                    heightInput = it
                    viewModel.heightCm = it.toDoubleOrNull()
                },
                label = "Height (cm)",
                modifier = Modifier.fillMaxWidth(),
                required = true,
                errorText = heightError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            AppTextField(
                value = ageInput,
                onValueChange = {
                    ageInput = it
                    viewModel.age = it.toIntOrNull()
                },
                label = "Age (optional)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            SexSelector(
                selected = viewModel.sex,
                onSelected = { viewModel.sex = it }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            AppSecondaryButton(text = "Back", modifier = Modifier.weight(1f), onClick = onBack)
            AppPrimaryButton(
                text = "Continue",
                modifier = Modifier.weight(1f),
                enabled = isFormValid,
                onClick = {
                    if (isFormValid) {
                        onContinue()
                    }
                }
            )
        }
    }
}

@Composable
private fun PermissionsStep(
    viewModel: ProfileSetupViewModel,
    isSaving: Boolean,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Allow step tracking?",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "RAMBOOST can use your phone’s step data to track progress automatically.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            AppPrimaryButton(
                text = "Enable step tracking",
                enabled = !isSaving,
                onClick = {
                    viewModel.requestStepTrackingPermission {
                        viewModel.toggleStepTracking(true)
                        onFinish()
                    }
                }
            )
            AppSecondaryButton(
                text = "Not now",
                enabled = !isSaving,
                onClick = {
                    viewModel.toggleStepTracking(false)
                    onFinish()
                }
            )
            AppTextButton(
                text = "Back",
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                onClick = onBack
            )
        }
    }
}

@Composable
private fun GoalSelector(
    selected: FitnessGoal?,
    onSelected: (FitnessGoal) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Fitness Goal", style = MaterialTheme.typography.titleLarge)
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
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Experience Level", style = MaterialTheme.typography.titleLarge)
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
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text("Sex (optional)", style = MaterialTheme.typography.titleLarge)
        SexOption.values().forEach { sex ->
            SelectionRow(
                label = sex.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                selected = sex == selected,
                onClick = { onSelected(sex) }
            )
        }
        AppTextButton(text = "Prefer not to say", onClick = { onSelected(null) }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun SelectionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AppSelectionCard(
        label = label,
        selected = selected,
        onClick = onClick
    )
}

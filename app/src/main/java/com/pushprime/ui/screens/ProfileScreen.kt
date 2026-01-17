package com.pushprime.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pushprime.coach.CoachFrequency
import com.pushprime.coach.CoachIntelligence
import com.pushprime.coach.CoachSettings
import com.pushprime.coach.CoachStyle
import com.pushprime.coach.VoiceProviderType
import com.pushprime.data.AiCoachMode
import com.pushprime.data.AiCoachSettingsRepository
import com.pushprime.data.AppDatabase
import com.pushprime.data.CoachSettingsRepository
import com.pushprime.data.NutritionRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.ProfileRepository
import com.pushprime.data.SessionDao
import com.pushprime.data.StepsDay
import com.pushprime.data.StepsRepository
import com.pushprime.data.StreakRepository
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.UserProfile
import com.pushprime.ui.components.DailyDataPoint
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.components.RamboostCard
import com.pushprime.ui.components.RamboostPrimaryButton
import com.pushprime.ui.components.RamboostSecondaryButton
import com.pushprime.ui.components.RamboostStatTile
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.components.WeeklyTrendChart
import com.pushprime.ui.screens.common.ErrorScreen
import com.pushprime.ui.theme.AppSpacing
import com.pushprime.ui.validation.FormValidation
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    localStore: LocalStore,
    sessionDao: SessionDao?,
    currentUserId: String?,
    onLogout: () -> Unit,
    onNavigateToShareProgress: (() -> Unit)? = null,
    onNavigateToAchievements: (() -> Unit)? = null,
    onNavigateToPhotoVault: (() -> Unit)? = null,
    onNavigateToNotificationSettings: (() -> Unit)? = null,
    onNavigateToNutritionGoals: (() -> Unit)? = null,
    onNavigateToAiSetup: (() -> Unit)? = null,
    onNavigateToVoiceCoachSettings: (() -> Unit)? = null,
    onNavigateToCoachChat: (() -> Unit)? = null,
    onNavigateToMusicSettings: (() -> Unit)? = null,
    onNavigateToAccount: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (sessionDao == null) {
        ErrorScreen(message = "Database not available")
        return
    }

    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val nutritionRepository = remember { NutritionRepository(context) }
    val streakRepository = remember(sessionDao) {
        StreakRepository(
            context = context,
            sessionDao = sessionDao,
            dailyStatusDao = database.dailyStatusDao(),
            nutritionRepository = nutritionRepository,
            pullupSessionDao = database.pullupSessionDao(),
            pullupMaxTestDao = database.pullupMaxTestDao()
        )
    }
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(
            localStore = localStore,
            sessionDao = sessionDao,
            stepsRepository = StepsRepository(context),
            profileRepository = ProfileRepository(localStore),
            streakRepository = streakRepository
        )
    )

    val user by localStore.user.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()
    var showEditSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coachSettingsRepository = remember { CoachSettingsRepository(context) }
    val coachSettings by coachSettingsRepository.settings.collectAsState(initial = CoachSettings())
    val aiCoachSettingsRepository = remember { AiCoachSettingsRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUserId, user?.username) {
        viewModel.loadProfile(currentUserId, user?.username ?: "RAMBOOST User")
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event.useToast) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            } else {
                snackbarHostState.showSnackbar(event.message)
            }
            if (event.dismissSheet) {
                showEditSheet = false
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PremiumFadeSlideIn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                item {
                    HeaderSection(profile = uiState.profile)
                }

                item {
                    ProfileDetailsCard(
                        profile = uiState.profile,
                        onEditClick = { showEditSheet = true }
                    )
                }

                item {
                    StatsRow(
                        streakDays = uiState.streakDays,
                        sessionsThisWeek = uiState.sessionsThisWeek,
                        stepsToday = if (uiState.stepTrackingEnabled) uiState.stepsToday else null
                    )
                }

                item {
                    StreakSummaryCard(
                        hasSessions = uiState.hasSessions,
                        streakDays = uiState.streakDays,
                        lastWorkoutLabel = uiState.lastWorkoutLabel
                    )
                }

                item {
                    StepsSection(
                        stepsEnabled = uiState.stepTrackingEnabled,
                        weeklySteps = uiState.weeklySteps,
                        onEnableSteps = { viewModel.setStepTrackingEnabled(true) }
                    )
                }

                item {
                    CoachSettingsSection(
                        settings = coachSettings,
                        onHybridToggle = { enabled ->
                            coroutineScope.launch { coachSettingsRepository.setHybridEnabled(enabled) }
                        },
                        onIntelligenceChange = { value ->
                            coroutineScope.launch {
                                coachSettingsRepository.setCoachIntelligence(value)
                                val mode = if (value == CoachIntelligence.BASIC) {
                                    AiCoachMode.BASIC
                                } else {
                                    AiCoachMode.OPENAI
                                }
                                aiCoachSettingsRepository.updateMode(mode)
                            }
                        },
                        onVoiceProviderChange = { value ->
                            coroutineScope.launch { coachSettingsRepository.setVoiceProvider(value) }
                        },
                        onStyleChange = { value ->
                            coroutineScope.launch { coachSettingsRepository.setCoachStyle(value) }
                        },
                        onFrequencyChange = { value ->
                            coroutineScope.launch { coachSettingsRepository.setCoachFrequency(value) }
                        }
                    )
                }

                item {
                    ActionsSection(
                        stepsEnabled = uiState.stepTrackingEnabled,
                        onEnableSteps = { viewModel.setStepTrackingEnabled(true) },
                        onExport = { viewModel.onExportData() },
                        onPrivacy = { viewModel.onPrivacy() },
                        onLogout = onLogout,
                        onNavigateToShareProgress = onNavigateToShareProgress,
                        onNavigateToAchievements = onNavigateToAchievements,
                        onNavigateToPhotoVault = onNavigateToPhotoVault,
                        onNavigateToNotificationSettings = onNavigateToNotificationSettings,
                        onNavigateToNutritionGoals = onNavigateToNutritionGoals,
                        onNavigateToAiSetup = onNavigateToAiSetup,
                        onNavigateToCoachChat = onNavigateToCoachChat,
                        onNavigateToVoiceCoachSettings = onNavigateToVoiceCoachSettings,
                        onNavigateToMusicSettings = onNavigateToMusicSettings,
                        onNavigateToAccount = onNavigateToAccount
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xxl))
                }
            }
        }
    }

    if (showEditSheet) {
        EditProfileSheet(
            profile = uiState.profile,
            isSaving = uiState.isSaving,
            onDismiss = { showEditSheet = false },
            onSave = viewModel::saveProfile
        )
    }
}

@Composable
private fun HeaderSection(profile: UserProfile) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Settings & your progress",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initialsForName(profile.fullName),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(AppSpacing.lg))
            Column {
                Text(
                    text = profile.fullName.ifBlank { "RAMBOOST User" },
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = profile.experience.label(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailsCard(
    profile: UserProfile,
    onEditClick: () -> Unit
) {
    RamboostCard(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profile Details",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                    Text(text = "Edit")
                }
            }
            ProfileDetailRow(label = "Name", value = profile.fullName.ifBlank { "Not set" })
            ProfileDetailRow(label = "Goal", value = profile.goal.label())
            ProfileDetailRow(label = "Experience", value = profile.experience.label())
            ProfileDetailRow(label = "Weight", value = profile.weightKg.formatOptional("kg"))
            ProfileDetailRow(label = "Height", value = profile.heightCm.formatOptional("cm"))
        }
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun StatsRow(streakDays: Int, sessionsThisWeek: Int, stepsToday: Long?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        RamboostStatTile(
            label = "Streak",
            value = "$streakDays",
            modifier = Modifier.weight(1f)
        )
        RamboostStatTile(
            label = "Sessions",
            value = "$sessionsThisWeek",
            modifier = Modifier.weight(1f)
        )
        RamboostStatTile(
            label = "Steps",
            value = stepsToday?.toString() ?: "Off",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StreakSummaryCard(
    hasSessions: Boolean,
    streakDays: Int,
    lastWorkoutLabel: String?
) {
    RamboostCard(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            if (!hasSessions) {
                Text(
                    text = "No sessions yet — start your first workout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Current streak: $streakDays days",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Last: ${lastWorkoutLabel ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepsSection(
    stepsEnabled: Boolean,
    weeklySteps: List<StepsDay>,
    onEnableSteps: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = "Weekly step trend",
            style = MaterialTheme.typography.titleLarge
        )
        if (stepsEnabled) {
            WeeklyTrendChart(
                weeklyData = weeklySteps.map { it.toDailyDataPoint() },
                exerciseName = "Steps"
            )
        } else {
            RamboostCard(containerColor = MaterialTheme.colorScheme.surface) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
                ) {
                    Text(
                        text = "Step tracking is off",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    RamboostPrimaryButton(text = "Enable", onClick = onEnableSteps)
                }
            }
        }
    }
}

@Composable
private fun CoachSettingsSection(
    settings: CoachSettings,
    onHybridToggle: (Boolean) -> Unit,
    onIntelligenceChange: (CoachIntelligence) -> Unit,
    onVoiceProviderChange: (VoiceProviderType) -> Unit,
    onStyleChange: (CoachStyle) -> Unit,
    onFrequencyChange: (CoachFrequency) -> Unit
) {
    RamboostCard(containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Text(
                text = "Coach Settings",
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Hybrid coach", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = settings.hybridEnabled,
                    onCheckedChange = onHybridToggle
                )
            }

            CoachOptionRow(
                label = "Intelligence",
                value = settings.intelligence.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                onNext = {
                    val values = CoachIntelligence.values()
                    val next = values[(settings.intelligence.ordinal + 1) % values.size]
                    onIntelligenceChange(next)
                }
            )
            CoachOptionRow(
                label = "Voice provider",
                value = settings.voiceProvider.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                onNext = {
                    val values = VoiceProviderType.values()
                    val next = values[(settings.voiceProvider.ordinal + 1) % values.size]
                    onVoiceProviderChange(next)
                }
            )
            CoachOptionRow(
                label = "Style",
                value = settings.style.name.lowercase().replaceFirstChar { it.uppercase() },
                onNext = {
                    val values = CoachStyle.values()
                    val next = values[(settings.style.ordinal + 1) % values.size]
                    onStyleChange(next)
                }
            )
            CoachOptionRow(
                label = "Frequency",
                value = settings.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                onNext = {
                    val values = CoachFrequency.values()
                    val next = values[(settings.frequency.ordinal + 1) % values.size]
                    onFrequencyChange(next)
                }
            )
        }
    }
}

@Composable
private fun CoachOptionRow(
    label: String,
    value: String,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextButton(onClick = onNext) {
            Text(text = "Change")
        }
    }
}

@Composable
private fun ActionsSection(
    stepsEnabled: Boolean,
    onEnableSteps: () -> Unit,
    onExport: () -> Unit,
    onPrivacy: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToShareProgress: (() -> Unit)?,
    onNavigateToAchievements: (() -> Unit)?,
    onNavigateToPhotoVault: (() -> Unit)?,
    onNavigateToNotificationSettings: (() -> Unit)?,
    onNavigateToNutritionGoals: (() -> Unit)?,
    onNavigateToAiSetup: (() -> Unit)?,
    onNavigateToCoachChat: (() -> Unit)?,
    onNavigateToVoiceCoachSettings: (() -> Unit)?,
    onNavigateToMusicSettings: (() -> Unit)?,
    onNavigateToAccount: (() -> Unit)?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleLarge
        )
        if (!stepsEnabled) {
            RamboostSecondaryButton(
                text = "Enable step tracking",
                onClick = onEnableSteps
            )
        }
        
        val actions = listOfNotNull(
            onNavigateToAccount?.let { "Account" to it },
            onNavigateToCoachChat?.let { "AI Coach Chat" to it },
            onNavigateToShareProgress?.let { "Share progress" to it },
            onNavigateToAchievements?.let { "Achievements" to it },
            onNavigateToPhotoVault?.let { "Photo vault" to it },
            onNavigateToNotificationSettings?.let { "Notifications" to it },
            onNavigateToNutritionGoals?.let { "Nutrition goals" to it },
            onNavigateToAiSetup?.let { "AI Coach setup" to it },
            onNavigateToVoiceCoachSettings?.let { "Voice coach" to it },
            onNavigateToMusicSettings?.let { "Music mode" to it }
        )

        actions.forEach { (label, navigate) ->
            RamboostSecondaryButton(
                text = label,
                onClick = navigate
            )
        }

        RamboostSecondaryButton(
            text = "Export my data",
            onClick = onExport
        )
        RamboostSecondaryButton(
            text = "Privacy",
            onClick = onPrivacy
        )
        
        Spacer(modifier = Modifier.height(AppSpacing.md))
        
        RamboostPrimaryButton(
            text = "Log out",
            onClick = onLogout,
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditProfileSheet(
    profile: UserProfile,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ProfileEditInput) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(profile) { mutableStateOf(profile.fullName) }
    var weight by remember(profile) { mutableStateOf(profile.weightKg.takeIf { it > 0 }?.toInt()?.toString().orEmpty()) }
    var height by remember(profile) { mutableStateOf(profile.heightCm.takeIf { it > 0 }?.toInt()?.toString().orEmpty()) }
    var selectedGoal by remember(profile) { mutableStateOf(profile.goal) }
    var selectedExperience by remember(profile) { mutableStateOf(profile.experience) }

    val weightValue = weight.toDoubleOrNull() ?: 0.0
    val heightValue = height.toDoubleOrNull() ?: profile.heightCm
    val isNameValid = !FormValidation.isBlank(name)
    val isWeightValid = weightValue > 0.0
    val canSave = isNameValid && isWeightValid && !isSaving

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg)
                .padding(bottom = AppSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleLarge
            )

            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Name",
                required = true,
                errorText = if (!isNameValid) "Name is required" else null,
                fieldModifier = Modifier.testTag("profile_edit_name")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(
                        label = "Goal",
                        selected = selectedGoal.label(),
                        options = FitnessGoal.values().toList(),
                        onOptionSelected = { selectedGoal = it },
                        optionLabel = { it.label() }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    DropdownField(
                        label = "Experience",
                        selected = selectedExperience.label(),
                        options = ExperienceLevel.values().toList(),
                        onOptionSelected = { selectedExperience = it },
                        optionLabel = { it.label() }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                AppTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Weight (kg)",
                    required = true,
                    errorText = if (!isWeightValid) "Required" else null,
                    fieldModifier = Modifier.weight(1f).testTag("profile_edit_weight")
                )

                AppTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = "Height (cm)",
                    enabled = false,
                    fieldModifier = Modifier.weight(1f).testTag("profile_edit_height")
                )
            }

            RamboostPrimaryButton(
                text = "Save Changes",
                onClick = {
                    if (canSave) {
                        onSave(
                            ProfileEditInput(
                                name = FormValidation.trim(name),
                                goal = selectedGoal,
                                experience = selectedExperience,
                                weightKg = weightValue,
                                heightCm = heightValue
                            )
                        )
                    }
                },
                enabled = canSave,
                loading = isSaving,
                modifier = Modifier.testTag("profile_edit_save")
            )
        }
    }
}

@Composable
private fun <T> DropdownField(
    label: String,
    selected: String,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box {
            AppTextField(
                value = selected,
                onValueChange = {},
                label = "",
                readOnly = true,
                fieldModifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text("Change")
                    }
                }
            )
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(optionLabel(option)) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private fun FitnessGoal.label(): String {
    return when (this) {
        FitnessGoal.LOSE_FAT -> "Lose fat"
        FitnessGoal.BUILD_MUSCLE -> "Build muscle"
        FitnessGoal.GET_STRONGER -> "Get stronger"
        FitnessGoal.IMPROVE_STAMINA -> "Improve stamina"
    }
}

private fun ExperienceLevel.label(): String {
    return when (this) {
        ExperienceLevel.BEGINNER -> "Beginner"
        ExperienceLevel.INTERMEDIATE -> "Intermediate"
        ExperienceLevel.ADVANCED -> "Advanced"
    }
}

private fun Double.formatOptional(unit: String): String {
    return if (this > 0) {
        "${this.toInt()} $unit"
    } else {
        "Not set"
    }
}

private fun initialsForName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "RB"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

private fun StepsDay.toDailyDataPoint(): DailyDataPoint {
    val date = Date.from(this.date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    return DailyDataPoint(date = date, total = this.steps.toInt())
}

private fun CoachIntelligence.label(): String {
    return when (this) {
        CoachIntelligence.BASIC -> "Basic (no AI)"
        CoachIntelligence.AI_TEXT -> "AI Coach (text only)"
        CoachIntelligence.AI_VOICE -> "AI + Voice (Hybrid)"
    }
}

private fun VoiceProviderType.label(): String {
    return when (this) {
        VoiceProviderType.SYSTEM -> "System Voice (offline)"
        VoiceProviderType.OPENAI -> "AI Voice (OpenAI)"
    }
}

private fun CoachStyle.label(): String {
    return when (this) {
        CoachStyle.CALM -> "Calm"
        CoachStyle.FRIENDLY -> "Friendly"
        CoachStyle.HYPE -> "Hype"
        CoachStyle.MILITARY -> "Military"
    }
}

private fun CoachFrequency.label(): String {
    return when (this) {
        CoachFrequency.LOW -> "Low (every 120s)"
        CoachFrequency.MEDIUM -> "Medium (every 60s)"
        CoachFrequency.HIGH -> "High (every 30s)"
    }
}

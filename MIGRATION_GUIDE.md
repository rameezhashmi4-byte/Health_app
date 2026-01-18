# Migration Guide: Legacy AI → OpenAI Responses API

This guide helps you migrate existing AI code to the new structured outputs implementation.

## Overview

**Before:** Manual JSON parsing, prone to errors
**After:** Strict JSON schemas, type-safe, crash-proof

## Changes Summary

### ✅ Backward Compatible
- `OpenAiCoachProvider` - Updated internally, same interface
- `AiCoachViewModel` - No changes needed
- All existing UI code continues to work

### 🆕 New Features
- `OpenAiWorkoutPlanGenerator` - AI-powered workout generation
- `OpenAiCoachMessageGenerator` - Structured coach messages
- `EnhancedWorkoutGenerator` - Smart fallback system
- `AiErrorMessages` - User-friendly error formatting

## Migration Steps

### 1. Coach Messages (Already Done ✅)

**Before:**
```kotlin
class OpenAiCoachProvider(
    private val apiKey: String,
    private val modelName: String
) : AiCoachProvider {
    override suspend fun sendMessage(
        userMessage: String, 
        contextSummary: String
    ): String {
        // Manual JSON building
        // Manual response parsing
        // Basic error messages
    }
}
```

**After:**
```kotlin
class OpenAiCoachProvider(
    private val apiKey: String,
    private val modelName: String,
    private val baseUrl: String = "https://api.openai.com",
    private val client: OkHttpClient = OkHttpClient()
) : AiCoachProvider {
    private val generator = OpenAiCoachMessageGenerator(
        apiKey, modelName, baseUrl, client
    )

    override suspend fun sendMessage(
        userMessage: String, 
        contextSummary: String
    ): String {
        val result = generator.generateCoachMessage(userMessage, contextSummary)
        return result.fold(
            onSuccess = { msg -> msg.message },
            onFailure = { error -> formatUserFriendlyError(error) }
        )
    }
}
```

**Benefits:**
- ✅ Strict JSON schema (no markdown)
- ✅ Validation before returning
- ✅ Better error messages
- ✅ Same interface (no breaking changes)

### 2. Workout Generation (New Feature)

#### Option A: Local Generation Only (Current)
```kotlin
// WorkoutGeneratorSetupViewModel.kt
val generator = WorkoutGenerator()
val plan = generator.generate(inputs, avoidExercises)
// Always works, offline
```

#### Option B: AI Generation with Manual Fallback
```kotlin
// New approach with explicit fallback
val aiGenerator = OpenAiWorkoutPlanGenerator(apiKey, model, baseUrl)
val localGenerator = WorkoutGenerator()

val result = aiGenerator.generateWorkoutPlan(...)
val plan = result.getOrElse { 
    // Fallback to local
    localGenerator.generate(inputs, avoidExercises)
}
```

#### Option C: Enhanced Generator (Recommended)
```kotlin
// Automatic fallback, best user experience
val generator = EnhancedWorkoutGenerator()

val result = generator.generateWithFallback(
    apiKey = settings.openAiKey, // Can be null
    model = settings.modelName,
    baseUrl = settings.baseUrl,
    goal = goal,
    timeMinutes = timeMinutes,
    equipment = equipment,
    focus = focus,
    style = style,
    avoidExercises = avoidExercises
)

when (result) {
    is WorkoutGenerationResult.Success -> {
        _state.value = _state.value.copy(
            plan = result.plan,
            isLoading = false,
            generationSource = result.source.name // "OPENAI" or "LOCAL"
        )
    }
    is WorkoutGenerationResult.SuccessWithWarning -> {
        _state.value = _state.value.copy(
            plan = result.plan,
            isLoading = false,
            generationSource = result.source.name,
            warningMessage = result.warning // "AI unavailable, using offline mode"
        )
    }
    is WorkoutGenerationResult.Failure -> {
        _state.value = _state.value.copy(
            isLoading = false,
            errorMessage = result.message
        )
    }
}
```

### 3. Error Handling

**Before:**
```kotlin
try {
    val response = apiClient.call()
} catch (e: Exception) {
    showError("Error: ${e.message}") // Technical message
}
```

**After:**
```kotlin
import com.pushprime.data.ai.AiErrorMessages

try {
    val response = apiClient.call()
} catch (e: Exception) {
    val userMessage = AiErrorMessages.formatForUser(e)
    showError(userMessage) // "No internet connection. Please check your network."
}
```

## Example ViewModels

### WorkoutGeneratorPreviewViewModel

**Add AI generation option:**

```kotlin
@HiltViewModel
class WorkoutGeneratorPreviewViewModel @Inject constructor(
    private val localGenerator: WorkoutGenerator,
    private val settingsRepository: AiCoachSettingsRepository,
    private val secureStore: AiCoachSecureStore,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    fun generateWorkout(
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?,
        avoidExercises: Set<String> = emptySet(),
        useAi: Boolean = true // New parameter
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true)

            if (!useAi) {
                // Use local generation
                val inputs = WorkoutGeneratorInputs(goal, timeMinutes, equipment, focus, style)
                val plan = localGenerator.generate(inputs, avoidExercises)
                savePlanAndNavigate(plan)
                return@launch
            }

            // Use enhanced generator with fallback
            val settings = settingsRepository.settings.first()
            val apiKey = secureStore.getOpenAiKey()
            
            val generator = EnhancedWorkoutGenerator(localGenerator)
            val result = generator.generateWithFallback(
                apiKey = apiKey,
                model = settings.modelName,
                baseUrl = settings.baseUrl,
                goal = goal,
                timeMinutes = timeMinutes,
                equipment = equipment,
                focus = focus,
                style = style,
                avoidExercises = avoidExercises
            )

            when (result) {
                is WorkoutGenerationResult.Success -> {
                    savePlanAndNavigate(result.plan)
                }
                is WorkoutGenerationResult.SuccessWithWarning -> {
                    savePlanAndNavigate(result.plan)
                    // Optionally show toast: result.warning
                }
                is WorkoutGenerationResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun savePlanAndNavigate(plan: GeneratedWorkoutPlan) {
        val planId = workoutPlanRepository.createDraft(plan)
        _uiState.value = _uiState.value.copy(
            isGenerating = false,
            generatedPlanId = planId
        )
    }
}
```

### UI State Updates

```kotlin
data class WorkoutGeneratorUiState(
    val isGenerating: Boolean = false,
    val generatedPlanId: Long? = null,
    val errorMessage: String? = null,
    val warningMessage: String? = null, // New
    val generationSource: String? = null // "LOCAL" or "OPENAI"
)
```

### UI Screen Updates

```kotlin
// WorkoutGeneratorSetupScreen.kt

// Show generation source
if (uiState.generationSource == "OPENAI") {
    Text("Generated with AI", style = MaterialTheme.typography.labelSmall)
}

// Show warning if fallback used
uiState.warningMessage?.let { warning ->
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(warning, style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Show error with retry
uiState.errorMessage?.let { error ->
    ErrorCard(
        message = error,
        onRetry = { viewModel.generateWorkout(...) }
    )
}
```

## Testing Migration

### 1. Test Coach Messages (Already Working)
```kotlin
// Test in AiCoachChatScreen
// Send messages, verify responses
// Check error handling (invalid key, no internet)
```

### 2. Test Workout Generation
```kotlin
// Test local generation (always works)
viewModel.generateWorkout(..., useAi = false)

// Test AI generation (with valid key)
viewModel.generateWorkout(..., useAi = true)

// Test AI generation (with invalid key - should fallback)
// Temporarily use wrong key, verify fallback

// Test AI generation (offline - should fallback)
// Disable internet, verify fallback
```

### 3. Test Error Scenarios
```kotlin
// Invalid API key
// Verify user sees: "Invalid API key. Please update your settings."

// No internet
// Verify user sees: "No internet connection."
// Verify fallback to local generation

// Rate limit
// Verify user sees: "Too many requests. Please wait."
```

## Configuration

### Enable AI Features

```kotlin
// AiCoachSetupScreen.kt or Settings
// User enters API key
secureStore.saveOpenAiKey(apiKey)

// User selects model
settingsRepository.updateModelName("gpt-4o-mini")

// User can customize base URL (for proxies)
settingsRepository.updateBaseUrl("https://api.openai.com")
```

### Feature Flags

```kotlin
// Add to LocalStore or Settings
data class FeatureFlags(
    val enableAiWorkoutGeneration: Boolean = false,
    val enableAiCoachChat: Boolean = true
)

// Use in ViewModels
if (featureFlags.enableAiWorkoutGeneration && apiKey != null) {
    // Use AI generation
} else {
    // Use local generation
}
```

## Rollout Strategy

### Phase 1: Coach Messages Only (Current)
- ✅ OpenAiCoachProvider updated
- ✅ AiCoachViewModel using structured outputs
- ✅ No changes to UI needed

### Phase 2: Workout Generation (Optional Feature)
- Add toggle in Settings: "Use AI for workout generation"
- Default: OFF (use local templates)
- When enabled: Use EnhancedWorkoutGenerator with fallback
- Show badge/indicator when plan generated with AI

### Phase 3: Full Rollout
- Make AI generation default (with fallback)
- Remove toggle (always try AI first)
- Keep local templates as fallback

## Rollback Plan

If issues arise:

1. **Disable AI generation:**
```kotlin
// Force local generation
val useAi = false // Was: settings.enableAiGeneration
```

2. **Revert OpenAiCoachProvider:**
```kotlin
// Keep old implementation in separate file
// Switch back if needed
```

3. **Feature flag:**
```kotlin
// Remote config to disable AI features
if (!remoteConfig.enableAiFeatures) {
    return@launch localGenerator.generate(...)
}
```

## Performance Considerations

### API Costs
- gpt-4o-mini: ~$0.15 per 1M input tokens, ~$0.60 per 1M output tokens
- Average workout plan: ~500 tokens input, ~800 tokens output = $0.0006
- Average coach message: ~300 tokens input, ~100 tokens output = $0.0001

### Latency
- AI generation: 2-5 seconds (depends on OpenAI)
- Local generation: <100ms
- With fallback: Worst case = AI timeout + local generation

### Caching (Future Enhancement)
```kotlin
// Cache generated plans
val cacheKey = "$goal-$time-$equipment-$focus-$style"
val cached = cache.get(cacheKey)
if (cached != null) return cached

val plan = generator.generate(...)
cache.put(cacheKey, plan)
```

## Monitoring

### Log Key Metrics
```kotlin
// Track generation source
analytics.logEvent("workout_generated", mapOf(
    "source" to result.source.name,
    "goal" to goal.name,
    "duration" to timeMinutes,
    "success" to (result is Success)
))

// Track errors
analytics.logEvent("workout_generation_error", mapOf(
    "error_type" to error::class.simpleName,
    "http_code" to (error as? OpenAiException)?.httpCode
))
```

### Monitor API Health
```kotlin
// Track API response times
val startTime = System.currentTimeMillis()
val result = generator.generateWorkoutPlan(...)
val duration = System.currentTimeMillis() - startTime

analytics.logEvent("api_latency", mapOf(
    "duration_ms" to duration,
    "success" to result.isSuccess
))
```

## Summary

✅ **Coach messages** - Already migrated, working
🆕 **Workout generation** - New optional feature
📦 **Storage** - No changes needed
🛡️ **Error handling** - Significantly improved
🔄 **Fallback** - Automatic, seamless
🎯 **Type safety** - Guaranteed by schemas

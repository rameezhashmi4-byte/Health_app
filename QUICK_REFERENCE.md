# OpenAI Responses API - Quick Reference

## 🚀 Quick Start

### Generate a Workout Plan
```kotlin
val generator = OpenAiWorkoutPlanGenerator(
    apiKey = "sk-...",
    model = "gpt-4o-mini"
)

val result = generator.generateWorkoutPlan(
    goal = WorkoutGoal.BUILD_MUSCLE,
    timeMinutes = 45,
    equipment = EquipmentOption.DUMBBELLS,
    focus = WorkoutFocus.UPPER,
    style = TrainingStyle.STRAIGHT_SETS
)

result.fold(
    onSuccess = { plan -> /* Use plan */ },
    onFailure = { error -> /* Handle error */ }
)
```

### Generate a Coach Message
```kotlin
val generator = OpenAiCoachMessageGenerator(
    apiKey = "sk-...",
    model = "gpt-4o-mini"
)

val result = generator.generateCoachMessage(
    userMessage = "Should I train today?",
    contextSummary = "Streak: 7 days..."
)

result.fold(
    onSuccess = { msg -> println(msg.message) },
    onFailure = { error -> /* Handle error */ }
)
```

### Use Enhanced Generator with Fallback
```kotlin
val generator = EnhancedWorkoutGenerator()

val result = generator.generateWithFallback(
    apiKey = apiKey, // null = local only
    model = "gpt-4o-mini",
    baseUrl = "https://api.openai.com",
    goal = WorkoutGoal.LOSE_FAT,
    timeMinutes = 30,
    equipment = EquipmentOption.HOME,
    focus = null,
    style = null
)

when (result) {
    is WorkoutGenerationResult.Success -> {
        // ✅ Generated successfully
    }
    is WorkoutGenerationResult.SuccessWithWarning -> {
        // ⚠️ Fallback used, show warning
    }
    is WorkoutGenerationResult.Failure -> {
        // ❌ Both failed
    }
}
```

## 📝 Request Payloads

### Workout Plan Request
```json
{
  "model": "gpt-4o-mini",
  "input": [
    {
      "role": "system",
      "content": [{"type": "text", "text": "Expert fitness coach..."}]
    },
    {
      "role": "user",
      "content": [{"type": "text", "text": "Create 45-min workout..."}]
    }
  ],
  "response_format": {
    "type": "json_schema",
    "json_schema": {
      "name": "workout_plan",
      "strict": true,
      "schema": {
        "type": "object",
        "properties": {
          "title": {"type": "string"},
          "totalDurationMinutes": {"type": "integer"},
          "blocks": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "type": {"type": "string", "enum": ["WARMUP", "MAIN", "FINISHER", "COOLDOWN"]},
                "title": {"type": "string"},
                "durationMinutes": {"type": "integer"},
                "exercises": {"type": "array", "items": {...}}
              },
              "required": ["type", "title", "durationMinutes", "exercises"],
              "additionalProperties": false
            }
          }
        },
        "required": ["title", "totalDurationMinutes", "blocks"],
        "additionalProperties": false
      }
    }
  }
}
```

### Coach Message Request
```json
{
  "model": "gpt-4o-mini",
  "input": [
    {
      "role": "system",
      "content": [{"type": "text", "text": "Concise fitness coach..."}]
    },
    {
      "role": "user",
      "content": [{"type": "text", "text": "Should I train today?"}]
    }
  ],
  "response_format": {
    "type": "json_schema",
    "json_schema": {
      "name": "coach_message",
      "strict": true,
      "schema": {
        "type": "object",
        "properties": {
          "message": {"type": "string"},
          "tone": {"type": "string", "enum": ["motivational", "supportive", "analytical", "encouraging"]},
          "actionable": {"type": "boolean"}
        },
        "required": ["message", "tone", "actionable"],
        "additionalProperties": false
      }
    }
  }
}
```

## 🛡️ Error Handling

### User-Friendly Errors
```kotlin
import com.pushprime.data.ai.AiErrorMessages

try {
    val result = generator.generateWorkoutPlan(...)
} catch (e: Exception) {
    val userMessage = AiErrorMessages.formatForUser(e)
    showError(userMessage)
}
```

### Common Errors
| Code | User Message |
|------|--------------|
| 401  | Invalid API key. Please update your settings. |
| 404  | Service not found. Please verify your base URL. |
| 429  | Too many requests. Please wait a moment. |
| 500  | Service temporarily unavailable. |
| Net  | No internet connection. |

## 📦 Models

### Workout Plan
```kotlin
data class GeneratedWorkoutPlan(
    val id: Long = 0,
    val title: String,
    val totalDurationMinutes: Int,
    val goal: WorkoutGoal,
    val timeMinutes: Int,
    val equipment: EquipmentOption,
    val focus: WorkoutFocus?,
    val style: TrainingStyle?,
    val blocks: List<WorkoutBlock>
)

data class WorkoutBlock(
    val type: WorkoutBlockType, // WARMUP | MAIN | FINISHER | COOLDOWN
    val title: String,
    val durationMinutes: Int?,
    val exercises: List<GeneratedExercise>
)

data class GeneratedExercise(
    val name: String,
    val sets: Int? = null,        // For rep-based
    val reps: Int? = null,        // For rep-based
    val seconds: Int? = null,     // For time-based
    val restSeconds: Int = 0,
    val notes: String? = null,
    val intensityTag: String? = null,  // Light | Moderate | High | Heavy | Max
    val difficultyTag: String? = null  // Easy | Moderate | Hard | Fast
)
```

### Coach Message
```kotlin
data class CoachMessage(
    val message: String,          // 2-4 sentences
    val tone: String,             // motivational | supportive | analytical | encouraging
    val actionable: Boolean       // Has specific advice?
)
```

## 💾 Storage

### Save Workout Plan
```kotlin
// Encode to JSON
val json = WorkoutPlanJson.encode(plan)

// Save to database
val entity = GeneratedWorkoutPlanEntity(
    planJson = json,
    title = plan.title,
    // ... other fields
)
database.insert(entity)
```

### Load Workout Plan
```kotlin
// Load from database
val entity = database.getById(planId)

// Decode from JSON
val plan = WorkoutPlanJson.decode(entity.planJson)
```

## ⚙️ Configuration

### Settings
```kotlin
data class AiCoachSettings(
    val mode: AiCoachMode = AiCoachMode.BASIC,
    val modelName: String = "gpt-4o-mini",
    val baseUrl: String = "https://api.openai.com"
)
```

### Recommended Models
- `gpt-4o-mini` - Fast, cheap, good quality ✅ Recommended
- `gpt-4o` - Slower, more expensive, best quality
- `gpt-3.5-turbo` - Legacy, not recommended

## 🔍 Validation

### Validate Workout Plan
```kotlin
generator.validateWorkoutPlan(plan).fold(
    onSuccess = { /* Valid */ },
    onFailure = { error -> /* Invalid: ${error.message} */ }
)
```

### Validate Coach Message
```kotlin
generator.validateCoachMessage(msg).fold(
    onSuccess = { /* Valid */ },
    onFailure = { error -> /* Invalid: ${error.message} */ }
)
```

## 📂 File Structure
```
app/src/main/java/com/pushprime/data/ai/
├── openai/
│   ├── OpenAiModels.kt              # Request/response models
│   ├── OpenAiSchemas.kt             # JSON schemas
│   ├── OpenAiResponsesService.kt    # HTTP client
│   ├── OpenAiWorkoutPlanGenerator.kt
│   ├── OpenAiCoachMessageGenerator.kt
│   └── OpenAiUsageExamples.kt
├── OpenAiCoachProvider.kt           # Updated provider
├── EnhancedWorkoutGenerator.kt      # Fallback system
└── AiErrorMessages.kt               # Error handling
```

## 🎯 Key Benefits

✅ **No parsing crashes** - Strict schemas enforce structure
✅ **No markdown** - Only JSON responses
✅ **Type-safe** - Kotlin models for everything
✅ **User-friendly errors** - Technical → human translation
✅ **Offline fallback** - Always works
✅ **Zero drift** - Exact replay from storage

## 📚 Documentation

- Full docs: `OPENAI_RESPONSES_API.md`
- Summary: `IMPLEMENTATION_SUMMARY.md`
- Examples: `OpenAiUsageExamples.kt`

# OpenAI Responses API Integration

This document describes the integration of OpenAI's Responses API with strict JSON schemas for workout generation and AI coaching.

## Overview

The implementation replaces legacy AI endpoints with:
- **OpenAI Responses API**: `POST https://api.openai.com/v1/responses`
- **Structured Outputs**: Using `json_schema` with `strict: true`
- **Type-safe parsing**: No markdown, no broken JSON

## Features

### 1. Workout Plan Generation
Generates complete workout plans with strict JSON schema validation.

**Schema Guarantees:**
- Valid JSON structure (no markdown)
- Required fields always present
- Enum validation for intensity/difficulty
- Type validation for all numeric fields

**Models:**
```kotlin
GeneratedWorkoutPlan
  - title: String
  - totalDurationMinutes: Int
  - blocks: List<WorkoutBlock>
    - type: WARMUP | MAIN | FINISHER | COOLDOWN
    - exercises: List<GeneratedExercise>
      - name: String
      - sets/reps OR seconds
      - restSeconds: Int
      - intensityTag: Light | Moderate | High | Heavy | Max
```

### 2. AI Coach Messages
Generates concise, voice-friendly coaching responses.

**Schema Guarantees:**
- Message is always a string (2-4 sentences)
- Tone is validated enum
- Actionable flag indicates if advice is included

**Models:**
```kotlin
CoachMessage
  - message: String
  - tone: motivational | supportive | analytical | encouraging
  - actionable: Boolean
```

## Architecture

### Core Components

```
OpenAiModels.kt           - Request/response models
OpenAiSchemas.kt          - Strict JSON schemas
OpenAiResponsesService.kt - HTTP client wrapper
OpenAiWorkoutPlanGenerator.kt - Workout generation
OpenAiCoachMessageGenerator.kt - Coach messages
EnhancedWorkoutGenerator.kt - Fallback system
AiErrorMessages.kt        - User-friendly errors
```

### Request Flow

```
User Input
    ↓
Generator (OpenAiWorkoutPlanGenerator or OpenAiCoachMessageGenerator)
    ↓
OpenAiResponsesService
    ↓
POST /v1/responses with json_schema
    ↓
Parse JSON response
    ↓
Validate with schema
    ↓
Return typed Kotlin model
```

## Usage

### Generate Workout Plan

```kotlin
val generator = OpenAiWorkoutPlanGenerator(
    apiKey = "sk-...",
    model = "gpt-4o-mini",
    baseUrl = "https://api.openai.com"
)

val result = generator.generateWorkoutPlan(
    goal = WorkoutGoal.BUILD_MUSCLE,
    timeMinutes = 45,
    equipment = EquipmentOption.DUMBBELLS,
    focus = WorkoutFocus.UPPER,
    style = TrainingStyle.STRAIGHT_SETS,
    avoidExercises = setOf("Bench press")
)

result.fold(
    onSuccess = { plan ->
        // Validate
        generator.validateWorkoutPlan(plan).fold(
            onSuccess = { /* Use plan */ },
            onFailure = { error -> /* Handle validation error */ }
        )
    },
    onFailure = { error -> /* Handle API error */ }
)
```

### Generate Coach Message

```kotlin
val generator = OpenAiCoachMessageGenerator(
    apiKey = "sk-...",
    model = "gpt-4o-mini"
)

val result = generator.generateCoachMessage(
    userMessage = "Should I train today?",
    contextSummary = "Streak: 7 days, Last session: yesterday"
)

result.fold(
    onSuccess = { coachMessage ->
        println(coachMessage.message)
    },
    onFailure = { error ->
        println(AiErrorMessages.formatForUser(error))
    }
)
```

### Enhanced Generator with Fallback

```kotlin
val generator = EnhancedWorkoutGenerator()

val result = generator.generateWithFallback(
    apiKey = apiKey, // Can be null
    model = "gpt-4o-mini",
    baseUrl = "https://api.openai.com",
    goal = WorkoutGoal.LOSE_FAT,
    timeMinutes = 30,
    equipment = EquipmentOption.HOME,
    focus = WorkoutFocus.FULL_BODY,
    style = TrainingStyle.CIRCUIT
)

when (result) {
    is WorkoutGenerationResult.Success -> {
        // Plan generated successfully
        val plan = result.plan
        val source = result.source // LOCAL or OPENAI
    }
    is WorkoutGenerationResult.SuccessWithWarning -> {
        // Plan generated with local fallback
        val plan = result.plan
        val warning = result.warning // User-friendly message
    }
    is WorkoutGenerationResult.Failure -> {
        // Both generators failed
        val message = result.message
    }
}
```

## Error Handling

### Error Types

1. **OpenAiException** - API errors (401, 404, 429, etc.)
2. **ValidationException** - Schema validation failures
3. **IOException** - Network errors

### User-Friendly Messages

```kotlin
try {
    val result = generator.generateWorkoutPlan(...)
} catch (e: Exception) {
    val userMessage = AiErrorMessages.formatForUser(e)
    // Display: "Invalid API key. Please check your settings."
}
```

### Common Error Messages

| Error Code | User Message |
|------------|--------------|
| 401 | Invalid API key. Please update your settings. |
| 404 | Service not found. Please verify your base URL. |
| 429 | Too many requests. Please wait a moment. |
| 500/502/503 | Service temporarily unavailable. |
| Network | No internet connection. Please check your network. |

## JSON Schema Details

### Workout Plan Schema

```json
{
  "type": "object",
  "properties": {
    "title": { "type": "string" },
    "totalDurationMinutes": { "type": "integer" },
    "blocks": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "type": { "type": "string", "enum": ["WARMUP", "MAIN", "FINISHER", "COOLDOWN"] },
          "title": { "type": "string" },
          "durationMinutes": { "type": "integer" },
          "exercises": {
            "type": "array",
            "items": {
              "type": "object",
              "properties": {
                "name": { "type": "string" },
                "sets": { "type": "integer" },
                "reps": { "type": "integer" },
                "seconds": { "type": "integer" },
                "restSeconds": { "type": "integer" },
                "notes": { "type": "string" },
                "intensityTag": { "type": "string", "enum": ["Light", "Moderate", "High", "Heavy", "Max"] },
                "difficultyTag": { "type": "string", "enum": ["Easy", "Moderate", "Hard", "Fast"] }
              },
              "required": ["name", "restSeconds"],
              "additionalProperties": false
            }
          }
        },
        "required": ["type", "title", "durationMinutes", "exercises"],
        "additionalProperties": false
      }
    }
  },
  "required": ["title", "totalDurationMinutes", "blocks"],
  "additionalProperties": false
}
```

### Coach Message Schema

```json
{
  "type": "object",
  "properties": {
    "message": { "type": "string" },
    "tone": { "type": "string", "enum": ["motivational", "supportive", "analytical", "encouraging"] },
    "actionable": { "type": "boolean" }
  },
  "required": ["message", "tone", "actionable"],
  "additionalProperties": false
}
```

## Storage

### Workout Plan Storage

Plans are stored as JSON in the database:

```kotlin
// Save to database
val json = WorkoutPlanJson.encode(plan)
database.save(json)

// Retrieve from database
val json = database.load()
val plan = WorkoutPlanJson.decode(json)
```

**Benefits:**
- Zero drift (exact replay of generated plan)
- No parsing errors on retrieval
- Complete workout structure preserved

### Coach Message Storage

Coach messages are transient (not stored), but can be saved if needed:

```kotlin
val message = coachMessage.message
// Display in UI or speak via TTS
```

## Testing

### Validation Tests

```kotlin
// Test workout plan validation
val plan = GeneratedWorkoutPlan(...)
generator.validateWorkoutPlan(plan).fold(
    onSuccess = { /* Valid */ },
    onFailure = { error -> /* Invalid: ${error.message} */ }
)

// Test coach message validation
val coachMessage = CoachMessage(...)
generator.validateCoachMessage(coachMessage).fold(
    onSuccess = { /* Valid */ },
    onFailure = { error -> /* Invalid: ${error.message} */ }
)
```

### Error Handling Tests

```kotlin
// Test API key error
assertThrows<OpenAiException> {
    generator.generateWorkoutPlan(...) // with invalid key
}

// Test fallback
val result = enhancedGenerator.generateWithFallback(
    apiKey = null, // Force local fallback
    ...
)
assertTrue(result is WorkoutGenerationResult.Success)
assertEquals(GenerationSource.LOCAL, result.source)
```

## Acceptance Criteria

✅ **Workout generator always returns valid JSON**
- Strict schema enforcement
- Validation after parsing
- No markdown or broken JSON

✅ **AI coach always returns valid JSON**
- Strict schema enforcement
- Validation after parsing
- Voice-friendly responses (2-4 sentences)

✅ **No random markdown / broken parsing**
- `strict: true` in json_schema
- `additionalProperties: false` in schemas
- Type validation on all fields
- Enum validation for categorical fields

✅ **Parsing never crashes**
- Try-catch around all JSON operations
- Result types for error handling
- User-friendly error messages
- Validation before returning to UI

✅ **Full JSON response stored for replay**
- WorkoutPlanJson.encode/decode
- Database storage of complete JSON
- Zero drift on replay

## Configuration

### API Settings

Configure in `AiCoachSettings`:

```kotlin
data class AiCoachSettings(
    val mode: AiCoachMode = AiCoachMode.BASIC,
    val modelName: String = "gpt-4o-mini",
    val baseUrl: String = "https://api.openai.com"
)
```

### Models

Supported models:
- `gpt-4o-mini` (recommended, fast + cheap)
- `gpt-4o` (more capable, slower)
- `gpt-3.5-turbo` (legacy, not recommended)

## Future Enhancements

1. **Caching**: Cache generated plans to reduce API calls
2. **Offline mode**: Expand local templates
3. **Personalization**: Learn from user preferences
4. **Multi-language**: Support multiple languages
5. **Voice input**: Direct voice commands to AI coach

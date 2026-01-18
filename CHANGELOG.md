# Changelog

## 2026-01-17 - OpenAI Responses API Integration

### 🚀 New Features
- **AI Workout Generator**: Generate personalized workout plans using OpenAI
  - Strict JSON schema validation (no broken parsing)
  - Support for goals: Lose Fat, Build Muscle, Strength, Stamina
  - Equipment options: Home, Dumbbells, Full Gym
  - Focus areas: Full Body, Upper, Lower, Pull-ups, Core
  - Training styles: Circuit, Supersets, AMRAP, EMOM, Straight Sets
  
- **AI Coach Messages**: Context-aware coaching responses
  - Voice-friendly format (2-4 sentences)
  - Personalized based on user stats (streak, sessions, nutrition)
  - Tone validation: motivational, supportive, analytical, encouraging
  
- **Smart Fallback System**: Always works, even offline
  - Tries OpenAI first (if API key available)
  - Falls back to local templates on failure
  - User-friendly warning messages

### 🔧 Technical Improvements
- **OpenAI Responses API**: `POST /v1/responses` with structured outputs
- **Strict JSON Schemas**: Enforced by OpenAI, prevents markdown/broken JSON
- **Type-Safe Models**: Kotlin data classes for all API interactions
- **Validation**: All responses validated before reaching UI
- **Error Handling**: Technical errors converted to user-friendly messages
- **Storage**: Full JSON storage for workout plans (zero drift on replay)

### 📝 Files Added
- `app/src/main/java/com/pushprime/data/ai/openai/`
  - `OpenAiModels.kt` - Request/response models
  - `OpenAiSchemas.kt` - Strict JSON schemas
  - `OpenAiResponsesService.kt` - HTTP client wrapper
  - `OpenAiWorkoutPlanGenerator.kt` - Workout generation
  - `OpenAiCoachMessageGenerator.kt` - Coach messages
  - `OpenAiUsageExamples.kt` - Usage examples
- `app/src/main/java/com/pushprime/data/ai/`
  - `EnhancedWorkoutGenerator.kt` - Fallback system
  - `AiErrorMessages.kt` - Error handling utilities

### 📚 Documentation Added
- `OPENAI_RESPONSES_API.md` - Complete API documentation
- `IMPLEMENTATION_SUMMARY.md` - Implementation details
- `MIGRATION_GUIDE.md` - Migration instructions for existing code
- `QUICK_REFERENCE.md` - Developer quick reference

### 🔄 Files Modified
- `OpenAiCoachProvider.kt` - Updated to use structured outputs
- `README.md` - Added AI features documentation

### ✅ Acceptance Criteria Met
- ✅ Workout generator always returns valid JSON
- ✅ AI coach always returns valid JSON
- ✅ No random markdown / broken parsing
- ✅ Parsing never crashes
- ✅ Full JSON response stored for plan replay

## 2026-01-16
- Rename app display name to RAMBOOST.

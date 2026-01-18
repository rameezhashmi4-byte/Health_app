package com.pushprime.data.ai

import android.util.Log
import com.pushprime.data.WorkoutGenerator
import com.pushprime.data.WorkoutGeneratorInputs
import com.pushprime.data.ai.openai.OpenAiException
import com.pushprime.data.ai.openai.OpenAiWorkoutPlanGenerator
import com.pushprime.model.*
import okhttp3.OkHttpClient

/**
 * Enhanced workout generator that can use either:
 * 1. Local template-based generation (fast, offline)
 * 2. OpenAI-powered generation (personalized, requires API key)
 */
class EnhancedWorkoutGenerator(
    private val localGenerator: WorkoutGenerator = WorkoutGenerator()
) {
    companion object {
        private const val TAG = "EnhancedWorkoutGen"
    }

    /**
     * Generate workout using local templates (always available)
     */
    suspend fun generateLocal(
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?,
        avoidExercises: Set<String> = emptySet()
    ): Result<GeneratedWorkoutPlan> {
        return try {
            val inputs = WorkoutGeneratorInputs(
                goal = goal,
                timeMinutes = timeMinutes,
                equipment = equipment,
                focus = focus,
                style = style
            )
            val plan = localGenerator.generate(inputs, avoidExercises)
            Result.success(plan)
        } catch (e: Exception) {
            Log.e(TAG, "Local generation failed", e)
            Result.failure(Exception("Failed to generate workout: ${e.message}"))
        }
    }

    /**
     * Generate workout using OpenAI (requires API key)
     */
    suspend fun generateWithAi(
        apiKey: String,
        model: String,
        baseUrl: String,
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?,
        avoidExercises: Set<String> = emptySet(),
        client: OkHttpClient = OkHttpClient()
    ): Result<GeneratedWorkoutPlan> {
        val generator = OpenAiWorkoutPlanGenerator(apiKey, model, baseUrl, client)
        
        val result = generator.generateWorkoutPlan(
            goal = goal,
            timeMinutes = timeMinutes,
            equipment = equipment,
            focus = focus,
            style = style,
            avoidExercises = avoidExercises
        )

        return result.fold(
            onSuccess = { plan ->
                // Validate the generated plan
                generator.validateWorkoutPlan(plan).fold(
                    onSuccess = { Result.success(plan) },
                    onFailure = { error ->
                        Log.e(TAG, "Invalid workout plan", error)
                        Result.failure(Exception("Generated plan failed validation: ${error.message}"))
                    }
                )
            },
            onFailure = { error ->
                Log.e(TAG, "AI generation failed", error)
                Result.failure(error)
            }
        )
    }

    /**
     * Generate workout with automatic fallback:
     * Try OpenAI first, fall back to local if it fails
     */
    suspend fun generateWithFallback(
        apiKey: String?,
        model: String,
        baseUrl: String,
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?,
        avoidExercises: Set<String> = emptySet(),
        client: OkHttpClient = OkHttpClient()
    ): WorkoutGenerationResult {
        // If no API key, use local generation
        if (apiKey.isNullOrBlank()) {
            val result = generateLocal(goal, timeMinutes, equipment, focus, style, avoidExercises)
            return result.fold(
                onSuccess = { WorkoutGenerationResult.Success(it, source = GenerationSource.LOCAL) },
                onFailure = { WorkoutGenerationResult.Failure("Local generation failed", null) }
            )
        }

        // Try OpenAI generation
        val aiResult = generateWithAi(
            apiKey, model, baseUrl, goal, timeMinutes, equipment, focus, style, avoidExercises, client
        )

        return aiResult.fold(
            onSuccess = { plan ->
                WorkoutGenerationResult.Success(plan, source = GenerationSource.OPENAI)
            },
            onFailure = { error ->
                Log.w(TAG, "OpenAI generation failed, falling back to local", error)
                
                // Fall back to local generation
                val localResult = generateLocal(goal, timeMinutes, equipment, focus, style, avoidExercises)
                localResult.fold(
                    onSuccess = { plan ->
                        WorkoutGenerationResult.SuccessWithWarning(
                            plan = plan,
                            source = GenerationSource.LOCAL,
                            warning = formatAiErrorForUser(error)
                        )
                    },
                    onFailure = {
                        WorkoutGenerationResult.Failure(
                            message = "Both AI and local generation failed",
                            error = error
                        )
                    }
                )
            }
        )
    }

    private fun formatAiErrorForUser(error: Throwable): String {
        return when (error) {
            is OpenAiException -> when (error.httpCode) {
                401 -> "Invalid API key. Using offline mode."
                404 -> "API endpoint error. Using offline mode."
                429 -> "Rate limit exceeded. Using offline mode."
                else -> "AI service unavailable. Using offline mode."
            }
            else -> "Could not connect to AI. Using offline mode."
        }
    }
}

/**
 * Result of workout generation with metadata
 */
sealed class WorkoutGenerationResult {
    data class Success(
        val plan: GeneratedWorkoutPlan,
        val source: GenerationSource
    ) : WorkoutGenerationResult()

    data class SuccessWithWarning(
        val plan: GeneratedWorkoutPlan,
        val source: GenerationSource,
        val warning: String
    ) : WorkoutGenerationResult()

    data class Failure(
        val message: String,
        val error: Throwable?
    ) : WorkoutGenerationResult()
}

enum class GenerationSource {
    LOCAL,
    OPENAI
}

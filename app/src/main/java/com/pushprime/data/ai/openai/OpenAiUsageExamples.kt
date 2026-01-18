package com.pushprime.data.ai.openai

/**
 * Example usage of OpenAI Responses API with strict JSON schemas
 * 
 * This file demonstrates how to use the new AI-powered features:
 * 1. Generate Workout Plans with strict schema
 * 2. Generate Coach Messages with strict schema
 * 3. Error handling and validation
 */

import com.pushprime.model.*
import okhttp3.OkHttpClient

object OpenAiUsageExamples {
    
    /**
     * Example 1: Generate a workout plan using OpenAI
     */
    suspend fun generateWorkoutPlanExample(apiKey: String) {
        val generator = OpenAiWorkoutPlanGenerator(
            apiKey = apiKey,
            model = "gpt-4o-mini",
            baseUrl = "https://api.openai.com",
            client = OkHttpClient()
        )

        val result = generator.generateWorkoutPlan(
            goal = WorkoutGoal.BUILD_MUSCLE,
            timeMinutes = 45,
            equipment = EquipmentOption.DUMBBELLS,
            focus = WorkoutFocus.UPPER,
            style = TrainingStyle.STRAIGHT_SETS,
            avoidExercises = setOf("Bench press") // Optional: avoid specific exercises
        )

        result.fold(
            onSuccess = { plan ->
                // Validate the plan
                generator.validateWorkoutPlan(plan).fold(
                    onSuccess = {
                        println("Generated plan: ${plan.title}")
                        println("Blocks: ${plan.blocks.size}")
                        plan.blocks.forEach { block ->
                            println("  ${block.title}: ${block.exercises.size} exercises")
                        }
                    },
                    onFailure = { error ->
                        println("Validation failed: ${error.message}")
                    }
                )
            },
            onFailure = { error ->
                println("Generation failed: ${error.message}")
            }
        )
    }

    /**
     * Example 2: Generate a coach message using OpenAI
     */
    suspend fun generateCoachMessageExample(apiKey: String) {
        val generator = OpenAiCoachMessageGenerator(
            apiKey = apiKey,
            model = "gpt-4o-mini",
            baseUrl = "https://api.openai.com",
            client = OkHttpClient()
        )

        val contextSummary = """
            Goal: BUILD_MUSCLE
            Steps today: 8500
            Streak: 7 days
            Last 7 days sessions: 4
            Nutrition today: 2100 kcal, 145 g protein
            Pull-up max: 12 reps
            Pull-up weekly volume: 84
        """.trimIndent()

        val result = generator.generateCoachMessage(
            userMessage = "Should I train today or rest?",
            contextSummary = contextSummary
        )

        result.fold(
            onSuccess = { coachMessage ->
                // Validate the message
                generator.validateCoachMessage(coachMessage).fold(
                    onSuccess = {
                        println("Coach says: ${coachMessage.message}")
                        println("Tone: ${coachMessage.tone}")
                        println("Actionable: ${coachMessage.actionable}")
                    },
                    onFailure = { error ->
                        println("Validation failed: ${error.message}")
                    }
                )
            },
            onFailure = { error ->
                println("Generation failed: ${error.message}")
            }
        )
    }

    /**
     * Example 3: Using the enhanced workout generator with fallback
     */
    suspend fun enhancedWorkoutGeneratorExample(apiKey: String?) {
        val generator = com.pushprime.data.ai.EnhancedWorkoutGenerator()

        val result = generator.generateWithFallback(
            apiKey = apiKey,
            model = "gpt-4o-mini",
            baseUrl = "https://api.openai.com",
            goal = WorkoutGoal.LOSE_FAT,
            timeMinutes = 30,
            equipment = EquipmentOption.HOME,
            focus = WorkoutFocus.FULL_BODY,
            style = TrainingStyle.CIRCUIT
        )

        when (result) {
            is com.pushprime.data.ai.WorkoutGenerationResult.Success -> {
                println("Generated plan: ${result.plan.title}")
                println("Source: ${result.source}")
            }
            is com.pushprime.data.ai.WorkoutGenerationResult.SuccessWithWarning -> {
                println("Generated plan: ${result.plan.title}")
                println("Source: ${result.source}")
                println("Warning: ${result.warning}")
            }
            is com.pushprime.data.ai.WorkoutGenerationResult.Failure -> {
                println("Failed: ${result.message}")
            }
        }
    }

    /**
     * Example 4: Direct API usage with OpenAiResponsesService
     */
    suspend fun directApiUsageExample(apiKey: String) {
        val service = OpenAiResponsesService(
            apiKey = apiKey,
            baseUrl = "https://api.openai.com"
        )

        val messages = listOf(
            Message(
                role = "system",
                content = listOf(ContentBlock(text = "You are a helpful fitness coach."))
            ),
            Message(
                role = "user",
                content = listOf(ContentBlock(text = "Give me a quick tip for muscle recovery."))
            )
        )

        val responseFormat = ResponseFormat.JsonSchema(
            name = "recovery_tip",
            strict = true,
            schema = mapOf(
                "type" to "object",
                "properties" to mapOf(
                    "tip" to mapOf("type" to "string"),
                    "category" to mapOf("type" to "string", "enum" to listOf("sleep", "nutrition", "stretching"))
                ),
                "required" to listOf("tip", "category"),
                "additionalProperties" to false
            )
        )

        val result = service.sendRequest(
            model = "gpt-4o-mini",
            messages = messages,
            responseFormat = responseFormat
        )

        result.fold(
            onSuccess = { response ->
                val json = service.extractJsonContent(response)
                println("Recovery tip: ${json?.optString("tip")}")
                println("Category: ${json?.optString("category")}")
            },
            onFailure = { error ->
                println("Request failed: ${error.message}")
            }
        )
    }
}

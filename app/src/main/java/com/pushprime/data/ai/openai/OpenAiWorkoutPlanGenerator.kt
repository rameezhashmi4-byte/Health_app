package com.pushprime.data.ai.openai

import android.util.Log
import com.pushprime.model.*
import org.json.JSONObject
import okhttp3.OkHttpClient

/**
 * Workout plan generator using OpenAI Responses API with strict JSON schema
 * Ensures valid JSON with no markdown or broken parsing
 */
class OpenAiWorkoutPlanGenerator(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini",
    private val baseUrl: String = "https://api.openai.com",
    private val client: OkHttpClient = OkHttpClient()
) {
    private val service = OpenAiResponsesService(apiKey, baseUrl, client)

    companion object {
        private const val TAG = "OpenAiWorkoutPlanGen"
    }

    /**
     * Generate a workout plan using OpenAI with strict JSON schema
     */
    suspend fun generateWorkoutPlan(
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?,
        avoidExercises: Set<String> = emptySet()
    ): Result<GeneratedWorkoutPlan> {
        try {
            val systemPrompt = buildSystemPrompt()
            val userPrompt = buildUserPrompt(goal, timeMinutes, equipment, focus, style, avoidExercises)

            val messages = listOf(
                Message(
                    role = "system",
                    content = listOf(ContentBlock(text = systemPrompt))
                ),
                Message(
                    role = "user",
                    content = listOf(ContentBlock(text = userPrompt))
                )
            )

            val responseFormat = ResponseFormat.JsonSchema(
                name = "workout_plan",
                strict = true,
                schema = OpenAiSchemas.workoutPlanSchema
            )

            val result = service.sendRequest(model, messages, responseFormat)

            return result.fold(
                onSuccess = { response ->
                    val jsonContent = service.extractJsonContent(response)
                    if (jsonContent == null) {
                        Log.e(TAG, "Failed to extract JSON from response")
                        return Result.failure(Exception("Invalid response format"))
                    }

                    Log.d(TAG, "Received JSON: ${jsonContent.toString(2)}")
                    parseWorkoutPlan(jsonContent, goal, timeMinutes, equipment, focus, style)
                },
                onFailure = { error ->
                    Log.e(TAG, "API request failed", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error", e)
            return Result.failure(e)
        }
    }

    private fun buildSystemPrompt(): String {
        return """
            You are an expert fitness coach creating personalized workout plans.
            Always provide structured, safe, and effective workouts.
            
            Guidelines:
            - Create balanced workouts with proper warm-up and cool-down
            - Use appropriate intensity for the goal
            - Ensure exercises match the available equipment
            - Include rest periods for safety and recovery
            - Keep time-based exercises realistic (30-240 seconds typical)
            - Keep rep-based exercises in standard ranges (5-25 reps typical)
        """.trimIndent()
    }

    private fun buildUserPrompt(
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?,
        avoidExercises: Set<String>
    ): String {
        val focusText = focus?.let { " with ${it.displayName} focus" } ?: ""
        val styleText = style?.let { " using ${it.displayName} style" } ?: ""
        val avoidText = if (avoidExercises.isNotEmpty()) {
            "\n\nAvoid these exercises: ${avoidExercises.joinToString(", ")}"
        } else ""

        return """
            Create a $timeMinutes-minute workout plan for:
            Goal: ${goal.displayName}$focusText
            Equipment: ${equipment.displayName}$styleText$avoidText
            
            Structure:
            - WARMUP: 5-10% of total time
            - MAIN: 60-75% of total time (primary work)
            - FINISHER: 10-15% of total time (optional for longer workouts)
            - COOLDOWN: 5-10% of total time (optional for longer workouts)
            
            For each exercise provide:
            - name: Clear exercise name
            - sets/reps OR seconds (time-based)
            - restSeconds: Appropriate rest
            - notes: Brief coaching cue
            - intensityTag: Light/Moderate/High/Heavy/Max
            - difficultyTag: Easy/Moderate/Hard/Fast
        """.trimIndent()
    }

    private fun parseWorkoutPlan(
        json: JSONObject,
        goal: WorkoutGoal,
        timeMinutes: Int,
        equipment: EquipmentOption,
        focus: WorkoutFocus?,
        style: TrainingStyle?
    ): Result<GeneratedWorkoutPlan> {
        return try {
            val title = json.getString("title")
            val totalDurationMinutes = json.getInt("totalDurationMinutes")
            val blocksArray = json.getJSONArray("blocks")
            
            val blocks = mutableListOf<WorkoutBlock>()
            
            for (i in 0 until blocksArray.length()) {
                val blockJson = blocksArray.getJSONObject(i)
                val blockType = WorkoutBlockType.valueOf(blockJson.getString("type"))
                val blockTitle = blockJson.getString("title")
                val durationMinutes = blockJson.getInt("durationMinutes")
                val exercisesArray = blockJson.getJSONArray("exercises")
                
                val exercises = mutableListOf<GeneratedExercise>()
                
                for (j in 0 until exercisesArray.length()) {
                    val exJson = exercisesArray.getJSONObject(j)
                    exercises.add(
                        GeneratedExercise(
                            name = exJson.getString("name"),
                            sets = exJson.optInt("sets").takeIf { it > 0 },
                            reps = exJson.optInt("reps").takeIf { it > 0 },
                            seconds = exJson.optInt("seconds").takeIf { it > 0 },
                            restSeconds = exJson.getInt("restSeconds"),
                            notes = exJson.optString("notes", null),
                            intensityTag = exJson.optString("intensityTag", null),
                            difficultyTag = exJson.optString("difficultyTag", null)
                        )
                    )
                }
                
                blocks.add(
                    WorkoutBlock(
                        type = blockType,
                        title = blockTitle,
                        durationMinutes = durationMinutes,
                        exercises = exercises
                    )
                )
            }

            val plan = GeneratedWorkoutPlan(
                title = title,
                totalDurationMinutes = totalDurationMinutes,
                goal = goal,
                timeMinutes = timeMinutes,
                equipment = equipment,
                focus = focus,
                style = style,
                blocks = blocks
            )

            Result.success(plan)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse workout plan", e)
            Result.failure(Exception("Failed to parse workout plan: ${e.message}"))
        }
    }

    /**
     * Validate that a workout plan is complete and safe
     */
    fun validateWorkoutPlan(plan: GeneratedWorkoutPlan): Result<Unit> {
        if (plan.blocks.isEmpty()) {
            return Result.failure(Exception("Workout plan has no blocks"))
        }

        if (plan.blocks.none { it.type == WorkoutBlockType.MAIN }) {
            return Result.failure(Exception("Workout plan missing main workout block"))
        }

        plan.blocks.forEach { block ->
            if (block.exercises.isEmpty()) {
                return Result.failure(Exception("Block '${block.title}' has no exercises"))
            }

            block.exercises.forEach { exercise ->
                if (exercise.name.isBlank()) {
                    return Result.failure(Exception("Exercise has no name"))
                }

                val hasRepBased = exercise.sets != null && exercise.reps != null
                val hasTimeBased = exercise.seconds != null

                if (!hasRepBased && !hasTimeBased) {
                    return Result.failure(
                        Exception("Exercise '${exercise.name}' missing sets/reps or seconds")
                    )
                }
            }
        }

        return Result.success(Unit)
    }
}

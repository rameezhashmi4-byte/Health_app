package com.pushprime.data.ai.openai

/**
 * Strict JSON schemas for OpenAI Responses API
 */
object OpenAiSchemas {
    
    /**
     * Strict schema for workout plan generation
     * Ensures valid JSON with no markdown or broken parsing
     */
    val workoutPlanSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "title" to mapOf(
                "type" to "string",
                "description" to "Title of the workout plan"
            ),
            "totalDurationMinutes" to mapOf(
                "type" to "integer",
                "description" to "Total duration in minutes"
            ),
            "blocks" to mapOf(
                "type" to "array",
                "description" to "Workout blocks (warmup, main, finisher, cooldown)",
                "items" to mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "type" to mapOf(
                            "type" to "string",
                            "enum" to listOf("WARMUP", "MAIN", "FINISHER", "COOLDOWN")
                        ),
                        "title" to mapOf(
                            "type" to "string"
                        ),
                        "durationMinutes" to mapOf(
                            "type" to "integer"
                        ),
                        "exercises" to mapOf(
                            "type" to "array",
                            "items" to mapOf(
                                "type" to "object",
                                "properties" to mapOf(
                                    "name" to mapOf(
                                        "type" to "string",
                                        "description" to "Exercise name"
                                    ),
                                    "sets" to mapOf(
                                        "type" to "integer",
                                        "description" to "Number of sets (null for time-based)"
                                    ),
                                    "reps" to mapOf(
                                        "type" to "integer",
                                        "description" to "Number of reps (null for time-based)"
                                    ),
                                    "seconds" to mapOf(
                                        "type" to "integer",
                                        "description" to "Duration in seconds (null for rep-based)"
                                    ),
                                    "restSeconds" to mapOf(
                                        "type" to "integer",
                                        "description" to "Rest in seconds"
                                    ),
                                    "notes" to mapOf(
                                        "type" to "string",
                                        "description" to "Exercise notes"
                                    ),
                                    "intensityTag" to mapOf(
                                        "type" to "string",
                                        "enum" to listOf("Light", "Moderate", "High", "Heavy", "Max")
                                    ),
                                    "difficultyTag" to mapOf(
                                        "type" to "string",
                                        "enum" to listOf("Easy", "Moderate", "Hard", "Fast")
                                    )
                                ),
                                "required" to listOf("name", "restSeconds"),
                                "additionalProperties" to false
                            )
                        )
                    ),
                    "required" to listOf("type", "title", "durationMinutes", "exercises"),
                    "additionalProperties" to false
                )
            )
        ),
        "required" to listOf("title", "totalDurationMinutes", "blocks"),
        "additionalProperties" to false
    )

    /**
     * Strict schema for AI coach messages
     * Ensures voice-friendly coaching responses
     */
    val coachMessageSchema = mapOf(
        "type" to "object",
        "properties" to mapOf(
            "message" to mapOf(
                "type" to "string",
                "description" to "Concise, motivational coaching message (2-4 sentences max)"
            ),
            "tone" to mapOf(
                "type" to "string",
                "enum" to listOf("motivational", "supportive", "analytical", "encouraging"),
                "description" to "Tone of the message"
            ),
            "actionable" to mapOf(
                "type" to "boolean",
                "description" to "Whether the message includes actionable advice"
            )
        ),
        "required" to listOf("message", "tone", "actionable"),
        "additionalProperties" to false
    )
}

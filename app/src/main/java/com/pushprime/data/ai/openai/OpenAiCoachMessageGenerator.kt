package com.pushprime.data.ai.openai

import android.util.Log
import okhttp3.OkHttpClient
import org.json.JSONObject

/**
 * Data class for structured coach message
 */
data class CoachMessage(
    val message: String,
    val tone: String,
    val actionable: Boolean
)

/**
 * AI Coach message generator using OpenAI Responses API with strict JSON schema
 * Ensures valid JSON with voice-friendly coaching responses
 */
class OpenAiCoachMessageGenerator(
    private val apiKey: String,
    private val model: String = "gpt-4o-mini",
    private val baseUrl: String = "https://api.openai.com",
    private val client: OkHttpClient = OkHttpClient()
) {
    private val service = OpenAiResponsesService(apiKey, baseUrl, client)

    companion object {
        private const val TAG = "OpenAiCoachMessageGen"
    }

    /**
     * Generate a coach message using OpenAI with strict JSON schema
     */
    suspend fun generateCoachMessage(
        userMessage: String,
        contextSummary: String
    ): Result<CoachMessage> {
        try {
            val systemPrompt = buildSystemPrompt(contextSummary)

            val messages = listOf(
                Message(
                    role = "system",
                    content = listOf(ContentBlock(text = systemPrompt))
                ),
                Message(
                    role = "user",
                    content = listOf(ContentBlock(text = userMessage))
                )
            )

            val responseFormat = ResponseFormat.JsonSchema(
                name = "coach_message",
                strict = true,
                schema = OpenAiSchemas.coachMessageSchema
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
                    parseCoachMessage(jsonContent)
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

    private fun buildSystemPrompt(contextSummary: String): String {
        return """
            You are a concise, motivational fitness coach for RAMBOOST.
            
            Guidelines:
            - Keep responses SHORT: 2-4 sentences maximum
            - Be voice-friendly (easy to read aloud)
            - Use natural, conversational language
            - Personalize based on user context
            - Give specific, actionable advice when possible
            - Be encouraging but realistic
            - Avoid jargon and complex terms
            
            User Context:
            $contextSummary
        """.trimIndent()
    }

    private fun parseCoachMessage(json: JSONObject): Result<CoachMessage> {
        return try {
            val message = json.getString("message")
            val tone = json.getString("tone")
            val actionable = json.getBoolean("actionable")

            val coachMessage = CoachMessage(
                message = message,
                tone = tone,
                actionable = actionable
            )

            Result.success(coachMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse coach message", e)
            Result.failure(Exception("Failed to parse coach message: ${e.message}"))
        }
    }

    /**
     * Validate that a coach message is appropriate
     */
    fun validateCoachMessage(coachMessage: CoachMessage): Result<Unit> {
        if (coachMessage.message.isBlank()) {
            return Result.failure(Exception("Coach message is empty"))
        }

        if (coachMessage.message.length > 500) {
            return Result.failure(Exception("Coach message is too long (should be concise)"))
        }

        val validTones = setOf("motivational", "supportive", "analytical", "encouraging")
        if (coachMessage.tone !in validTones) {
            return Result.failure(Exception("Invalid tone: ${coachMessage.tone}"))
        }

        return Result.success(Unit)
    }
}

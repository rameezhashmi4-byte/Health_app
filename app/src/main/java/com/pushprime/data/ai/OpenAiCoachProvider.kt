package com.pushprime.data.ai

import android.util.Log
import com.pushprime.data.ai.openai.OpenAiCoachMessageGenerator
import com.pushprime.data.ai.openai.OpenAiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * OpenAI Coach Provider using Responses API with strict JSON schema
 * Ensures valid JSON with no markdown or broken parsing
 */
class OpenAiCoachProvider(
    private val apiKey: String,
    private val modelName: String,
    private val baseUrl: String = "https://api.openai.com",
    private val client: OkHttpClient = OkHttpClient()
) : AiCoachProvider {

    private val generator = OpenAiCoachMessageGenerator(apiKey, modelName, baseUrl, client)

    companion object {
        private const val TAG = "OpenAiCoachProvider"
    }

    override suspend fun sendMessage(userMessage: String, contextSummary: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val result = generator.generateCoachMessage(userMessage, contextSummary)
                
                result.fold(
                    onSuccess = { coachMessage ->
                        // Validate the message
                        generator.validateCoachMessage(coachMessage).fold(
                            onSuccess = { coachMessage.message },
                            onFailure = { error ->
                                Log.e(TAG, "Invalid coach message", error)
                                "Sorry, I couldn't generate a proper response. Please try again."
                            }
                        )
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to generate message", error)
                        formatUserFriendlyError(error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                "Connection error. Please check your internet and try again."
            }
        }
    }

    private fun formatUserFriendlyError(error: Throwable): String {
        return when (error) {
            is OpenAiException -> when (error.httpCode) {
                401 -> "Invalid API key. Please check your OpenAI settings."
                404 -> "API endpoint not found. Please check your base URL in settings."
                429 -> "Rate limit exceeded. Please wait a moment and try again."
                else -> "AI service temporarily unavailable. Please try again."
            }
            else -> "Could not connect to AI coach. Please check your internet connection."
        }
    }
}

package com.pushprime.data.ai.openai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * OpenAI Responses API service
 * POST https://api.openai.com/v1/responses
 */
class OpenAiResponsesService(
    private val apiKey: String,
    private val baseUrl: String = "https://api.openai.com",
    private val client: OkHttpClient = OkHttpClient()
) {
    companion object {
        private const val TAG = "OpenAiResponsesService"
    }

    /**
     * Send a request to OpenAI Responses API with structured output
     */
    suspend fun sendRequest(
        model: String,
        messages: List<Message>,
        responseFormat: ResponseFormat? = null
    ): Result<OpenAiResponseResult> {
        return withContext(Dispatchers.IO) {
            try {
                val payload = OpenAiJsonBuilder.buildRequest(model, messages, responseFormat)
                val url = buildUrl("/v1/responses")
                
                Log.d(TAG, "Request URL: $url")
                Log.d(TAG, "Request payload: ${payload.toString(2)}")

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(payload.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    
                    if (!response.isSuccessful) {
                        val errorMsg = parseErrorMessage(response.code, body, url)
                        Log.e(TAG, "API error: $errorMsg")
                        return@withContext Result.failure(OpenAiException(errorMsg, response.code))
                    }

                    Log.d(TAG, "Response: $body")
                    
                    val result = OpenAiJsonBuilder.parseResponse(body)
                    Result.success(result)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Network error", e)
                Result.failure(OpenAiException("Network error: ${e.localizedMessage}", -1))
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error", e)
                Result.failure(OpenAiException("Failed to parse response: ${e.localizedMessage}", -1))
            }
        }
    }

    /**
     * Extract text content from response
     */
    fun extractTextContent(result: OpenAiResponseResult): String? {
        return result.output
            .firstOrNull()
            ?.content
            ?.firstOrNull { it.type == "text" }
            ?.text
    }

    /**
     * Extract JSON object from response
     */
    fun extractJsonContent(result: OpenAiResponseResult): JSONObject? {
        val text = extractTextContent(result) ?: return null
        return try {
            JSONObject(text)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse JSON from response", e)
            null
        }
    }

    private fun buildUrl(path: String): String {
        val trimmedBase = baseUrl.trim().removeSuffix("/")
        val trimmedPath = if (path.startsWith("/")) path else "/$path"
        return "$trimmedBase$trimmedPath".replace("/v1/v1", "/v1")
    }

    private fun parseErrorMessage(code: Int, body: String, url: String): String {
        return when (code) {
            400 -> "Invalid request: ${extractErrorFromBody(body)}"
            401 -> "Invalid or expired API key"
            403 -> "Access forbidden"
            404 -> "Endpoint not found. Check base URL: $url"
            429 -> "Rate limit exceeded. Please try again later"
            500, 502, 503 -> "OpenAI service temporarily unavailable"
            else -> "API request failed (HTTP $code): ${extractErrorFromBody(body)}"
        }
    }

    private fun extractErrorFromBody(body: String): String {
        return try {
            val json = JSONObject(body)
            json.optJSONObject("error")?.optString("message") ?: body.take(100)
        } catch (e: Exception) {
            body.take(100)
        }
    }
}

/**
 * Custom exception for OpenAI API errors
 */
class OpenAiException(
    message: String,
    val httpCode: Int
) : Exception(message)

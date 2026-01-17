package com.pushprime.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class OpenAiCoachProvider(
    private val apiKey: String,
    private val modelName: String,
    private val client: OkHttpClient = OkHttpClient()
) : AiCoachProvider {
    override suspend fun sendMessage(userMessage: String, contextSummary: String): String {
        return withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("model", modelName)
                put(
                    "input",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("role", "system")
                                put(
                                    "content",
                                    JSONArray().apply {
                                        put(
                                            JSONObject().apply {
                                                put("type", "text")
                                                put(
                                                    "text",
                                                    "You are a concise fitness coach for RAMBOOST. " +
                                                        "Use the context summary to personalize. " +
                                                        "Context: $contextSummary"
                                                )
                                            }
                                        )
                                    }
                                )
                            }
                        )
                        put(
                            JSONObject().apply {
                                put("role", "user")
                                put(
                                    "content",
                                    JSONArray().apply {
                                        put(
                                            JSONObject().apply {
                                                put("type", "text")
                                                put("text", userMessage)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }

            val request = Request.Builder()
                .url("https://api.openai.com/v1/responses")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext "AI request failed (${response.code})."
                }
                parseResponseText(body) ?: "AI response was empty."
            }
        }
    }

    private fun parseResponseText(raw: String): String? {
        return try {
            val json = JSONObject(raw)
            val output = json.optJSONArray("output") ?: return null
            if (output.length() == 0) return null
            val content = output.getJSONObject(0).optJSONArray("content") ?: return null
            if (content.length() == 0) return null
            content.getJSONObject(0).optString("text", null)
        } catch (_: Exception) {
            null
        }
    }
}

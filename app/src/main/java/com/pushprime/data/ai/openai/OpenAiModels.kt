package com.pushprime.data.ai.openai

import org.json.JSONArray
import org.json.JSONObject

/**
 * OpenAI Responses API request/response models
 * https://api.openai.com/v1/responses
 */

// Request models
data class OpenAiResponseRequest(
    val model: String,
    val input: List<Message>,
    val responseFormat: ResponseFormat? = null
)

data class Message(
    val role: String, // "system", "user", "assistant"
    val content: List<ContentBlock>
)

data class ContentBlock(
    val type: String = "text",
    val text: String
)

sealed class ResponseFormat {
    data class JsonSchema(
        val name: String,
        val strict: Boolean = true,
        val schema: Map<String, Any>
    ) : ResponseFormat()
}

// Response models
data class OpenAiResponseResult(
    val output: List<OutputMessage>
)

data class OutputMessage(
    val role: String,
    val content: List<OutputContent>
)

data class OutputContent(
    val type: String,
    val text: String? = null
)

// JSON builders
object OpenAiJsonBuilder {
    fun buildRequest(
        model: String,
        messages: List<Message>,
        responseFormat: ResponseFormat? = null
    ): JSONObject {
        return JSONObject().apply {
            put("model", model)
            put("input", JSONArray().apply {
                messages.forEach { message ->
                    put(JSONObject().apply {
                        put("role", message.role)
                        put("content", JSONArray().apply {
                            message.content.forEach { block ->
                                put(JSONObject().apply {
                                    put("type", block.type)
                                    put("text", block.text)
                                })
                            }
                        })
                    })
                }
            })

            if (responseFormat is ResponseFormat.JsonSchema) {
                put("response_format", JSONObject().apply {
                    put("type", "json_schema")
                    put("json_schema", JSONObject().apply {
                        put("name", responseFormat.name)
                        put("strict", responseFormat.strict)
                        put("schema", buildJsonSchema(responseFormat.schema))
                    })
                })
            }
        }
    }

    private fun buildJsonSchema(schema: Map<String, Any>): JSONObject {
        return JSONObject().apply {
            schema.forEach { (key, value) ->
                when (value) {
                    is Map<*, *> -> put(key, buildJsonSchema(value as Map<String, Any>))
                    is List<*> -> put(key, JSONArray(value))
                    else -> put(key, value)
                }
            }
        }
    }

    fun parseResponse(rawJson: String): OpenAiResponseResult {
        val json = JSONObject(rawJson)
        val outputArray = json.getJSONArray("output")
        val messages = mutableListOf<OutputMessage>()

        for (i in 0 until outputArray.length()) {
            val msgObj = outputArray.getJSONObject(i)
            val role = msgObj.getString("role")
            val contentArray = msgObj.getJSONArray("content")
            val contentBlocks = mutableListOf<OutputContent>()

            for (j in 0 until contentArray.length()) {
                val contentObj = contentArray.getJSONObject(j)
                contentBlocks.add(
                    OutputContent(
                        type = contentObj.getString("type"),
                        text = contentObj.optString("text", null)
                    )
                )
            }

            messages.add(OutputMessage(role = role, content = contentBlocks))
        }

        return OpenAiResponseResult(output = messages)
    }
}

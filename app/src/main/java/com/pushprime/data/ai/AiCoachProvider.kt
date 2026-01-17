package com.pushprime.data.ai

interface AiCoachProvider {
    suspend fun sendMessage(userMessage: String, contextSummary: String): String
}

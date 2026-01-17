package com.pushprime.coach

interface AiCoachProvider {
    suspend fun sendMessage(userText: String, contextSummary: String): String

    suspend fun generateOneLiner(contextSummary: String, style: CoachStyle): String
}

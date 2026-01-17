package com.pushprime.coach

class OpenAiCoachProvider(
    private val apiKey: String?,
    private val fallback: AiCoachProvider = BasicCoachProvider()
) : AiCoachProvider {
    override suspend fun sendMessage(userText: String, contextSummary: String): String {
        if (apiKey.isNullOrBlank()) {
            return fallback.sendMessage(userText, contextSummary)
        }
        return fallback.sendMessage(userText, contextSummary)
    }

    override suspend fun generateOneLiner(contextSummary: String, style: CoachStyle): String {
        if (apiKey.isNullOrBlank()) {
            return fallback.generateOneLiner(contextSummary, style)
        }
        return fallback.generateOneLiner(contextSummary, style)
    }
}

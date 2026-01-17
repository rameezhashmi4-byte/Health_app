package com.pushprime.data.ai

class BasicCoachProvider : AiCoachProvider {
    override suspend fun sendMessage(userMessage: String, contextSummary: String): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("pull") -> {
                "Pull-up focus today: keep 2-3 reps in reserve, stop one set short of failure, " +
                    "and add a slow 3-second negative. " +
                    "Context: $contextSummary"
            }
            lower.contains("protein") || lower.contains("eat") || lower.contains("nutrition") -> {
                "Nutrition tip: aim for lean protein at each meal (25-35g), " +
                    "and split carbs around training. " +
                    "Context: $contextSummary"
            }
            lower.contains("workout") || lower.contains("session") -> {
                "Workout idea: 5-min warmup, 20-min strength circuit, 5-min finisher. " +
                    "Keep intensity moderate if your streak is high. " +
                    "Context: $contextSummary"
            }
            else -> {
                "Quick plan: pick one priority (strength, nutrition, or steps), " +
                    "do a 20-30 min focused session, then log your meal. " +
                    "Context: $contextSummary"
            }
        }
    }
}

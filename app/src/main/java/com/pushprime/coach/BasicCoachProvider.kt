package com.pushprime.coach

import kotlin.random.Random

class BasicCoachProvider : AiCoachProvider {
    private val calmLines = listOf(
        "Breathe steady. Stay smooth.",
        "Easy pace. Keep control.",
        "Reset your breath. Move with flow.",
        "You are steady. Keep it clean.",
        "Smooth reps. Quiet focus."
    )

    private val friendlyLines = listOf(
        "You got this. Stay sharp.",
        "Nice work. Keep the pace.",
        "Strong effort. Keep moving.",
        "Great form. Keep it tight.",
        "Stay with it. You are close."
    )

    private val hypeLines = listOf(
        "Drive hard. No brakes.",
        "Push now. Own this round.",
        "All gas. Finish strong.",
        "Big energy. Big finish.",
        "Explode. Finish the set."
    )

    private val militaryLines = listOf(
        "Lock in. Execute.",
        "No drift. Stay on mission.",
        "Form tight. Move now.",
        "Push through. Stay ready.",
        "Hold focus. Keep pressure."
    )

    private val chatReplies = listOf(
        "Locked. Give me your plan and I will tune it.",
        "I am with you. Tell me the goal.",
        "Say the target. We build the path.",
        "Got it. Keep it simple and we progress.",
        "Let us focus on the next win."
    )

    override suspend fun sendMessage(userText: String, contextSummary: String): String {
        val trimmed = userText.trim()
        if (trimmed.isBlank()) {
            return "Say the goal and I will guide you."
        }
        val prompt = trimmed.lowercase()
        return when {
            "plan" in prompt -> "Short plan: warm up, main work, cool down. Repeat."
            "motivation" in prompt -> "You are building momentum. Stay locked."
            "tired" in prompt -> "Short reset. Then one strong set."
            "rest" in prompt -> "Quick rest. Then clean reps."
            else -> chatReplies.random()
        }
    }

    override suspend fun generateOneLiner(contextSummary: String, style: CoachStyle): String {
        val lines = when (style) {
            CoachStyle.CALM -> calmLines
            CoachStyle.FRIENDLY -> friendlyLines
            CoachStyle.HYPE -> hypeLines
            CoachStyle.MILITARY -> militaryLines
        }
        return lines[Random.nextInt(lines.size)]
    }
}

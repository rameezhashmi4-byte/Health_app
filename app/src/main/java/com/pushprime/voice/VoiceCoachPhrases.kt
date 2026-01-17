package com.pushprime.voice

object VoiceCoachPhrases {
    fun sessionStart(): String = "Let\u2019s go \u2014 RAMBOOST time \uD83D\uDCAA"

    fun halfway(): String = "You\u2019re halfway. Don\u2019t slow down."

    fun finalMinute(): String = "Final push. Empty the tank!"

    fun restStart(): String = "Breathe. Reset. Next round."

    fun restEnd(): String = "Go. Go. Go!"

    fun completion(): String = "Session complete. You did the work \uD83D\uDD25"

    fun sample(personality: CoachPersonality): String {
        return when (personality) {
            CoachPersonality.CALM -> "Steady breath. You\u2019re in control."
            CoachPersonality.HYPE -> "Let\u2019s go! RAMBOOST energy \uD83D\uDCAA"
            CoachPersonality.MILITARY -> "Move with purpose. Own the rep."
            CoachPersonality.FRIENDLY -> "You\u2019ve got this. Keep it smooth."
        }
    }

    fun motivationSet(personality: CoachPersonality): List<String> {
        return when (personality) {
            CoachPersonality.CALM -> listOf(
                "Smooth and steady. Keep the pace.",
                "You\u2019re strong. Breathe and move.",
                "Control the tempo. Stay relaxed."
            )
            CoachPersonality.HYPE -> listOf(
                "Push it. RAMBOOST power!",
                "Big energy. Keep firing.",
                "You\u2019re on fire. Keep it up!"
            )
            CoachPersonality.MILITARY -> listOf(
                "Discipline. Stay sharp.",
                "No excuses. Keep moving.",
                "Hold the line. Keep pushing."
            )
            CoachPersonality.FRIENDLY -> listOf(
                "Great work. Keep going.",
                "You\u2019re doing awesome. Stay with it.",
                "Strong effort. Keep the rhythm."
            )
        }
    }
}

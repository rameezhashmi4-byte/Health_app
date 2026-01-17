package com.pushprime.coach

enum class CoachIntelligence {
    BASIC,
    AI_TEXT,
    AI_VOICE
}

enum class VoiceProviderType {
    SYSTEM,
    OPENAI
}

enum class CoachStyle {
    CALM,
    FRIENDLY,
    HYPE,
    MILITARY
}

enum class CoachFrequency(val seconds: Int) {
    LOW(120),
    MEDIUM(60),
    HIGH(30)
}

enum class SessionType {
    QUICK,
    WORKOUT,
    SPORT
}

enum class SessionPhase {
    WARMUP,
    MAIN,
    FINISHER,
    REST
}

data class CoachSettings(
    val hybridEnabled: Boolean = false,
    val intelligence: CoachIntelligence = CoachIntelligence.BASIC,
    val voiceProvider: VoiceProviderType = VoiceProviderType.SYSTEM,
    val style: CoachStyle = CoachStyle.FRIENDLY,
    val frequency: CoachFrequency = CoachFrequency.MEDIUM,
    val speakReplies: Boolean = false
)

data class SessionState(
    val sessionType: SessionType,
    val phase: SessionPhase,
    val secondsElapsed: Int,
    val secondsRemaining: Int,
    val roundNumber: Int,
    val isPaused: Boolean
)

data class UserContext(
    val goal: String? = null,
    val streakDays: Int? = null,
    val stepsToday: Long? = null,
    val lastWorkoutDate: String? = null
)

data class RecentPerformance(
    val heartRate: Int? = null,
    val reps: Int? = null,
    val effort: String? = null
)

data class CoachLineRequest(
    val contextSummary: String,
    val style: CoachStyle,
    val useAi: Boolean,
    val aiProvider: AiCoachProvider,
    val fallbackProvider: AiCoachProvider
)

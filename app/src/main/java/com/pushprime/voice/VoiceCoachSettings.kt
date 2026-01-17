package com.pushprime.voice

enum class CoachPersonality {
    CALM,
    HYPE,
    MILITARY,
    FRIENDLY
}

enum class CoachFrequency(val intervalSeconds: Int) {
    LOW(120),
    MEDIUM(60),
    HIGH(30)
}

enum class CoachIntensity {
    LOW,
    MEDIUM,
    HIGH
}

enum class VoiceType {
    SYSTEM_DEFAULT,
    MALE,
    FEMALE
}

enum class VoiceProviderType {
    SYSTEM,
    AI_OPENAI
}

data class VoiceCoachSettings(
    val enabled: Boolean = true,
    val personality: CoachPersonality = CoachPersonality.FRIENDLY,
    val frequency: CoachFrequency = CoachFrequency.MEDIUM,
    val voiceType: VoiceType = VoiceType.SYSTEM_DEFAULT,
    val speakDuringRestOnly: Boolean = false,
    val provider: VoiceProviderType = VoiceProviderType.SYSTEM
)

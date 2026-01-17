package com.pushprime.voice

interface VoiceProvider {
    val isAvailable: Boolean
    fun speak(text: String, queueIfBusy: Boolean = false)
    fun stop()
    fun setStyle(personality: CoachPersonality)
    fun setIntensity(level: CoachIntensity)
}

interface VoiceProviderLifecycle {
    fun shutdown()
}

interface VoiceTypeSupport {
    fun setVoiceType(voiceType: VoiceType)
}

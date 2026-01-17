package com.pushprime.voice

class OpenAiTtsVoiceProvider(
    private val apiKey: String
) : VoiceProvider {
    override val isAvailable: Boolean
        get() = false

    override fun speak(text: String, queueIfBusy: Boolean) {
        // Stub: OpenAI audio/TTS integration will be implemented later.
    }

    override fun stop() {
        // Stub.
    }

    override fun setStyle(personality: CoachPersonality) {
        // Stub.
    }

    override fun setIntensity(level: CoachIntensity) {
        // Stub.
    }
}

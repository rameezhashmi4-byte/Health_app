package com.pushprime.coach

class OpenAiVoiceProvider(
    private val fallback: VoiceProvider
) : VoiceProvider {
    override fun speak(text: String) {
        fallback.speak(text)
    }

    override fun stop() {
        fallback.stop()
    }

    override fun setStyle(style: CoachStyle) {
        fallback.setStyle(style)
    }

    override fun setMuted(isMuted: Boolean) {
        fallback.setMuted(isMuted)
    }

    override fun setEnabled(isEnabled: Boolean) {
        fallback.setEnabled(isEnabled)
    }
}

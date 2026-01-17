package com.pushprime.coach

import com.pushprime.voice.CoachPersonality
import com.pushprime.voice.VoiceProvider

class VoiceProviderAdapter(
    private var provider: VoiceProvider
) : com.pushprime.coach.VoiceProvider {
    private var isMuted = false
    private var isEnabled = true

    override fun speak(text: String) {
        if (!isEnabled || isMuted || !provider.isAvailable) return
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        provider.speak(cleanText, queueIfBusy = false)
    }

    override fun stop() {
        provider.stop()
    }

    override fun setStyle(style: CoachStyle) {
        provider.setStyle(style.toPersonality())
    }

    override fun setMuted(isMuted: Boolean) {
        this.isMuted = isMuted
        if (isMuted) {
            provider.stop()
        }
    }

    override fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        if (!isEnabled) {
            provider.stop()
        }
    }

    fun updateProvider(newProvider: VoiceProvider) {
        provider = newProvider
    }
}

private fun CoachStyle.toPersonality(): CoachPersonality {
    return when (this) {
        CoachStyle.CALM -> CoachPersonality.CALM
        CoachStyle.FRIENDLY -> CoachPersonality.FRIENDLY
        CoachStyle.HYPE -> CoachPersonality.HYPE
        CoachStyle.MILITARY -> CoachPersonality.MILITARY
    }
}

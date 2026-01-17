package com.pushprime.voice

import android.content.Context
import kotlin.math.max
import kotlin.math.min

class SystemTtsVoiceProvider(
    context: Context,
    private val onUnavailable: (String) -> Unit
) : VoiceProvider, VoiceProviderLifecycle, VoiceTypeSupport {
    private val engine = VoiceCoachEngine(context, onUnavailable)
    private var personality: CoachPersonality = CoachPersonality.FRIENDLY
    private var intensity: CoachIntensity = CoachIntensity.MEDIUM

    override val isAvailable: Boolean
        get() = engine.isAvailable

    override fun speak(text: String, queueIfBusy: Boolean) {
        engine.speak(text, queueIfBusy)
    }

    override fun stop() {
        engine.stop()
    }

    override fun shutdown() {
        engine.shutdown()
    }

    override fun setStyle(personality: CoachPersonality) {
        this.personality = personality
        applyStyle()
    }

    override fun setIntensity(level: CoachIntensity) {
        intensity = level
        applyStyle()
    }

    override fun setVoiceType(voiceType: VoiceType) {
        engine.setVoiceType(voiceType)
    }

    private fun applyStyle() {
        val baseStyle = when (personality) {
            CoachPersonality.CALM -> VoiceStyle(pitch = 0.9f, rate = 0.9f)
            CoachPersonality.HYPE -> VoiceStyle(pitch = 1.2f, rate = 1.1f)
            CoachPersonality.MILITARY -> VoiceStyle(pitch = 0.8f, rate = 1.05f)
            CoachPersonality.FRIENDLY -> VoiceStyle(pitch = 1.0f, rate = 1.0f)
        }
        val intensityMultiplier = when (intensity) {
            CoachIntensity.LOW -> 0.95f
            CoachIntensity.MEDIUM -> 1.0f
            CoachIntensity.HIGH -> 1.08f
        }
        val pitch = clamp(baseStyle.pitch * intensityMultiplier, 0.7f, 1.4f)
        val rate = clamp(baseStyle.rate * intensityMultiplier, 0.7f, 1.4f)
        engine.setStyle(pitch = pitch, rate = rate)
    }

    private fun clamp(value: Float, min: Float, max: Float): Float {
        return max(min, min(value, max))
    }

    private data class VoiceStyle(val pitch: Float, val rate: Float)
}

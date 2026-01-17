package com.pushprime.voice

import android.os.SystemClock
import kotlin.math.abs

class VoiceCoachController(
    private val provider: VoiceProvider
) {
    private var settings: VoiceCoachSettings = VoiceCoachSettings()
    private var muted = false
    private var hasSpokenStart = false
    private var hasSpokenHalfway = false
    private var hasSpokenFinalMinute = false
    private var hasSpokenCompletion = false
    private var lastMotivationSecond: Int? = null
    private var lastPhraseIndex = 0
    private var lastSpokenAtMs = 0L

    fun updateSettings(newSettings: VoiceCoachSettings) {
        settings = newSettings
        provider.setStyle(newSettings.personality)
        provider.setIntensity(newSettings.frequency.toIntensity())
        if (provider is VoiceTypeSupport) {
            provider.setVoiceType(newSettings.voiceType)
        }
    }

    fun setMuted(muted: Boolean) {
        this.muted = muted
        if (muted) {
            provider.stop()
        }
    }

    fun onSessionStart() {
        if (!hasSpokenStart) {
            hasSpokenStart = true
            speak(VoiceCoachPhrases.sessionStart(), queueIfBusy = true)
        }
    }

    fun onRestStart() {
        speak(VoiceCoachPhrases.restStart(), queueIfBusy = true)
    }

    fun onRestEnd() {
        speak(VoiceCoachPhrases.restEnd(), queueIfBusy = true)
    }

    fun onComplete() {
        if (!hasSpokenCompletion) {
            hasSpokenCompletion = true
            speak(VoiceCoachPhrases.completion(), queueIfBusy = true)
        }
    }

    fun onTick(elapsedSeconds: Int, totalSeconds: Int?, isRest: Boolean) {
        if (!canSpeakNow()) return
        val restOnlyBlock = settings.speakDuringRestOnly && !isRest
        if (totalSeconds != null && totalSeconds > 0 && !restOnlyBlock) {
            if (!hasSpokenHalfway && elapsedSeconds >= totalSeconds / 2) {
                hasSpokenHalfway = true
                speak(VoiceCoachPhrases.halfway(), queueIfBusy = false)
            }
            if (!hasSpokenFinalMinute && (totalSeconds - elapsedSeconds) <= 60) {
                hasSpokenFinalMinute = true
                speak(VoiceCoachPhrases.finalMinute(), queueIfBusy = false)
            }
        }
        val interval = settings.frequency.intervalSeconds
        if (interval <= 0 || elapsedSeconds == 0) return
        if (elapsedSeconds % interval != 0) return
        if (lastMotivationSecond == elapsedSeconds) return
        if (settings.speakDuringRestOnly && !isRest) return
        lastMotivationSecond = elapsedSeconds
        speak(nextMotivationPhrase(), queueIfBusy = false)
    }

    private fun nextMotivationPhrase(): String {
        val phrases = VoiceCoachPhrases.motivationSet(settings.personality)
        if (phrases.isEmpty()) return VoiceCoachPhrases.halfway()
        val phrase = phrases[lastPhraseIndex % phrases.size]
        lastPhraseIndex += 1
        return phrase
    }

    private fun speak(text: String, queueIfBusy: Boolean) {
        if (!canSpeakNow()) return
        val now = SystemClock.elapsedRealtime()
        if (abs(now - lastSpokenAtMs) < 800) return
        lastSpokenAtMs = now
        provider.speak(text, queueIfBusy)
    }

    private fun canSpeakNow(): Boolean {
        return settings.enabled && !muted && provider.isAvailable
    }

    private fun CoachFrequency.toIntensity(): CoachIntensity {
        return when (this) {
            CoachFrequency.LOW -> CoachIntensity.LOW
            CoachFrequency.MEDIUM -> CoachIntensity.MEDIUM
            CoachFrequency.HIGH -> CoachIntensity.HIGH
        }
    }
}

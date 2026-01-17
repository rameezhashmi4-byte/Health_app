package com.pushprime.coach

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class SystemTtsVoiceProvider(context: Context) : VoiceProvider, TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false
    private var isMuted = false
    private var isEnabled = true
    private var style: CoachStyle = CoachStyle.FRIENDLY

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isReady = true
            applyStyle()
        }
    }

    override fun speak(text: String) {
        if (!isEnabled || isMuted || !isReady) return
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        tts?.stop()
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "coach_${System.nanoTime()}")
    }

    override fun stop() {
        tts?.stop()
    }

    override fun setStyle(style: CoachStyle) {
        this.style = style
        applyStyle()
    }

    override fun setMuted(isMuted: Boolean) {
        this.isMuted = isMuted
        if (isMuted) {
            stop()
        }
    }

    override fun setEnabled(isEnabled: Boolean) {
        this.isEnabled = isEnabled
        if (!isEnabled) {
            stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    private fun applyStyle() {
        val rate = when (style) {
            CoachStyle.CALM -> 0.9f
            CoachStyle.FRIENDLY -> 1.0f
            CoachStyle.HYPE -> 1.1f
            CoachStyle.MILITARY -> 1.0f
        }
        val pitch = when (style) {
            CoachStyle.CALM -> 0.9f
            CoachStyle.FRIENDLY -> 1.0f
            CoachStyle.HYPE -> 1.1f
            CoachStyle.MILITARY -> 0.95f
        }
        tts?.setSpeechRate(rate)
        tts?.setPitch(pitch)
    }
}

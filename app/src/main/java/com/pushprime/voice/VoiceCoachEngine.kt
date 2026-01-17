package com.pushprime.voice

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

class VoiceCoachEngine(
    context: Context,
    private val onUnavailable: (String) -> Unit
) : TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = TextToSpeech(appContext, this)
    private var isReady = false
    private var didNotifyUnavailable = false
    private var pendingVoiceType: VoiceType = VoiceType.SYSTEM_DEFAULT
    private var pendingPitch = 1.0f
    private var pendingRate = 1.0f

    val isAvailable: Boolean
        get() = isReady && (tts != null)

    override fun onInit(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) {
            notifyUnavailable("Voice coach unavailable on this device.")
            return
        }
        val result = engine.setLanguage(Locale.getDefault())
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            notifyUnavailable("Voice coach language not supported.")
            return
        }
        isReady = true
        engine.setPitch(pendingPitch)
        engine.setSpeechRate(pendingRate)
        applyVoiceType(pendingVoiceType)
    }

    fun setVoiceType(voiceType: VoiceType) {
        pendingVoiceType = voiceType
        if (isReady) {
            applyVoiceType(voiceType)
        }
    }

    fun setStyle(pitch: Float, rate: Float) {
        pendingPitch = pitch
        pendingRate = rate
        if (isReady) {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(rate)
        }
    }

    fun speak(text: String, queueIfBusy: Boolean): Boolean {
        val engine = tts ?: return false
        if (!isAvailable) return false
        if (engine.isSpeaking && !queueIfBusy) return false
        val queueMode = if (engine.isSpeaking && queueIfBusy) {
            TextToSpeech.QUEUE_ADD
        } else {
            TextToSpeech.QUEUE_FLUSH
        }
        val result = engine.speak(
            text,
            queueMode,
            null,
            "voice_coach_${System.currentTimeMillis()}"
        )
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun applyVoiceType(voiceType: VoiceType) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        val engine = tts ?: return
        val voices = engine.voices ?: return
        val selected = when (voiceType) {
            VoiceType.MALE -> findVoiceWithHint(voices, listOf("male", "man", "m"))
            VoiceType.FEMALE -> findVoiceWithHint(voices, listOf("female", "woman", "f"))
            VoiceType.SYSTEM_DEFAULT -> engine.defaultVoice
        }
        if (selected != null) {
            engine.voice = selected
        }
    }

    private fun findVoiceWithHint(voices: Set<Voice>, hints: List<String>): Voice? {
        return voices.firstOrNull { voice ->
            val name = voice.name.lowercase(Locale.getDefault())
            val features = voice.features?.joinToString(" ")?.lowercase(Locale.getDefault()).orEmpty()
            hints.any { hint ->
                name.contains(hint) || features.contains(hint)
            }
        }
    }

    private fun notifyUnavailable(message: String) {
        if (didNotifyUnavailable) return
        didNotifyUnavailable = true
        onUnavailable(message)
    }
}

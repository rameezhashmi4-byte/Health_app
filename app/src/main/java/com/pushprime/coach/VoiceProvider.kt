package com.pushprime.coach

interface VoiceProvider {
    fun speak(text: String)
    fun stop()
    fun setStyle(style: CoachStyle)
    fun setMuted(isMuted: Boolean)
    fun setEnabled(isEnabled: Boolean)
}

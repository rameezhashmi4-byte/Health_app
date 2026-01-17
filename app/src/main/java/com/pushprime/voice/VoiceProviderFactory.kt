package com.pushprime.voice

import android.content.Context
import com.pushprime.data.OpenAiKeyStore

class VoiceProviderFactory(
    private val context: Context,
    private val keyStore: OpenAiKeyStore
) {
    fun create(
        settings: VoiceCoachSettings,
        onUnavailable: (String) -> Unit
    ): VoiceProvider {
        return when (settings.provider) {
            VoiceProviderType.SYSTEM -> SystemTtsVoiceProvider(context, onUnavailable)
            VoiceProviderType.AI_OPENAI -> {
                val apiKey = keyStore.getApiKey()
                if (apiKey.isNullOrBlank()) {
                    onUnavailable("AI Voice needs an OpenAI API key.")
                    OpenAiTtsVoiceProvider("")
                } else {
                    OpenAiTtsVoiceProvider(apiKey)
                }
            }
        }
    }
}

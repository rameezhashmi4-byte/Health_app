package com.pushprime.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AiCoachSecureStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "ai_coach_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveOpenAiKey(apiKey: String) {
        prefs.edit().putString(KEY_OPENAI, apiKey).apply()
    }

    fun getOpenAiKey(): String? = prefs.getString(KEY_OPENAI, null)

    fun clearOpenAiKey() {
        prefs.edit().remove(KEY_OPENAI).apply()
    }

    private companion object {
        const val KEY_OPENAI = "openai_api_key"
    }
}

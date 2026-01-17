package com.pushprime.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class OpenAiKeyStore(
    context: Context
) {
    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            "openai_voice_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(apiKey: String): Result<Unit> {
        return runCatching {
            prefs.edit().putString(KEY_OPENAI_API, apiKey.trim()).apply()
        }
    }

    fun getApiKey(): String? {
        return prefs.getString(KEY_OPENAI_API, null)
    }

    fun clearApiKey() {
        prefs.edit().remove(KEY_OPENAI_API).apply()
    }

    companion object {
        private const val KEY_OPENAI_API = "openai_api_key"
    }
}

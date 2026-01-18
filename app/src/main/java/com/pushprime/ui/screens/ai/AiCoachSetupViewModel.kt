package com.pushprime.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AiCoachMode
import com.pushprime.data.AiCoachSecureStore
import com.pushprime.data.AiCoachSettingsRepository
import com.pushprime.data.ai.OpenAiCoachProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import javax.inject.Inject

data class TestResult(
    val success: Boolean,
    val message: String,
    val baseUrl: String
)

data class AiCoachSetupState(
    val statusMessage: String? = null,
    val isVerifying: Boolean = false,
    val testResult: TestResult? = null
)

@HiltViewModel
class AiCoachSetupViewModel @Inject constructor(
    private val settingsRepository: AiCoachSettingsRepository,
    private val secureStore: AiCoachSecureStore
) : ViewModel() {
    private val client = OkHttpClient()
    private val _state = MutableStateFlow(AiCoachSetupState())
    val state: StateFlow<AiCoachSetupState> = _state.asStateFlow()

    val settings = settingsRepository.settings

    fun updateMode(mode: AiCoachMode) {
        viewModelScope.launch {
            settingsRepository.updateMode(mode)
        }
    }

    fun updateModelName(modelName: String) {
        viewModelScope.launch {
            settingsRepository.updateModelName(modelName)
        }
    }

    fun updateBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            settingsRepository.updateBaseUrl(baseUrl)
        }
    }

    fun getSavedKey(): String? = secureStore.getOpenAiKey()

    fun testAiCoach() {
        val apiKey = secureStore.getOpenAiKey()
        if (apiKey.isNullOrBlank()) {
            _state.value = _state.value.copy(statusMessage = "No API key saved.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isVerifying = true, statusMessage = "Testing AI Coach...")
            val currentSettings = settingsRepository.settings.first()
            val provider = OpenAiCoachProvider(
                apiKey = apiKey,
                modelName = currentSettings.modelName,
                baseUrl = currentSettings.baseUrl,
                client = client
            )

            val reply = try {
                provider.sendMessage("Say hello", "Test diagnostics")
            } catch (e: Exception) {
                "Error: ${e.localizedMessage}"
            }

            val isSuccess = !reply.contains("failed", ignoreCase = true) && 
                           !reply.contains("misconfigured", ignoreCase = true) &&
                           !reply.contains("error", ignoreCase = true)

            _state.value = _state.value.copy(
                isVerifying = false,
                testResult = TestResult(
                    success = isSuccess,
                    message = reply,
                    baseUrl = currentSettings.baseUrl
                )
            )
        }
    }

    fun verifyAndSaveKey(apiKey: String) {
        if (apiKey.isBlank()) {
            _state.value = AiCoachSetupState(statusMessage = "Enter an API key.", isVerifying = false)
            return
        }
        viewModelScope.launch {
            _state.value = AiCoachSetupState(statusMessage = "Verifying...", isVerifying = true)
            val currentSettings = settingsRepository.settings.first()
            val provider = OpenAiCoachProvider(
                apiKey = apiKey,
                modelName = currentSettings.modelName,
                baseUrl = currentSettings.baseUrl,
                client = client
            )
            val result = runCatching { provider.sendMessage("Ping", "Verification request") }
            val reply = result.getOrElse { "Error: ${it.localizedMessage}" }
            
            val isSuccess = !reply.contains("failed", ignoreCase = true) && 
                           !reply.contains("misconfigured", ignoreCase = true) &&
                           !reply.contains("error", ignoreCase = true)

            if (isSuccess) {
                secureStore.saveOpenAiKey(apiKey)
                _state.value = AiCoachSetupState(statusMessage = "Verified & saved.", isVerifying = false)
            } else {
                _state.value = AiCoachSetupState(statusMessage = reply, isVerifying = false)
            }
        }
    }
}

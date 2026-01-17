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

data class AiCoachSetupState(
    val statusMessage: String? = null,
    val isVerifying: Boolean = false
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

    fun getSavedKey(): String? = secureStore.getOpenAiKey()

    fun verifyAndSaveKey(apiKey: String) {
        if (apiKey.isBlank()) {
            _state.value = AiCoachSetupState(statusMessage = "Enter an API key.", isVerifying = false)
            return
        }
        viewModelScope.launch {
            _state.value = AiCoachSetupState(statusMessage = "Verifying...", isVerifying = true)
            val modelName = settingsRepository.settings.first().modelName
            val provider = OpenAiCoachProvider(apiKey = apiKey, modelName = modelName, client = client)
            val result = runCatching { provider.sendMessage("Ping", "Verification request") }
            if (result.isSuccess) {
                secureStore.saveOpenAiKey(apiKey)
                _state.value = AiCoachSetupState(statusMessage = "Verified & saved.", isVerifying = false)
            } else {
                _state.value = AiCoachSetupState(statusMessage = "Verification failed.", isVerifying = false)
            }
        }
    }
}

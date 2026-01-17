package com.pushprime.ui.screens.profile_setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AuthRepository
import com.pushprime.data.ProfileRepository
import com.pushprime.data.StepsRepository
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.SexOption
import com.pushprime.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val stepsRepository: StepsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _step = MutableStateFlow(0)
    val step: StateFlow<Int> = _step.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var fullName = ""
    var goal: FitnessGoal = FitnessGoal.GET_STRONGER
    var experience: ExperienceLevel = ExperienceLevel.BEGINNER
    var weightKg: Double? = null
    var heightCm: Double? = null
    var age: Int? = null
    var sex: SexOption? = null
    var stepTrackingEnabled: Boolean = false

    fun nextStep() {
        _step.value = (_step.value + 1).coerceAtMost(3)
    }

    fun prevStep() {
        _step.value = (_step.value - 1).coerceAtLeast(0)
    }

    fun setError(message: String?) {
        _error.value = message
    }

    fun toggleStepTracking(enabled: Boolean) {
        stepTrackingEnabled = enabled
    }

    fun requestStepTrackingPermission(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Health Connect helper is a safe no-op if unavailable
            val granted = stepsRepository.getTodaySteps() >= 0
            stepTrackingEnabled = granted || stepTrackingEnabled
            onComplete(granted)
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val uid = authRepository.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _error.value = "Not logged in. Please sign in again."
            return
        }

        val profile = UserProfile(
            fullName = fullName.trim(),
            goal = goal,
            experience = experience,
            weightKg = weightKg ?: 0.0,
            heightCm = heightCm ?: 0.0,
            age = age,
            sex = sex,
            stepTrackingEnabled = stepTrackingEnabled
        )

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            profileRepository.saveLocalProfile(profile)
            val result = profileRepository.saveRemoteProfile(uid, profile)
            _isSaving.value = false
            if (result.isFailure) {
                _error.value = "Saved locally. Cloud sync will retry later."
            }
            onSuccess()
        }
    }
}

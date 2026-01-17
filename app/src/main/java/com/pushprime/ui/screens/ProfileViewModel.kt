package com.pushprime.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pushprime.data.LocalStore
import com.pushprime.data.ProfileRepository
import com.pushprime.data.SessionDao
import com.pushprime.data.StepsDay
import com.pushprime.data.StepsRepository
import com.pushprime.data.StreakRepository
import com.pushprime.data.latestSession
import com.pushprime.model.ExperienceLevel
import com.pushprime.model.FitnessGoal
import com.pushprime.model.UserProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

data class ProfileUiState(
    val profile: UserProfile = UserProfile.placeholder("", "RAMBOOST User"),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val streakDays: Int = 0,
    val hasSessions: Boolean = false,
    val lastWorkoutLabel: String? = null,
    val sessionsThisWeek: Int = 0,
    val stepsToday: Long = 0,
    val weeklySteps: List<StepsDay> = emptyList(),
    val stepTrackingEnabled: Boolean = false
)

data class ProfileEditInput(
    val name: String,
    val goal: FitnessGoal,
    val experience: ExperienceLevel,
    val weightKg: Double,
    val heightCm: Double
)

data class ProfileEvent(
    val message: String,
    val dismissSheet: Boolean = false,
    val useToast: Boolean = false
)

class ProfileViewModel(
    private val localStore: LocalStore,
    private val sessionDao: SessionDao,
    private val stepsRepository: StepsRepository,
    private val profileRepository: ProfileRepository,
    private val streakRepository: StreakRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val lastWorkoutFormatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private var currentUid: String? = null

    init {
        observeLocalProfile()
        observeStepTracking()
        observeSessions()
        observeStreak()
    }

    fun loadProfile(uid: String?, fallbackName: String) {
        currentUid = uid
        val resolvedUid = uid ?: ""
        val placeholder = UserProfile.placeholder(resolvedUid, fallbackName)
        _uiState.update { it.copy(profile = placeholder) }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (uid != null) {
                val cached = profileRepository.getCachedProfile(uid)
                if (cached != null) {
                    _uiState.update { it.copy(profile = cached) }
                }
                val remote = profileRepository.fetchRemoteProfile(uid)
                if (remote != null) {
                    profileRepository.saveLocal(remote)
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun saveProfile(input: ProfileEditInput) {
        val uid = currentUid.orEmpty()
        if (uid.isBlank()) {
            emitMessage("Please sign in to update your profile.")
            return
        }
        if (input.name.isBlank()) {
            emitMessage("Name cannot be empty.")
            return
        }
        if (input.weightKg <= 0.0) {
            emitMessage("Weight must be greater than 0.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val existing = _uiState.value.profile
            val updated = existing.copy(
                uid = uid,
                fullName = input.name.trim(),
                goal = input.goal,
                experience = input.experience,
                weightKg = input.weightKg,
                heightCm = input.heightCm,
                updatedAt = System.currentTimeMillis()
            )
            profileRepository.saveLocal(updated)
            val remoteResult = profileRepository.updateRemoteProfile(updated)
            _uiState.update { it.copy(isSaving = false) }
            if (remoteResult.isSuccess) {
                emitMessage("Profile updated.", dismissSheet = true)
            } else {
                emitMessage("Saved locally. Sync failed.")
            }
        }
    }

    fun setStepTrackingEnabled(enabled: Boolean) {
        localStore.setStepTrackingEnabled(enabled)
        if (enabled) {
            refreshSteps()
        } else {
            _uiState.update { it.copy(stepsToday = 0, weeklySteps = emptyList()) }
        }
    }

    fun onExportData() {
        emitMessage("Coming soon.", useToast = true)
    }

    fun onPrivacy() {
        emitMessage("Coming soon.", useToast = true)
    }

    private fun observeLocalProfile() {
        viewModelScope.launch {
            localStore.profile.collect { profile ->
                if (profile != null && (currentUid == null || profile.uid == currentUid)) {
                    _uiState.update { it.copy(profile = profile) }
                }
            }
        }
    }

    private fun observeStepTracking() {
        viewModelScope.launch {
            localStore.stepTrackingEnabled.collect { enabled ->
                _uiState.update { it.copy(stepTrackingEnabled = enabled) }
                if (enabled) {
                    refreshSteps()
                }
            }
        }
    }

    private fun observeSessions() {
        viewModelScope.launch {
            sessionDao.getAllSessions().collect { sessions ->
                val lastSession = latestSession(sessions)
                val lastWorkout = lastSession?.startTime?.let { lastWorkoutFormatter.format(Date(it)) }
                val sessionsThisWeek = sessions.count { isWithinLast7Days(it.date) }
                _uiState.update {
                    it.copy(
                        hasSessions = sessions.isNotEmpty(),
                        lastWorkoutLabel = lastWorkout,
                        sessionsThisWeek = sessionsThisWeek
                    )
                }
            }
        }
    }

    private fun observeStreak() {
        viewModelScope.launch {
            streakRepository.streakState.collect { streakState ->
                _uiState.update { it.copy(streakDays = streakState.currentStreakDays) }
            }
        }
    }

    private fun refreshSteps() {
        viewModelScope.launch {
            val todaySteps = stepsRepository.getTodaySteps()
            val weekly = stepsRepository.getLast7DaysSteps()
            _uiState.update {
                it.copy(
                    stepsToday = todaySteps,
                    weeklySteps = weekly
                )
            }
        }
    }

    private fun isWithinLast7Days(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, dateFormatter)
            val today = LocalDate.now(ZoneId.systemDefault())
            val start = today.minusDays(6)
            !date.isBefore(start) && !date.isAfter(today)
        } catch (_: Exception) {
            false
        }
    }

    private fun emitMessage(
        message: String,
        dismissSheet: Boolean = false,
        useToast: Boolean = false
    ) {
        viewModelScope.launch {
            _events.emit(ProfileEvent(message, dismissSheet, useToast))
        }
    }
}

class ProfileViewModelFactory(
    private val localStore: LocalStore,
    private val sessionDao: SessionDao,
    private val stepsRepository: StepsRepository,
    private val profileRepository: ProfileRepository,
    private val streakRepository: StreakRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(
                localStore,
                sessionDao,
                stepsRepository,
                profileRepository,
                streakRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.pushprime.ui.screens.quick_session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AchievementsRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.QuickSessionDao
import com.pushprime.data.SessionDao
import com.pushprime.data.todaySessionDate
import com.pushprime.model.ActivityType
import com.pushprime.model.FitnessGoal
import com.pushprime.model.QuickSessionDifficulty
import com.pushprime.model.QuickSessionLog
import com.pushprime.model.QuickSessionTemplate
import com.pushprime.model.QuickSessionTemplates
import com.pushprime.model.SessionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class QuickSessionUiState(
    val templates: List<QuickSessionTemplate> = QuickSessionTemplates.all,
    val recommendedIds: Set<String> = emptySet(),
    val showStreakRisk: Boolean = false
)

@HiltViewModel
class QuickSessionViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val sessionDao: SessionDao,
    private val quickSessionDao: QuickSessionDao,
    private val achievementsRepository: AchievementsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuickSessionUiState())
    val uiState: StateFlow<QuickSessionUiState> = _uiState.asStateFlow()

    init {
        observeGoalRecommendations()
        observeStreakRisk()
    }

    fun getTemplate(templateId: String): QuickSessionTemplate? {
        return QuickSessionTemplates.byId(templateId)
    }

    fun saveCompletedSession(
        templateId: String,
        notes: String,
        markAsWorkout: Boolean,
        onSaved: () -> Unit
    ) {
        val template = QuickSessionTemplates.byId(templateId) ?: return
        val cleanedNotes = notes.trim().takeIf { it.isNotEmpty() }
        val durationSeconds = template.rounds * (template.workSeconds + template.restSeconds)
        val durationMinutes = (durationSeconds / 60).coerceAtLeast(1)
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            quickSessionDao.insert(
                QuickSessionLog(
                    templateId = templateId,
                    durationMinutes = durationMinutes,
                    completed = true,
                    notes = cleanedNotes
                )
            )

            val session = SessionEntity(
                userId = resolveUserId(),
                startTime = now - (durationSeconds * 1000L),
                endTime = now,
                activityType = ActivityType.QUICK_SESSION.name,
                exerciseId = template.name,
                mode = "TIMER",
                totalSeconds = durationSeconds,
                intensity = mapDifficulty(template.difficulty),
                intervalsEnabled = true,
                warmupEnabled = false,
                durationMinutes = durationMinutes,
                tags = buildTag(templateId, markAsWorkout),
                notes = cleanedNotes
            )
            sessionDao.insert(session)
            localStore.recordSessionDate(session.date)
            achievementsRepository.recalcAchievements(emitPopup = true)

            onSaved()
        }
    }

    private fun observeGoalRecommendations() {
        viewModelScope.launch {
            localStore.profile.collectLatest { profile ->
                val recommended = recommendedForGoal(profile?.goal)
                _uiState.update { it.copy(recommendedIds = recommended) }
            }
        }
    }

    private fun observeStreakRisk() {
        viewModelScope.launch {
            sessionDao.getSessionsByDate(todaySessionDate()).collectLatest { sessions ->
                val isLateDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 18
                val show = isLateDay && sessions.isEmpty()
                _uiState.update { it.copy(showStreakRisk = show) }
            }
        }
    }

    private fun mapDifficulty(difficulty: QuickSessionDifficulty): String {
        return when (difficulty) {
            QuickSessionDifficulty.EASY -> "LOW"
            QuickSessionDifficulty.MEDIUM -> "MEDIUM"
            QuickSessionDifficulty.HARD -> "HIGH"
        }
    }

    private fun resolveUserId(): String {
        val user = localStore.user.value
        val profile = localStore.profile.value
        return when {
            user != null && user.username.isNotBlank() -> user.username
            profile != null && profile.fullName.isNotBlank() -> profile.fullName
            else -> "anonymous"
        }
    }

    private fun recommendedForGoal(goal: FitnessGoal?): Set<String> {
        return when (goal) {
            FitnessGoal.LOSE_FAT -> setOf("fat_burner")
            FitnessGoal.BUILD_MUSCLE -> setOf("upper_body_blast")
            FitnessGoal.GET_STRONGER -> setOf("pull_up_booster", "legs_glutes")
            FitnessGoal.IMPROVE_STAMINA -> setOf("fat_burner")
            null -> emptySet()
        }
    }

    private fun buildTag(templateId: String, markAsWorkout: Boolean): String {
        return if (markAsWorkout) {
            "quick_session:$templateId"
        } else {
            "quick_session:$templateId:unmarked"
        }
    }
}

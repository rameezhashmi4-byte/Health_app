package com.pushprime.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AchievementsRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.SessionDao
import com.pushprime.data.SessionStats
import com.pushprime.data.StepsRepository
import com.pushprime.data.StreakRepository
import com.pushprime.data.WorkoutPlanRepository
import com.pushprime.model.DailyStatusType
import com.pushprime.model.GeneratedWorkoutPlanSummary
import com.pushprime.model.SessionEntity
import com.pushprime.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val user: User? = null,
    val streak: Int = 0,
    val bestStreakDays: Int = 0,
    val freezeTokensRemaining: Int = 0,
    val restDaysLeftThisWeek: Int = 0,
    val todayStatus: DailyStatusType = DailyStatusType.MISSED,
    val hasWorkoutToday: Boolean = false,
    val isRestDayToday: Boolean = false,
    val isStreakProtectedToday: Boolean = false,
    val weeklySessions: Int = 0,
    val todaySteps: Long = 0,
    val isStepsEnabled: Boolean = true,
    val weeklyProgress: List<Int> = List(7) { 0 },
    val caloriesBurned: Int = 0,
    val lastWorkoutLabel: String? = null,
    val savedPlans: List<GeneratedWorkoutPlanSummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val sessionDao: SessionDao,
    private val stepsRepository: StepsRepository,
    private val streakRepository: StreakRepository,
    private val achievementsRepository: AchievementsRepository,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observeStreakUpdates()
        refreshStreakState()
    }

    fun markRestDay() {
        viewModelScope.launch {
            streakRepository.markRestDay(LocalDate.now())
        }
    }

    private fun loadData() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        viewModelScope.launch {
            // Combine flows for user and sessions
            combine(
                localStore.user,
                sessionDao.getAllSessions(),
                workoutPlanRepository.getSavedPlans(),
                streakRepository.streakState,
                streakRepository.observeStatusForDate(today)
            ) { user, sessions, savedPlans, streakState, todayStatus ->
                val hasWorkoutToday = sessions.any { it.date == todayStr } ||
                    todayStatus?.status == DailyStatusType.WORKOUT.name
                val statusType = when {
                    hasWorkoutToday -> DailyStatusType.WORKOUT
                    todayStatus?.status == DailyStatusType.REST.name -> DailyStatusType.REST
                    todayStatus?.status == DailyStatusType.FROZEN.name -> DailyStatusType.FROZEN
                    else -> DailyStatusType.MISSED
                }
                val weeklySessions = SessionStats.calculateWeeklySessions(sessions)
                val weeklyProgress = SessionStats.calculateWeeklyProgress(sessions)
                val todaySteps = stepsRepository.getTodaySteps()
                val lastWorkoutLabel = formatLastWorkoutLabel(sessions)
                val restDaysLeft = (StreakRepository.MAX_REST_DAYS_PER_WEEK -
                    streakState.restDaysUsedThisWeek).coerceAtLeast(0)
                
                val stepsAsInt = todaySteps.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                achievementsRepository.recalcAchievements(
                    todayStepsOverride = stepsAsInt,
                    emitPopup = false
                )

                HomeUiState(
                    user = user,
                    streak = streakState.currentStreakDays,
                    bestStreakDays = streakState.longestStreakDays,
                    freezeTokensRemaining = streakState.freezeTokensRemaining,
                    restDaysLeftThisWeek = restDaysLeft,
                    todayStatus = statusType,
                    hasWorkoutToday = hasWorkoutToday,
                    isRestDayToday = statusType == DailyStatusType.REST,
                    isStreakProtectedToday = statusType == DailyStatusType.FROZEN,
                    weeklySessions = weeklySessions,
                    todaySteps = todaySteps,
                    isStepsEnabled = todaySteps > 0 || true, // Simplified check
                    weeklyProgress = weeklyProgress,
                    caloriesBurned = (weeklySessions * 300), // Placeholder logic
                    lastWorkoutLabel = lastWorkoutLabel,
                    savedPlans = savedPlans,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun formatLastWorkoutLabel(sessions: List<SessionEntity>): String? {
        val latest = sessions.maxByOrNull { it.startTime } ?: return null
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterday)
        return when (latest.date) {
            today -> "Today"
            yesterdayStr -> "Yesterday"
            else -> latest.date
        }
    }

    private fun observeStreakUpdates() {
        val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            sessionDao.getAllSessions()
                .map { sessions -> sessions.count { it.date == todayStr } }
                .distinctUntilChanged()
                .collect { count ->
                    if (count > 0) {
                        streakRepository.evaluateStreak(LocalDate.now(), force = true)
                    }
                }
        }
    }

    private fun refreshStreakState() {
        viewModelScope.launch {
            streakRepository.evaluateStreak(LocalDate.now(), force = false)
        }
    }
}

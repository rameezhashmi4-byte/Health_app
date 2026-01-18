package com.pushprime.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AiCoachMode
import com.pushprime.data.AiCoachSecureStore
import com.pushprime.data.AiCoachSettingsRepository
import com.pushprime.data.NutritionRepository
import com.pushprime.data.PullupMaxTestDao
import com.pushprime.data.PullupSessionDao
import com.pushprime.data.SessionDao
import com.pushprime.data.StepsRepository
import com.pushprime.data.calculateStreakFromDates
import com.pushprime.data.dateFromTimestamp
import com.pushprime.data.todaySessionDate
import com.pushprime.data.ai.BasicCoachProvider
import com.pushprime.data.ai.OpenAiCoachProvider
import com.pushprime.data.LocalStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class ChatRole {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val role: ChatRole,
    val content: String
)

data class AiCoachUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val stepsRepository: StepsRepository,
    private val sessionDao: SessionDao,
    private val nutritionRepository: NutritionRepository,
    private val pullupSessionDao: PullupSessionDao,
    private val pullupMaxTestDao: PullupMaxTestDao,
    private val settingsRepository: AiCoachSettingsRepository,
    private val secureStore: AiCoachSecureStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiCoachUiState())
    val uiState: StateFlow<AiCoachUiState> = _uiState.asStateFlow()

    private val client = OkHttpClient()

    fun sendMessage(message: String) {
        if (message.isBlank()) return
        val updated = _uiState.value.messages + ChatMessage(ChatRole.USER, message.trim())
        _uiState.value = _uiState.value.copy(messages = updated, isLoading = true, errorMessage = null)

        viewModelScope.launch {
            val context = buildContextSummary()
            val settings = settingsRepository.settings.first()
            val provider = when {
                settings.mode == AiCoachMode.BASIC -> BasicCoachProvider()
                settings.mode == AiCoachMode.OPENAI -> {
                    val key = secureStore.getOpenAiKey()
                    if (key.isNullOrBlank()) {
                        BasicCoachProvider()
                    } else {
                        OpenAiCoachProvider(
                            apiKey = key,
                            modelName = settings.modelName,
                            baseUrl = settings.baseUrl,
                            client = client
                        )
                    }
                }
                else -> BasicCoachProvider()
            }

            val reply = runCatching { provider.sendMessage(message.trim(), context) }
                .getOrElse { "Coach is unavailable right now." }

            val newMessages = _uiState.value.messages + ChatMessage(ChatRole.ASSISTANT, reply)
            _uiState.value = _uiState.value.copy(messages = newMessages, isLoading = false)
        }
    }

    private suspend fun buildContextSummary(): String {
        val goal = localStore.getUserProfile()?.goal?.name
            ?: localStore.user.value?.fitnessLevel?.name
            ?: "GENERAL"
        val todaySteps = stepsRepository.getTodaySteps()
        val today = todaySessionDate()
        val startDate = dateDaysAgo(6)

        val sessions = sessionDao.getAllSessionsOnce()
        val sessionCount7d = sessionDao.getSessionCountForRange(startDate, today)

        val nutritionEntries = nutritionRepository.getEntriesForDateOnce(today)
        val todayCalories = nutritionEntries.sumOf { it.calories ?: 0 }
        val todayProtein = nutritionEntries.sumOf { it.proteinGrams ?: 0 }

        val pullupSessions = pullupSessionDao.getSessionsForRangeOnce(startOfWeekMillis(), System.currentTimeMillis())
        val weeklyPullups = pullupSessions.sumOf { it.totalReps }
        val pullupMax = pullupMaxTestDao.getAllTestsOnce().maxOfOrNull { it.maxReps } ?: 0

        val streakDates = buildSet {
            addAll(sessions.map { it.date })
            addAll(nutritionRepository.getAllEntriesOnce()
                .filter { (it.calories ?: 0) > 0 || (it.proteinGrams ?: 0) > 0 }
                .map { it.date })
            addAll(pullupSessionDao.getAllSessionsOnce().map { dateFromTimestamp(it.dateTime) })
            addAll(pullupMaxTestDao.getAllTestsOnce().map { dateFromTimestamp(it.dateTime) })
        }
        val streak = calculateStreakFromDates(streakDates)

        return """
            Goal: $goal
            Steps today: $todaySteps
            Streak: $streak days
            Last 7 days sessions: $sessionCount7d
            Nutrition today: $todayCalories kcal, $todayProtein g protein
            Pull-up max: $pullupMax reps
            Pull-up weekly volume: $weeklyPullups
        """.trimIndent()
    }

    private fun dateDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    private fun startOfWeekMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}

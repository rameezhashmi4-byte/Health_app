package com.pushprime.ui.screens.streak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.DailyStatusDao
import com.pushprime.data.SessionDao
import com.pushprime.data.StreakRepository
import com.pushprime.model.DailyStatusType
import com.pushprime.model.SessionEntity
import com.pushprime.model.StreakState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class StreakCalendarDay(
    val date: LocalDate,
    val status: DailyStatusType,
    val sessionCount: Int
)

data class StreakDetailsUiState(
    val streakState: StreakState = StreakState(),
    val days: List<StreakCalendarDay> = emptyList()
)

@HiltViewModel
class StreakDetailsViewModel @Inject constructor(
    private val streakRepository: StreakRepository,
    private val dailyStatusDao: DailyStatusDao,
    private val sessionDao: SessionDao
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val today = LocalDate.now()
    private val startDate = today.minusDays(29)
    private val startDateStr = startDate.format(dateFormatter)
    private val endDateStr = today.format(dateFormatter)

    val uiState: StateFlow<StreakDetailsUiState> = combine(
        streakRepository.streakState,
        dailyStatusDao.observeStatusesBetween(startDateStr, endDateStr),
        sessionDao.getSessionsByDateRange(startDateStr, endDateStr)
    ) { streakState, dailyStatuses, sessions ->
        val statusMap = dailyStatuses.associateBy { it.date }
        val sessionCountMap = sessions.groupBy(SessionEntity::date).mapValues { it.value.size }

        val days = (0..29).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val dateStr = date.format(dateFormatter)
            val storedStatus = statusMap[dateStr]?.status?.let { status ->
                runCatching { DailyStatusType.valueOf(status) }.getOrNull()
            }
            val sessionCount = sessionCountMap[dateStr] ?: 0
            val status = when {
                sessionCount > 0 || storedStatus == DailyStatusType.WORKOUT -> DailyStatusType.WORKOUT
                storedStatus == DailyStatusType.REST -> DailyStatusType.REST
                storedStatus == DailyStatusType.FROZEN -> DailyStatusType.FROZEN
                storedStatus == DailyStatusType.MISSED -> DailyStatusType.MISSED
                else -> DailyStatusType.MISSED
            }
            StreakCalendarDay(date, status, sessionCount)
        }

        StreakDetailsUiState(
            streakState = streakState,
            days = days
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StreakDetailsUiState()
    )

    init {
        viewModelScope.launch {
            streakRepository.evaluateStreak(LocalDate.now(), force = false)
        }
    }
}

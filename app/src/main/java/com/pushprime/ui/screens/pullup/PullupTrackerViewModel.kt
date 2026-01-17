package com.pushprime.ui.screens.pullup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.PullupMaxTestDao
import com.pushprime.data.PullupSessionDao
import com.pushprime.data.dateFromTimestamp
import com.pushprime.model.PullupMaxTest
import com.pushprime.model.PullupSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class PullupTrackerUiState(
    val currentMax: Int = 0,
    val weeklyTotal: Int = 0,
    val lastSessionDate: String = "--",
    val isNewPr: Boolean = false
)

@HiltViewModel
class PullupTrackerViewModel @Inject constructor(
    private val pullupSessionDao: PullupSessionDao,
    private val pullupMaxTestDao: PullupMaxTestDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(PullupTrackerUiState())
    val uiState: StateFlow<PullupTrackerUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val startOfWeek = startOfWeekMillis()
        val endOfWeek = System.currentTimeMillis()

        viewModelScope.launch {
            combine(
                pullupSessionDao.getSessionsForRange(startOfWeek, endOfWeek),
                pullupSessionDao.getAllSessions(),
                pullupMaxTestDao.getAllTests()
            ) { weeklySessions, allSessions, tests ->
                val weeklyTotal = weeklySessions.sumOf { it.totalReps }
                val lastSessionDate = allSessions.firstOrNull()?.dateTime?.let { dateFromTimestamp(it) } ?: "--"
                val currentMax = tests.firstOrNull()?.maxReps ?: 0
                PullupTrackerUiState(
                    currentMax = currentMax,
                    weeklyTotal = weeklyTotal,
                    lastSessionDate = lastSessionDate,
                    isNewPr = isNewPr(tests)
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun isNewPr(tests: List<PullupMaxTest>): Boolean {
        if (tests.isEmpty()) return false
        val latest = tests.first()
        val previousMax = tests.drop(1).maxOfOrNull { it.maxReps } ?: 0
        return latest.maxReps > previousMax
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

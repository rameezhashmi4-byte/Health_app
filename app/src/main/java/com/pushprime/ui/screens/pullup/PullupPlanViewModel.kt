package com.pushprime.ui.screens.pullup

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.PullupMaxTestDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private val Context.pullupPlanStore by preferencesDataStore(name = "pullup_plan")

data class PullupPlanDay(
    val title: String,
    val detail: String
)

data class PullupPlanState(
    val maxReps: Int = 0,
    val plan: List<PullupPlanDay> = emptyList(),
    val lastCompletedDate: String? = null
)

@HiltViewModel
class PullupPlanViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pullupMaxTestDao: PullupMaxTestDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(PullupPlanState())
    val uiState: StateFlow<PullupPlanState> = _uiState.asStateFlow()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        observePlan()
    }

    private fun observePlan() {
        viewModelScope.launch {
            val completionFlow = context.pullupPlanStore.data.map { prefs ->
                prefs[Keys.LastCompletedDate]
            }
            combine(
                pullupMaxTestDao.getAllTests(),
                completionFlow
            ) { tests, completedDate ->
                val maxReps = tests.maxOfOrNull { it.maxReps } ?: 0
                PullupPlanState(
                    maxReps = maxReps,
                    plan = generatePlan(maxReps),
                    lastCompletedDate = completedDate
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun markCompletedToday() {
        val today = dateFormatter.format(Date())
        viewModelScope.launch {
            context.pullupPlanStore.edit { prefs ->
                prefs[Keys.LastCompletedDate] = today
            }
        }
    }

    fun startTodayPlan() {
        markCompletedToday()
    }

    private fun generatePlan(maxReps: Int): List<PullupPlanDay> {
        return when {
            maxReps < 5 -> listOf(
                PullupPlanDay("Day 1", "5 x 50% max reps"),
                PullupPlanDay("Day 2", "Pyramids: 1-2-3-2-1"),
                PullupPlanDay("Day 3", "3 x 60% max reps"),
                PullupPlanDay("Day 4", "Rest or mobility")
            )
            maxReps < 12 -> listOf(
                PullupPlanDay("Day 1", "5 x 60% max reps"),
                PullupPlanDay("Day 2", "Pyramids: 1-2-3-4-3-2-1"),
                PullupPlanDay("Day 3", "4 x 70% max reps"),
                PullupPlanDay("Day 4", "Rest or light negatives")
            )
            else -> listOf(
                PullupPlanDay("Day 1", "6 x 70% max reps"),
                PullupPlanDay("Day 2", "Weighted pyramids or tempo reps"),
                PullupPlanDay("Day 3", "5 x 80% max reps"),
                PullupPlanDay("Day 4", "Rest or accessory work")
            )
        }
    }

    private object Keys {
        val LastCompletedDate = stringPreferencesKey("last_completed_date")
    }
}

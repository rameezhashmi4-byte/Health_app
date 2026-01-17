package com.pushprime.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.NutritionPreferencesRepository
import com.pushprime.data.NutritionRepository
import com.pushprime.data.todaySessionDate
import com.pushprime.model.MealType
import com.pushprime.model.NutritionEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class NutritionDaySummary(
    val date: String,
    val calories: Int
)

data class NutritionUiState(
    val date: String = todaySessionDate(),
    val entries: List<NutritionEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val calorieGoal: Int = 2200,
    val proteinGoal: Int = 150,
    val last7Days: List<NutritionDaySummary> = emptyList()
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository,
    private val nutritionPreferencesRepository: NutritionPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        observeNutrition()
    }

    private fun observeNutrition() {
        val today = todaySessionDate()
        val startDate = getDateDaysAgo(6)

        viewModelScope.launch {
            combine(
                nutritionRepository.getEntriesForDate(today),
                nutritionRepository.getEntriesForDateRange(startDate, today),
                nutritionPreferencesRepository.calorieGoal,
                nutritionPreferencesRepository.proteinGoal
            ) { todayEntries, rangeEntries, calorieGoal, proteinGoal ->
                val totalCalories = todayEntries.sumOf { it.calories ?: 0 }
                val totalProtein = todayEntries.sumOf { it.proteinGrams ?: 0 }
                val last7Days = buildLast7DaySummaries(startDate, today, rangeEntries)
                NutritionUiState(
                    date = today,
                    entries = todayEntries,
                    totalCalories = totalCalories,
                    totalProtein = totalProtein,
                    calorieGoal = calorieGoal,
                    proteinGoal = proteinGoal,
                    last7Days = last7Days
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun quickAddCalories(value: Int) {
        if (value <= 0) return
        viewModelScope.launch {
            nutritionRepository.insert(
                NutritionEntry(
                    mealType = MealType.SNACK.name,
                    name = "Quick calories",
                    calories = value,
                    proteinGrams = null
                )
            )
        }
    }

    fun quickAddProtein(value: Int) {
        if (value <= 0) return
        viewModelScope.launch {
            nutritionRepository.insert(
                NutritionEntry(
                    mealType = MealType.SNACK.name,
                    name = "Quick protein",
                    calories = null,
                    proteinGrams = value
                )
            )
        }
    }

    private fun buildLast7DaySummaries(
        startDate: String,
        endDate: String,
        entries: List<NutritionEntry>
    ): List<NutritionDaySummary> {
        val totalsByDate = entries.groupBy { it.date }.mapValues { day ->
            day.value.sumOf { it.calories ?: 0 }
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            time = formatter.parse(startDate) ?: time
        }
        val summaries = mutableListOf<NutritionDaySummary>()
        while (true) {
            val dateStr = formatter.format(calendar.time)
            summaries.add(NutritionDaySummary(date = dateStr, calories = totalsByDate[dateStr] ?: 0))
            if (dateStr == endDate) break
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return summaries
    }

    private fun getDateDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}

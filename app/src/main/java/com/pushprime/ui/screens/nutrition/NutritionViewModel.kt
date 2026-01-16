package com.pushprime.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.CalorieEngine
import com.pushprime.data.LocalStore
import com.pushprime.data.MacroEngine
import com.pushprime.data.MealPlanGenerator
import com.pushprime.model.Macros
import com.pushprime.model.MealSuggestion
import com.pushprime.model.NutritionGoal
import com.pushprime.model.NutritionSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NutritionUiState(
    val settings: NutritionSettings = NutritionSettings(),
    val targetCalories: Int = 2000,
    val targetMacros: Macros = Macros(150, 200, 65, 2000),
    val mealSuggestions: List<MealSuggestion> = emptyList()
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val localStore: LocalStore,
    private val calorieEngine: CalorieEngine,
    private val macroEngine: MacroEngine,
    private val mealPlanGenerator: MealPlanGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(NutritionUiState())
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        val settings = localStore.loadNutritionSettings()
        updateState(settings)
    }

    fun updateGoal(goal: NutritionGoal) {
        val newSettings = _uiState.value.settings.copy(goal = goal)
        saveAndUpdate(newSettings)
    }

    fun updateRegion(region: String) {
        val newSettings = _uiState.value.settings.copy(region = region)
        saveAndUpdate(newSettings)
    }

    fun toggleHalal(value: Boolean) {
        val newSettings = _uiState.value.settings.copy(isHalal = value)
        saveAndUpdate(newSettings)
    }

    fun toggleVeggie(value: Boolean) {
        val newSettings = _uiState.value.settings.copy(isVeggie = value)
        saveAndUpdate(newSettings)
    }

    fun toggleBudget(value: Boolean) {
        val newSettings = _uiState.value.settings.copy(isBudget = value)
        saveAndUpdate(newSettings)
    }

    fun toggleRestaurantMode(value: Boolean) {
        val newSettings = _uiState.value.settings.copy(restaurantMode = value)
        saveAndUpdate(newSettings)
    }

    private fun saveAndUpdate(settings: NutritionSettings) {
        viewModelScope.launch {
            localStore.saveNutritionSettings(settings)
            updateState(settings)
        }
    }

    private fun updateState(settings: NutritionSettings) {
        val user = localStore.user.value
        val calories = calorieEngine.calculateTarget(user, settings)
        val macros = macroEngine.calculateMacros(calories, settings)
        val suggestions = mealPlanGenerator.generateSuggestions(settings)

        _uiState.update { 
            it.copy(
                settings = settings,
                targetCalories = calories,
                targetMacros = macros,
                mealSuggestions = suggestions
            )
        }
    }
}

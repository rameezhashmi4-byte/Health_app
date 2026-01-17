package com.pushprime.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.NutritionPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NutritionGoalsState(
    val calorieGoal: Int = 2200,
    val proteinGoal: Int = 150
)

@HiltViewModel
class NutritionGoalsViewModel @Inject constructor(
    private val preferencesRepository: NutritionPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NutritionGoalsState())
    val uiState: StateFlow<NutritionGoalsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesRepository.calorieGoal,
                preferencesRepository.proteinGoal
            ) { calories, protein ->
                NutritionGoalsState(calorieGoal = calories, proteinGoal = protein)
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun saveGoals(calorieGoal: Int, proteinGoal: Int) {
        viewModelScope.launch {
            preferencesRepository.updateCalorieGoal(calorieGoal)
            preferencesRepository.updateProteinGoal(proteinGoal)
        }
    }
}

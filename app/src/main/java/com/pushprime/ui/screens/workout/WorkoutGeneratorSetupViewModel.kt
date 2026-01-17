package com.pushprime.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.WorkoutGenerator
import com.pushprime.data.WorkoutGeneratorInputs
import com.pushprime.data.WorkoutPlanRepository
import com.pushprime.model.EquipmentOption
import com.pushprime.model.TrainingStyle
import com.pushprime.model.WorkoutFocus
import com.pushprime.model.WorkoutGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutGeneratorSetupState(
    val goal: WorkoutGoal? = null,
    val timeMinutes: Int? = null,
    val equipment: EquipmentOption? = null,
    val focus: WorkoutFocus? = null,
    val style: TrainingStyle? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
    val generatedPlanId: Long? = null
) {
    val canGenerate: Boolean = goal != null && timeMinutes != null && equipment != null
}

@HiltViewModel
class WorkoutGeneratorSetupViewModel @Inject constructor(
    private val workoutGenerator: WorkoutGenerator,
    private val workoutPlanRepository: WorkoutPlanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutGeneratorSetupState())
    val uiState: StateFlow<WorkoutGeneratorSetupState> = _uiState

    fun selectGoal(goal: WorkoutGoal) {
        _uiState.update { it.copy(goal = goal, errorMessage = null) }
    }

    fun selectTime(timeMinutes: Int) {
        _uiState.update { it.copy(timeMinutes = timeMinutes, errorMessage = null) }
    }

    fun selectEquipment(equipment: EquipmentOption) {
        _uiState.update { it.copy(equipment = equipment, errorMessage = null) }
    }

    fun selectFocus(focus: WorkoutFocus) {
        _uiState.update { state ->
            val newFocus = if (state.focus == focus) null else focus
            state.copy(focus = newFocus)
        }
    }

    fun selectStyle(style: TrainingStyle) {
        _uiState.update { state ->
            val newStyle = if (state.style == style) null else style
            state.copy(style = newStyle)
        }
    }

    fun generatePlan() {
        val state = _uiState.value
        if (!state.canGenerate) {
            _uiState.update { it.copy(errorMessage = "Select goal, time, and equipment to continue.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, errorMessage = null) }
            workoutPlanRepository.deleteDrafts()
            val plan = workoutGenerator.generate(
                WorkoutGeneratorInputs(
                    goal = state.goal!!,
                    timeMinutes = state.timeMinutes!!,
                    equipment = state.equipment!!,
                    focus = state.focus,
                    style = state.style
                )
            )
            val planId = workoutPlanRepository.createDraft(plan)
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    generatedPlanId = planId
                )
            }
        }
    }

    fun consumeGeneratedPlanId() {
        _uiState.update { it.copy(generatedPlanId = null) }
    }
}

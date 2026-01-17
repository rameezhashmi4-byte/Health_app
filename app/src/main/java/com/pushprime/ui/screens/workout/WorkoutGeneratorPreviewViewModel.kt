package com.pushprime.ui.screens.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.WorkoutGenerator
import com.pushprime.data.WorkoutGeneratorInputs
import com.pushprime.data.WorkoutPlanRecord
import com.pushprime.data.WorkoutPlanRepository
import com.pushprime.model.GeneratedWorkoutPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutGeneratorPreviewState(
    val plan: GeneratedWorkoutPlan? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isRegenerating: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class WorkoutGeneratorPreviewViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val workoutGenerator: WorkoutGenerator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val planId: Long = savedStateHandle.get<Long>("planId") ?: 0L

    private val _uiState = MutableStateFlow(WorkoutGeneratorPreviewState())
    val uiState: StateFlow<WorkoutGeneratorPreviewState> = _uiState

    init {
        loadPlan()
    }

    fun loadPlan() {
        viewModelScope.launch {
            val record = workoutPlanRepository.getPlanRecord(planId)
            if (record == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Plan not found.") }
                return@launch
            }
            _uiState.update {
                it.copy(
                    plan = record.plan,
                    isSaved = record.isSaved,
                    isLoading = false
                )
            }
        }
    }

    fun regeneratePlan() {
        val current = _uiState.value.plan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRegenerating = true) }
            val avoid = exerciseSignature(current)
            var updatedPlan = workoutGenerator.generate(
                WorkoutGeneratorInputs(
                    goal = current.goal,
                    timeMinutes = current.timeMinutes,
                    equipment = current.equipment,
                    focus = current.focus,
                    style = current.style
                ),
                avoidExerciseNames = avoid
            )
            var attempts = 0
            while (attempts < 3 && exerciseSignature(updatedPlan) == avoid) {
                updatedPlan = workoutGenerator.generate(
                    WorkoutGeneratorInputs(
                        goal = current.goal,
                        timeMinutes = current.timeMinutes,
                        equipment = current.equipment,
                        focus = current.focus,
                        style = current.style
                    ),
                    avoidExerciseNames = avoid
                )
                attempts++
            }
            val saved = workoutPlanRepository.updateDraft(planId, updatedPlan, isSaved = _uiState.value.isSaved)
            _uiState.update { it.copy(plan = saved, isRegenerating = false) }
        }
    }

    fun savePlan() {
        viewModelScope.launch {
            workoutPlanRepository.markSaved(planId)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    private fun exerciseSignature(plan: GeneratedWorkoutPlan): Set<String> {
        return plan.blocks.flatMap { block -> block.exercises.map { it.name } }.toSet()
    }
}

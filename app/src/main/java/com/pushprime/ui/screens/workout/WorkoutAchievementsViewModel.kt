package com.pushprime.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AchievementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutAchievementsViewModel @Inject constructor(
    private val achievementsRepository: AchievementsRepository
) : ViewModel() {
    fun onSessionSaved() {
        viewModelScope.launch {
            achievementsRepository.recalcAchievements(emitPopup = true)
        }
    }
}

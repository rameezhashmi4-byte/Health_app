package com.pushprime.ui.screens.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AchievementSummary
import com.pushprime.data.AchievementsRepository
import com.pushprime.data.SessionDao
import com.pushprime.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val achievementsRepository: AchievementsRepository,
    private val sessionDao: SessionDao
) : ViewModel() {
    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _summary = MutableStateFlow(
        AchievementSummary(
            totalUnlocked = 0,
            totalBadges = 0,
            currentStreak = 0,
            sessionsCompleted = 0
        )
    )
    val summary: StateFlow<AchievementSummary> = _summary.asStateFlow()

    init {
        viewModelScope.launch {
            achievementsRepository.observeAchievements().collect { list ->
                _achievements.value = list
                _summary.update { current ->
                    current.copy(
                        totalUnlocked = list.count { it.unlocked },
                        totalBadges = list.size
                    )
                }
            }
        }

        viewModelScope.launch {
            sessionDao.getAllSessions().collect {
                achievementsRepository.recalcAchievements(emitPopup = false)
                val fresh = achievementsRepository.getSummary()
                _summary.value = fresh
            }
        }
    }
}

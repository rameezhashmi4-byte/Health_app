package com.pushprime.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AchievementsRepository
import com.pushprime.model.Achievement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementPopupViewModel @Inject constructor(
    private val achievementsRepository: AchievementsRepository
) : ViewModel() {
    private val _popup = MutableStateFlow<Achievement?>(null)
    val popup: StateFlow<Achievement?> = _popup.asStateFlow()

    init {
        viewModelScope.launch {
            achievementsRepository.popupEvents.collect { achievement ->
                if (_popup.value != null) return@collect
                _popup.value = achievement
                delay(2000)
                _popup.value = null
            }
        }
    }
}

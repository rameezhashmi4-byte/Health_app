package com.pushprime.ui.screens.pullup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.PullupSessionDao
import com.pushprime.model.PullupSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PullupLogSessionViewModel @Inject constructor(
    private val pullupSessionDao: PullupSessionDao
) : ViewModel() {

    fun saveSession(
        repsBySet: List<Int>,
        addedWeightKg: Double?,
        restSeconds: Int?,
        notes: String?
    ) {
        val totalReps = repsBySet.sum()
        val volumeScore = calculateVolumeScore(totalReps, addedWeightKg)
        viewModelScope.launch {
            pullupSessionDao.insert(
                PullupSession(
                    repsBySet = repsBySet,
                    totalReps = totalReps,
                    addedWeightKg = addedWeightKg,
                    restSeconds = restSeconds,
                    notes = notes?.takeIf { it.isNotBlank() },
                    volumeScore = volumeScore
                )
            )
        }
    }

    private fun calculateVolumeScore(totalReps: Int, addedWeightKg: Double?): Int {
        val weightFactor = ((addedWeightKg ?: 0.0) / 20.0).coerceAtLeast(0.0)
        return (totalReps * (1.0 + weightFactor)).roundToInt()
    }
}

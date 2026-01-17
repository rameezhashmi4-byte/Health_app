package com.pushprime.ui.screens.pullup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.PullupMaxTestDao
import com.pushprime.model.PullupMaxTest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PullupMaxTestViewModel @Inject constructor(
    private val pullupMaxTestDao: PullupMaxTestDao
) : ViewModel() {
    private val _isNewPr = MutableStateFlow(false)
    val isNewPr: StateFlow<Boolean> = _isNewPr.asStateFlow()

    fun saveMaxTest(maxReps: Int, formRating: Int?) {
        viewModelScope.launch {
            val previousMax = pullupMaxTestDao.getAllTestsOnce()
                .maxOfOrNull { it.maxReps } ?: 0
            _isNewPr.value = maxReps > previousMax
            pullupMaxTestDao.insert(
                PullupMaxTest(
                    maxReps = maxReps,
                    formRating = formRating
                )
            )
        }
    }
}

package com.pushprime.ui.screens.progress_photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.ProgressPhotoRepository
import com.pushprime.model.ProgressCollageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressCollageDetailViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository
) : ViewModel() {
    private val _collage = MutableStateFlow<ProgressCollageEntity?>(null)
    val collage: StateFlow<ProgressCollageEntity?> = _collage.asStateFlow()

    fun load(collageId: String) {
        viewModelScope.launch {
            _collage.value = repository.getCollageById(collageId)
        }
    }
}

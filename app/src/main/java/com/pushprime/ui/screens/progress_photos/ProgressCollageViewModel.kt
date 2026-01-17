package com.pushprime.ui.screens.progress_photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AuthRepository
import com.pushprime.data.ProgressPhotoRepository
import com.pushprime.model.ProgressCollageEntity
import com.pushprime.model.ProgressPhotoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressCollageViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository,
    authRepository: AuthRepository
) : ViewModel() {
    private val uid: String = authRepository.currentUser?.uid ?: "anonymous"

    val photos: StateFlow<List<ProgressPhotoEntity>> = repository.observePhotos(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    suspend fun createCollage(selected: List<ProgressPhotoEntity>): Result<ProgressCollageEntity> {
        _isSaving.value = true
        val result = repository.createCollage(uid, selected)
        if (result.isSuccess) {
            val collage = result.getOrNull()
            if (collage != null) {
                viewModelScope.launch {
                    val success = repository.uploadCollage(collage)
                    if (!success) {
                        repository.enqueueRetryUploads()
                    }
                }
            }
        }
        _isSaving.value = false
        return result
    }
}

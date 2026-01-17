package com.pushprime.ui.screens.progress_photos

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AuthRepository
import com.pushprime.data.ProgressPhotoRepository
import com.pushprime.model.PoseTag
import com.pushprime.model.ProgressPhotoEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProgressPhotoViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository,
    authRepository: AuthRepository
) : ViewModel() {
    private val uid: String = authRepository.currentUser?.uid ?: "anonymous"

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    suspend fun savePhoto(
        sourceUri: Uri,
        poseTag: PoseTag,
        notes: String?,
        takenAt: Long
    ): Result<ProgressPhotoEntity> {
        _isSaving.value = true
        val result = repository.savePhotoLocally(uid, sourceUri, poseTag, notes, takenAt)
        if (result.isSuccess) {
            val entity = result.getOrNull()
            if (entity != null) {
                viewModelScope.launch {
                    val success = repository.uploadPhoto(entity)
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

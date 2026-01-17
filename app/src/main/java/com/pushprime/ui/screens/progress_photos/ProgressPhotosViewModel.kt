package com.pushprime.ui.screens.progress_photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pushprime.data.AuthRepository
import com.pushprime.data.ProgressPhotoRepository
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
class ProgressPhotosViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository,
    authRepository: AuthRepository
) : ViewModel() {
    private val uid: String = authRepository.currentUser?.uid ?: "anonymous"

    val photos: StateFlow<List<ProgressPhotoEntity>> = repository.observePhotos(uid)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refreshFromRemote()
    }

    fun refreshFromRemote() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshFromRemote(uid)
            _isRefreshing.value = false
        }
    }

    fun retryUploads() {
        repository.enqueueRetryUploads()
    }

    fun deletePhoto(photo: ProgressPhotoEntity) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
        }
    }
}

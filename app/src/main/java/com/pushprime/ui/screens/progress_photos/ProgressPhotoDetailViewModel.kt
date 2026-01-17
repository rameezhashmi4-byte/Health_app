package com.pushprime.ui.screens.progress_photos

import androidx.lifecycle.ViewModel
import com.pushprime.data.ProgressPhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProgressPhotoDetailViewModel @Inject constructor(
    private val repository: ProgressPhotoRepository
) : ViewModel() {
    fun observePhoto(photoId: String) = repository.observePhotoById(photoId)
}

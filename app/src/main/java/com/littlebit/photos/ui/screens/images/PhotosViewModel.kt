package com.littlebit.photos.ui.screens.images

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableIntState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.repository.MediaRepository
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


class PhotosViewModel(
    private val mediaRepository: MediaRepository = MediaRepository()
) : ViewModel() {
    private val _photos = MutableStateFlow(mutableListOf<PhotoItem>())
    private val _photoGroups = MutableStateFlow(mutableListOf<ImageGroup>())
    val photoGroups = _photoGroups
    val photos = _photos

    private val customDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun loadMedia(context: Context) {
        try {
            viewModelScope.launch {
                val media = withContext(customDispatcher) {
                    mediaRepository.getImagesGroupedByDate(context.contentResolver)
                }
                _photoGroups.value = media.first
                photos.value = media.second
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        customDispatcher.close()
    }

    fun deletePhoto(listGroupIndex: Int, imageIndex: MutableIntState) {
        try {
            _photoGroups.value[listGroupIndex].images.removeAt(imageIndex.intValue)
            if (_photoGroups.value[listGroupIndex].images.isEmpty()) {
                _photoGroups.value.removeAt(listGroupIndex)
                imageIndex.intValue = 0
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh(context: Context) = loadMedia(context)

}




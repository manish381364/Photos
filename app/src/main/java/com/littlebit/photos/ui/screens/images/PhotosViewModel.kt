package com.littlebit.photos.ui.screens.images

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
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
    val isLoading = MutableStateFlow(false)
    val photoGroups = _photoGroups
    val photos = _photos

    private val customDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val addImageGroupDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private fun loadMedia(context: Context) {
        Log.d("PHOTOS_VIEW_MODEL", "addPhotosGroupedByDate: Inside LOAD_MEDIA")
        try {
            viewModelScope.launch(Dispatchers.Default) {
                val media = withContext(customDispatcher) {
                    mediaRepository.getImagesGroupedByDate(context.contentResolver, isLoading)
                }
                _photoGroups.value = media.first
                photos.value = media.second
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun addPhotosGroupedByDate(context: Context){
        Log.d("PHOTOS_VIEW_MODEL", "addPhotosGroupedByDate: Inside ADD_PHOTOS")
        viewModelScope.launch(Dispatchers.Default){
            withContext(addImageGroupDispatcher){
                mediaRepository.addImagesGroupedByDate(context.contentResolver, _photoGroups, _photos, isLoading)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        customDispatcher.close()
        addImageGroupDispatcher.close()
    }


    fun deletePhoto(listGroupIndex: Int, imageIndex: MutableIntState) {
        try {
            if(listGroupIndex < _photoGroups.value.size){
                if(imageIndex.intValue < _photoGroups.value[listGroupIndex].images.size){
                    _photoGroups.value.removeAt(listGroupIndex)
                    if(_photoGroups.value[listGroupIndex].images.size == 0){
                        _photoGroups.value.removeAt(listGroupIndex)
                    }
                }
                if(imageIndex.intValue < _photos.value.size){
                    _photos.value.removeAt(imageIndex.intValue)
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun refresh(context: Context) = loadMedia(context)

}




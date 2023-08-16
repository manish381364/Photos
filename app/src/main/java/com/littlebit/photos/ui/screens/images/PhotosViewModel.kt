package com.littlebit.photos.ui.screens.images

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant


class PhotosViewModel : ViewModel() {
    private val _photoGroups = MutableStateFlow(mutableListOf<ImageGroup>())
    private val _mediaList = MutableLiveData<List<ImageGroup>>()
    val mediaList: LiveData<List<ImageGroup>> get() = _mediaList
    val photoGroups = _photoGroups
    private val mediaRepository = MediaRepository()
    fun loadMedia(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val media = mediaRepository.getImagesGroupedByDate(context.contentResolver)
            _mediaList.postValue(media)
            _photoGroups.value = media.toMutableList()
        }
    }

    fun deletePhoto(listGroupIndex: Int, imageIndex: Int) {
        try {
            _photoGroups.value[listGroupIndex].images.removeAt(imageIndex)
        }
        catch (exception : Exception) {
            throw exception
        }
    }

    // Refreshable list
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _currentTime = MutableStateFlow(Instant.now())

    @RequiresApi(Build.VERSION_CODES.O)
    val currentTime = _currentTime.asStateFlow()

    private val _items = MutableStateFlow(mediaList.value)
    val items = _items.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh(context: Context) = viewModelScope.launch {
        _isRefreshing.update { true }
        // Simulate API call
        delay(2000)
        _currentTime.value = Instant.now()
        loadMedia(context)
        _items.value = mediaList.value
        _isRefreshing.update { false }
    }

//    fun delete(context: Context, uri: Uri?) {
//        try {
//            context.contentResolver.delete(uri!!, null, null)
//            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
//        } catch (exception: SecurityException) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                if (exception is RecoverableSecurityException) {
//                    val intent = exception.userAction
//                        .actionIntent
//                        .intentSender
//                    val activity = context as Activity
//                    startIntentSenderForResult(activity, intent, , null, 0, 0, 0, null)
//                    Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
//                    return
//                }
//            }
//            Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
//            throw exception
//        }
//    }

}




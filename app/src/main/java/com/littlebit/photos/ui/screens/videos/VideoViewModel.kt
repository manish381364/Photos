package com.littlebit.photos.ui.screens.videos

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.model.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


class VideoViewModel(
    private val repository: MediaRepository = MediaRepository()
) : ViewModel() {
    val videos = MutableStateFlow(mutableListOf<VideoItem>())
    val videoGroups = MutableStateFlow(mutableListOf<VideoGroup>())
    val isLoading = MutableStateFlow(false)


    private val addVideoDispatcher: ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val loadVideoDispatcher: ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()


    fun addVideos(context: Context) {
        viewModelScope.launch(Dispatchers.Default)  {
            try {
                withContext(addVideoDispatcher) {
                    repository.addVideoGroups(context, videoGroups, isLoading)
                }
            } catch (e: Exception) {
                // Handle exceptions here
                e.printStackTrace()
            }
        }
    }

    private fun loadVideos(context: Context) {
        viewModelScope.launch(Dispatchers.Default)  {
            try {
                withContext(loadVideoDispatcher) {
                    val result = repository.loadVideos(context, isLoading)
                    videos.value = result.first
                    videoGroups.value = result.second
                }
            } catch (e: Exception) {
                // Handle exceptions here
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Close the custom dispatcher to release resources
        addVideoDispatcher.close()
    }



    fun refreshVideos(context: Context) {
        loadVideos(context)
    }
}


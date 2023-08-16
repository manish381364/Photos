package com.littlebit.photos.ui.screens.videos

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.model.repository.MediaRepository
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors


class VideoViewModel(
    private val repository: MediaRepository = MediaRepository()
) : ViewModel() {
    val videos = MutableStateFlow<List<VideoItem>>(listOf())
    val videoGroups = MutableStateFlow<List<VideoGroup>>(listOf())


    private val customDispatcher: ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadVideos(context: Context) {
        viewModelScope.launch {
            try {
                val result = withContext(customDispatcher) {
                    repository.fetchVideoGroups(context)
                }
                videos.value = result.first
                videoGroups.value = result.second
            } catch (e: Exception) {
                // Handle exceptions here
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Close the custom dispatcher to release resources
        customDispatcher.close()
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun refreshVideos(context: Context) {
        loadVideos(context)
    }

}


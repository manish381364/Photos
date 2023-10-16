package com.littlebit.photos.ui.screens.videos

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.format.Formatter.formatFileSize
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.model.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import javax.inject.Inject


@HiltViewModel
class VideoViewModel @Inject constructor(
    private val repository: MediaRepository
) : ViewModel() {
    private val selectedVideoList = MutableStateFlow(hashMapOf<Long, Pair<Int, Int>>())
    val videos = MutableStateFlow(mutableListOf<VideoItem>())
    val videoGroups = MutableStateFlow(mutableListOf<VideoGroup>())
    val isLoading = MutableStateFlow(true)
    val selectedVideos = MutableStateFlow(0)


    private val addVideoDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val loadVideoDispatcher: ExecutorCoroutineDispatcher =
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()


    fun addVideosGroupedByDate(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
            withContext(addVideoDispatcher) {
                repository.addVideosGroupedByDate(
                    videoGroups,
                    videos,
                    isLoading,
                    context
                )
            }
        }
    }

    private fun loadVideos(context: Context) {
        viewModelScope.launch(Dispatchers.Default) {
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
        loadVideoDispatcher.close()
    }


    fun refreshVideos(context: Context) {
        loadVideos(context)
    }


    fun selectVideo(videoId: Long, listIndex: Int, videoIndex: Int) {
        if (selectedVideoList.value.containsKey(videoId)) {
            selectedVideoList.value.remove(videoId)
            videoGroups.value[listIndex].videos[videoIndex].isSelected = false
            selectedVideos.value--
        } else {
            selectedVideoList.value[videoId] = Pair(listIndex, videoIndex)
            videoGroups.value[listIndex].videos[videoIndex].isSelected = true
            selectedVideos.value++
        }
        Log.d(
            "LIST_VIDEO",
            "selectVideo: ${selectedVideoList.value.size} || ${selectedVideos.value}"
        )
    }

    fun selectAllVideos(videoGroup: VideoGroup, listIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            var markedAll = true
            // Use anyMatch to check if at least one item is not marked
            if (videoGroup.videos.any { !it.isSelected }) {
                markedAll = false
            }
            videoGroup.videos.forEachIndexed { index, videoItem ->
                if (markedAll) {
                    videoItem.isSelected = false
                    selectedVideoList.value.remove(videoItem.id)
                } else {
                    if (!videoItem.isSelected) {
                        videoItem.isSelected = true
                        selectedVideoList.value[videoItem.id] = Pair(listIndex, index)
                    }
                }
            }
            selectedVideos.value = selectedVideoList.value.size
            Log.d(
                "LIST_VIDEO",
                "selectAllVideos: ${selectedVideoList.value.size} || ${selectedVideos.value}"
            )
        }
    }

    fun unSelectAllVideos() {
        viewModelScope.launch(Dispatchers.Default) {
            selectedVideoList.value.forEach { (_, pair) ->
                val listIndex = pair.first
                val videoIndex = pair.second
                videoGroups.value[listIndex].videos[videoIndex].isSelected = false
            }
            selectedVideos.value = 0
            selectedVideoList.value.clear()

            Log.d(
                "LIST_VIDEO",
                "unSelectAllVideos: ${selectedVideoList.value.size} || ${selectedVideos.value}"
            )
        }
    }

    fun getSelectedMemorySize(context: Context): String {
        var totalSize = 0L
        selectedVideoList.value.forEach {
            val listIndex = it.value.first
            val videoIndex = it.value.second
            try {
                totalSize += videoGroups.value[listIndex].videos[videoIndex].size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return formatFileSize(context, totalSize)
    }

    fun shareSelectedVideos(): Intent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(
                Intent.EXTRA_STREAM,
                ArrayList(selectedVideoList.value.map { (_, pair) ->
                    val listIndex = pair.first
                    val videoIndex = pair.second
                    videoGroups.value[listIndex].videos[videoIndex].uri
                })
            )
            type = "video/*"
        }
        return Intent.createChooser(shareIntent, "Share Videos")
    }

    private fun getSelectedVideos(): List<Uri?> {

        val list = selectedVideoList.value.values.map { pair ->
            val listIndex = pair.first
            val videoIndex = pair.second
            var uri = Uri.EMPTY
            if (listIndex < videoGroups.value.size && videoIndex < videoGroups.value[listIndex].videos.size) {
                uri = videoGroups.value[listIndex].videos[videoIndex].uri
            }
            uri
        }
        return list
    }

    fun moveToTrashSelectedVideos(
        context: Context,
        trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    ) {
        val contentResolver = context.contentResolver
        viewModelScope.launch(Dispatchers.Default) {
            val selectedImages = getSelectedVideos()
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                selectedImages.forEach {
                    contentResolver.delete(it!!, null, null)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                selectedImages.forEach {
                    contentResolver.delete(it!!, null, null)
                }
            } else {
                val intentSender = MediaStore.createTrashRequest(
                    contentResolver,
                    selectedImages,
                    true
                ).intentSender
                trashLauncher.launch(
                    intentSender.let { IntentSenderRequest.Builder(it).build() }
                )
            }
        }
    }

    fun removeVideosFromList(context: Context, message: String = "Moved to trash") {
        viewModelScope.launch(Dispatchers.Default) {
            val listMap = mutableMapOf<Int, MutableList<Int>>()
            selectedVideoList.value.forEach { item ->
                val listIndex = item.value.first
                val videoIndex = item.value.second
                if (listMap.containsKey(listIndex)) {
                    listMap[listIndex]?.add(videoIndex)
                } else listMap[listIndex] = mutableListOf(videoIndex)
            }
            listMap.forEach { item ->
                val indicesToRemove = item.value
                val listIndex = item.key
                if (videoGroups.value.size > listIndex) {
                    val filteredList =
                        videoGroups.value[listIndex].videos.filterIndexed { index, _ -> index !in indicesToRemove }
                    videoGroups.value[listIndex].videos.clear()
                    videoGroups.value[listIndex].videos.addAll(filteredList)
                }
            }
            videoGroups.value =
                videoGroups.value.filter { item -> item.videos.size != 0 }.toMutableList()
            selectedVideoList.value.clear()
            selectedVideos.value = 0
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getData(applicationContext: Context) {
        // if permission is granted, load the images
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (videoGroups.value.isEmpty()) addVideosGroupedByDate(applicationContext)
                else loadVideos(applicationContext)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (videoGroups.value.isEmpty()) addVideosGroupedByDate(applicationContext)
                else loadVideos(applicationContext)
            }
        }
    }

    fun isSelectionInProgress(): Boolean {
        return selectedVideos.value > 0
    }

    fun deleteSelected(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val selectedVideos = getSelectedVideos()
                selectedVideos.forEach {
                    if (it != null) {
                        context.contentResolver.delete(it, null, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        removeVideosFromList(context, "Deleted")
    }
}


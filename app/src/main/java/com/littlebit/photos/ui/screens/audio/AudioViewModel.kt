package com.littlebit.photos.ui.screens.audio

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class AudioViewModel(
    private val repository: MediaRepository = MediaRepository()
) : ViewModel() {
    private val _audioList = MutableStateFlow(mutableListOf<Audio>())
    val audioList = _audioList
    private val customDispatcher: ExecutorCoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadAudio(context: Context) {
        viewModelScope.launch(Dispatchers.Default)  {
            try {
                val result = withContext(customDispatcher) {
                    repository.getAudioList(context)
                }
                _audioList.value = result
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

    fun setSelectedAudio(audioIndex: Int, isSelectionInProcess: MutableState<Boolean>) {
        _audioList.value[audioIndex].isSelected.value = !_audioList.value[audioIndex].isSelected.value
        isSelectionInProcess.value = _audioList.value.any { it.isSelected.value }
    }

    fun shareAudio(audioFile: Audio, context: Context) {
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_STREAM, audioFile.uri)
            type = "audio/*"
        }
        android.content.Intent.createChooser(shareIntent, "Share Audio").apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            context.startActivity(intent)
        }
    }

    fun openWith(audioFile: Audio, context: Context) {
        val openIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_VIEW
            putExtra(android.content.Intent.EXTRA_STREAM, audioFile.uri)
            type = "audio/*"
        }
        android.content.Intent.createChooser(openIntent, "Open With").apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            context.startActivity(intent)
        }
    }
}

data class Audio(
    val id: Long,
    val name: String,
    val path: String,
    val duration: String,
    val size: String,
    val isMusic: Boolean,
    val uri: Uri,
    val thumbNail: ImageBitmap? = null,
    val dateAdded: String = "",
    var isSelected: MutableState<Boolean> = mutableStateOf(false  )
)
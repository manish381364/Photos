package com.littlebit.photos.ui.screens.audio.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.model.player.service.AudioService
import com.littlebit.photos.model.player.service.AudioServiceHandler
import com.littlebit.photos.model.player.service.AudioState
import com.littlebit.photos.model.player.service.PlayerEvent
import com.littlebit.photos.model.repository.MediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@OptIn(SavedStateHandleSaveableApi::class)
class XAudioViewModel @Inject constructor(
    private val audioServiceHandler: AudioServiceHandler,
    private val repository: MediaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var duration by savedStateHandle.saveable { mutableLongStateOf(0L) }
    var progress by savedStateHandle.saveable { mutableFloatStateOf(0f) }
    var progressString = MutableStateFlow("--:--")
    var isPlaying by savedStateHandle.saveable { mutableStateOf(false) }
    var isListLooping by savedStateHandle.saveable { mutableStateOf(false) }
    var isShuffling by savedStateHandle.saveable { mutableStateOf(false) }
    var isCurrentRepeat by savedStateHandle.saveable { mutableStateOf(false) }
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(AudioItem()) }
    var isServiceRunning by savedStateHandle.saveable { mutableStateOf(false) }
    private var audioList by savedStateHandle.saveable { mutableStateOf(listOf<AudioItem>()) }
    private val _uiState = MutableStateFlow<UIState>(UIState.InitialState)


    init {
        loadAudioData()
    }

    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    AudioState.Initial -> _uiState.value = UIState.InitialState
                    is AudioState.Buffering -> calculateProgressValue(mediaState.progress)
                    is AudioState.Playing -> isPlaying = mediaState.isPlaying
                    is AudioState.Progress -> calculateProgressValue(mediaState.progress)
                    is AudioState.CurrentPlaying -> {
                        currentSelectedAudio = audioList[mediaState.mediaItemIndex]
                        Log.d("CURRENT_PLAYING", ": ${currentSelectedAudio.displayName}")
                    }

                    is AudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }

        }
    }


    fun onUiEvents(uiEvents: UIEvents) = viewModelScope.launch {
        when (uiEvents) {
            UIEvents.Backward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Backward)
            UIEvents.Forward -> audioServiceHandler.onPlayerEvents(PlayerEvent.Forward)
            UIEvents.SeekToNext -> audioServiceHandler.onPlayerEvents(PlayerEvent.SeekToNext)
            UIEvents.SeekToPrevious -> audioServiceHandler.onPlayerEvents((PlayerEvent.SeekToPrevious))
            is UIEvents.PlayPause -> audioServiceHandler.onPlayerEvents((PlayerEvent.PlayPause))
            is UIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position) / 100f).toLong()
                )
            }

            is UIEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.SelectedAudioChanged,
                    selectedAudioIndex = uiEvents.index
                )
            }

            is UIEvents.UpdateProgress -> {
                audioServiceHandler.onPlayerEvents(
                    PlayerEvent.UpdateProgress(
                        uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }

        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvents(PlayerEvent.Stop)
        }
        super.onCleared()
    }

    fun loadAudioData() {
        if (!isPlaying) {
            viewModelScope.launch {
                val audio = repository.getAudioList()
                audioList = audio
                setMediaItems()
            }
        }
    }

    private fun setMediaItems() {
        audioList.map { audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setDisplayTitle(audio.displayName)
                        .build()
                )
                .build()
        }.also {
            audioServiceHandler.setMediaItems(it)
        }
    }

    private fun calculateProgressValue(currentProgress: Long) {
        progress =
            if (currentProgress >= 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f) else 0f
        progressString.value = formatTime(currentProgress)
        Log.d("PROGRESS", "AudioScreen: $progress $currentProgress")
    }


    fun getDominantColor(audioThumbNail: ImageBitmap?): Color {
        // Use the LaunchedEffect to extract the dominant color when the image is loaded
        var dominantColor = Color.Transparent
        if (audioThumbNail != null) {
            val palette = Palette.Builder(audioThumbNail.asAndroidBitmap()).generate()
            val dominantSwatch = palette.dominantSwatch
            dominantColor = dominantSwatch?.rgb?.let {
                Color(
                    red = (it shr 16 and 0xFF) / 255.0f,
                    green = (it shr 8 and 0xFF) / 255.0f,
                    blue = (it and 0xFF) / 255.0f
                )
            } ?: Color.Transparent
        }

        return dominantColor
    }


    fun formatTime(progress: Long): String {
        val totalSeconds = progress / 1000 // Convert milliseconds to seconds
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }


    fun shareIntent(context: Context) {
        if (currentSelectedAudio.uri != Uri.EMPTY) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, currentSelectedAudio.uri)
                type = "audio/*"
            }
            Intent.createChooser(shareIntent, "Share Audio").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { intent ->
                context.startActivity(intent)
            }
        }
    }

    fun openWith(context: Context) {
        val openIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            putExtra(Intent.EXTRA_STREAM, currentSelectedAudio.uri)
            type = "audio/*"
        }
        Intent.createChooser(openIntent, "Open With").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            context.startActivity(intent)
        }
    }

    @UnstableApi
    fun startService(context: Context) {
        val intent = Intent(context, AudioService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        }
        else{
            context.startService(intent)
        }
    }


        fun repeat() {
            viewModelScope.launch {
                isCurrentRepeat = !isCurrentRepeat
                audioServiceHandler.onPlayerEvents(PlayerEvent.RepeatMode(isCurrentRepeat))
            }
        }

        fun shuffle() {
            viewModelScope.launch {
                isShuffling = !isShuffling
                audioServiceHandler.onPlayerEvents(PlayerEvent.ShuffleMode(isShuffling))
            }
        }
    }


    sealed class UIEvents {
        data object PlayPause : UIEvents()
        data object SeekToNext : UIEvents()
        data object SeekToPrevious : UIEvents()
        data object Backward : UIEvents()
        data object Forward : UIEvents()
        data class SelectedAudioChange(val index: Int) : UIEvents()
        data class SeekTo(val position: Float) : UIEvents()
        data class UpdateProgress(val newProgress: Float) : UIEvents()

    }

    sealed class UIState {
        data object InitialState : UIState()
        data object Ready : UIState()

    }
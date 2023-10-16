package com.littlebit.photos.ui.screens.audio.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.AudioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt

class PlayAudioViewModel : ViewModel() {
    private val mediaPlayer = MediaPlayer()
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)
    val isShuffling = MutableStateFlow(false)
    val isLooping = MutableStateFlow(false)
    val isListLooping = MutableStateFlow(false)
    val playbackState: StateFlow<PlaybackState> = _playbackState
    val playbackProgress = MutableStateFlow(0)
    private val currentUri = MutableStateFlow(Uri.EMPTY)


    fun play(uri: Uri, context: Context) {
        playbackProgress.value = 0
        currentUri.value = uri
        mediaPlayer.reset()
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            _playbackState.value = PlaybackState.PLAYING
            updatePlayBackProgress()
            if (!isListLooping.value && isLooping.value) mediaPlayer.isLooping = true
        }
        mediaPlayer.setOnCompletionListener {
            playbackProgress.value = 0
            _playbackState.value = PlaybackState.STOP
        }
    }


    private fun updatePlayBackProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            while (mediaPlayer.isPlaying) {
                playbackProgress.value = mediaPlayer.currentPosition
                delay(500)
            }
        }
    }


    fun pause() {
        mediaPlayer.pause()
        playbackProgress.value = mediaPlayer.currentPosition
        _playbackState.value = PlaybackState.PAUSED
        updatePlayBackProgress()
        Log.d("SEEK_TO", "pause: ${mediaPlayer.currentPosition}")
    }

    fun resume() {
        mediaPlayer.seekTo(playbackProgress.value)
        mediaPlayer.start()
        updatePlayBackProgress()
        _playbackState.value = PlaybackState.PLAYING
        Log.d("SEEK_TO", "resume: ${mediaPlayer.currentPosition}")
    }


    fun seekTo(toInt: Int) {
        mediaPlayer.seekTo(toInt)
        playbackProgress.value = toInt
        if (_playbackState.value == PlaybackState.STOP) {
            mediaPlayer.start()
            _playbackState.value = PlaybackState.PLAYING
        }
    }


    fun repeatCurrent() {
        if (!isListLooping.value && !isLooping.value) {
            isListLooping.value = true
        } else if (isListLooping.value) {
            isListLooping.value = false
            isLooping.value = true
            mediaPlayer.isLooping = true
        } else {
            mediaPlayer.isLooping = false
            isLooping.value = false
        }
    }

    fun shareIntent(context: Context) {
        if (currentUri.value != Uri.EMPTY) {
            val shareIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_STREAM, currentUri.value)
                type = "audio/*"
            }
            android.content.Intent.createChooser(shareIntent, "Share Audio").apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { intent ->
                context.startActivity(intent)
            }
        }
    }

    fun getDuration(): Int {
        return mediaPlayer.duration
    }


    fun getTimeDuration(): String {
        val duration = mediaPlayer.duration // Get the duration in milliseconds
        val seconds = duration / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val formattedDuration: String = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
        } else {
            String.format("%02d:%02d", minutes, seconds % 60)
        }
        return formattedDuration
    }

    fun getCurrentTimeDuration(): String {
        val duration =
            if (playbackState.value == PlaybackState.STOP) 0 else mediaPlayer.currentPosition // Get the duration in milliseconds
        val seconds = duration / 1000
        val minutes = seconds / 60
        val hours = minutes / 60

        val formattedDuration: String = if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
        } else {
            String.format("%02d:%02d", minutes, seconds % 60)
        }
        return formattedDuration

    }


    override fun onCleared() {
        super.onCleared()
        mediaPlayer.release()
    }

    fun setPlayBackState(state: PlaybackState) {
        _playbackState.value = state
    }

    private fun getRandomNumberInRangeExcluding(range: IntRange, excludedNumber: Int): Int {
        var randomNumber: Int
        do {
            randomNumber = Random.nextInt(range)
        } while (randomNumber == excludedNumber)

        return randomNumber
    }


    private fun getNextIndex(audioList: MutableList<AudioItem>, currentIndex: Int): Int =
        if (isShuffling.value) getRandomNumberInRangeExcluding(
            IntRange(0, audioList.size - 1),
            currentIndex
        ) else kotlin.math.abs(
            currentIndex + 1
        ) % audioList.size

    fun setStateAfterSeek() {
        if (mediaPlayer.isPlaying) _playbackState.value = PlaybackState.PLAYING
        else _playbackState.value = PlaybackState.PAUSED
    }

    fun playNext(
        audioList: MutableList<AudioItem>,
        currentIndex: MutableIntState,
        context: Context,
        skipNext: Boolean
    ) {
        viewModelScope.launch {
            _playbackState.value = PlaybackState.NEXT
            if (isListLooping.value) {
                currentIndex.intValue = getNextIndex(audioList, currentIndex.intValue)
                Log.d("INSIDE_LOOP", "playNext: LOOPING")
                play(audioList[currentIndex.intValue].uri, context)
            } else {
                if (skipNext || currentIndex.intValue < audioList.size - 1) {
                    currentIndex.intValue = getNextIndex(audioList, currentIndex.intValue)
                    play(audioList[currentIndex.intValue].uri, context)
                    Log.d("INSIDE_NOT_LOOP_PLAY", "playNext: LOOPING")
                } else {
                    playbackProgress.value = 0
                    _playbackState.value = PlaybackState.COMPLETED
                    mediaPlayer.pause()
                    updatePlayBackProgress()
                    Log.d("INSIDE_NOT_LOOP_STOP", "playNext: LOOPING")
                }
            }
        }
    }

    fun playPrevious(
        audioList: MutableList<AudioItem>,
        currentIndex: MutableIntState,
        context: Context
    ) {
        viewModelScope.launch {
            currentIndex.intValue = getPrevIndex(audioList, currentIndex.intValue)
            _playbackState.value = PlaybackState.PREV
            play(audioList[currentIndex.intValue].uri, context)
        }
    }

    private fun getPrevIndex(audioList: MutableList<AudioItem>, currentIndex: Int): Int =
        if (isShuffling.value) getRandomNumberInRangeExcluding(
            IntRange(0, audioList.size - 1),
            currentIndex
        ) else kotlin.math.abs(
            (currentIndex + audioList.size) - 1
        ) % audioList.size
}

sealed class PlaybackState {
    data object IDLE : PlaybackState()
    data object PLAYING : PlaybackState()
    data object PAUSED : PlaybackState()
    data object STOP : PlaybackState()
    data object SEEKING : PlaybackState()
    data object COMPLETED : PlaybackState()
    data object NEXT : PlaybackState()
    data object PREV : PlaybackState()
}





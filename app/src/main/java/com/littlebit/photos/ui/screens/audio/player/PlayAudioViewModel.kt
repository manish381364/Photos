package com.littlebit.photos.ui.screens.audio.player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayAudioViewModel : ViewModel() {
    private val mediaPlayer = MediaPlayer()
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)

    val isLooping = MutableStateFlow(false)
    val playbackState: StateFlow<PlaybackState> = _playbackState
    val playbackProgress = MutableStateFlow(0)
    private val currentUri = MutableStateFlow(Uri.EMPTY)


    @RequiresApi(Build.VERSION_CODES.O)
    fun play(uri: Uri, context: Context) {
        currentUri.value = uri
        mediaPlayer.reset()
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()
        mediaPlayer.start()
        _playbackState.value = PlaybackState.PLAYING
        mediaPlayer.setOnCompletionListener {
            _playbackState.value = PlaybackState.COMPLETED
            playbackProgress.value = 0
        }
    }


    fun updatePlayBackProgress() {
        viewModelScope.launch(Dispatchers.Default) {
            while (mediaPlayer.isPlaying) {
                playbackProgress.value = mediaPlayer.currentPosition
                delay(1000)
            }
        }
    }


    fun pause() {
        mediaPlayer.pause()
        playbackProgress.value = mediaPlayer.currentPosition
        _playbackState.value = PlaybackState.PAUSED
        Log.d("SEEK_TO", "pause: ${mediaPlayer.currentPosition}")
    }

    fun resume() {
        mediaPlayer.seekTo(playbackProgress.value)
        mediaPlayer.start()
        if(_playbackState.value == PlaybackState.COMPLETED){
            updatePlayBackProgress()
        }
        _playbackState.value = PlaybackState.PLAYING
        Log.d("SEEK_TO", "resume: ${mediaPlayer.currentPosition}")
    }



    fun seekTo(toInt: Int) {
        mediaPlayer.seekTo(toInt)
        playbackProgress.value = toInt
        if (_playbackState.value == PlaybackState.COMPLETED) {
            mediaPlayer.start()
            _playbackState.value = PlaybackState.PLAYING
        }
    }


    fun repeatCurrent() {
        if (mediaPlayer.isLooping) {
            mediaPlayer.isLooping = false
            isLooping.value = false
        } else {
            mediaPlayer.isLooping = true
            isLooping.value = true
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
            if (playbackState.value == PlaybackState.COMPLETED) 0 else mediaPlayer.currentPosition // Get the duration in milliseconds
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

    fun getPlayBackState(): PlaybackState {
        return  _playbackState.value
    }

}

sealed class PlaybackState {
    data object IDLE : PlaybackState()
    data object PLAYING : PlaybackState()
    data object PAUSED : PlaybackState()
    data object COMPLETED : PlaybackState()
}





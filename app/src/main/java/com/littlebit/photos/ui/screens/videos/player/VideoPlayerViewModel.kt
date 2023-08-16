package com.littlebit.photos.ui.screens.videos.player

import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@UnstableApi
class PlaybackPositionController(private val clock: Clock = Clock.DEFAULT) {
    private val playbackPositionKey = "playback_position"
    private val playbackSpeedKey = "playback_speed"
    private val currentMediaUriKey = "current_media_uri"

    fun savePlaybackPosition(player: ExoPlayer, context: Context, currentMediaItem: MediaItem?) {
        val playbackPosition = player.currentPosition
        val playbackSpeed = player.playbackParameters.speed
        val currentMediaUri = currentMediaItem?.playbackProperties?.uri?.toString()
        val lastPlaybackUpdateTime = clock.elapsedRealtime()

        context.getSharedPreferences("exoplayer_shared_prefs", Context.MODE_PRIVATE).edit {
            putLong(playbackPositionKey, playbackPosition)
            putFloat(playbackSpeedKey, playbackSpeed)
            putString(currentMediaUriKey, currentMediaUri)
            putLong("last_playback_update_time", lastPlaybackUpdateTime)
            apply()
        }
    }

    fun restorePlaybackPosition(player: ExoPlayer, context: Context, mediaItems: List<MediaItem?>) {
        val sharedPreferences =
            context.getSharedPreferences("exoplayer_shared_prefs", Context.MODE_PRIVATE)
        val playbackPosition = sharedPreferences.getLong(playbackPositionKey, 0)
        val playbackSpeed = sharedPreferences.getFloat(playbackSpeedKey, 1.0f)
        val currentMediaUri = sharedPreferences.getString(currentMediaUriKey, null)
        val lastPlaybackUpdateTime = sharedPreferences.getLong("last_playback_update_time", 0)

        val currentMediaIndex =
            mediaItems.indexOfFirst { it?.playbackProperties?.uri?.toString() == currentMediaUri }

        if (currentMediaIndex != -1) {
            val playbackParameters = PlaybackParameters(playbackSpeed)
            player.playbackParameters = playbackParameters
            player.setMediaItem(mediaItems[currentMediaIndex]!!)
            player.seekTo(playbackPosition)
            player.prepare()
            player.play()
        }
    }
}


class VideoPlayerViewModel : ViewModel() {
    var xPlayer: ExoPlayer? = null
    val playBackProgress = MutableStateFlow(0L)
    val xPlayerState = MutableStateFlow(PlayerState.IDLE)
    val duration = MutableStateFlow(0L)
    fun initialiseXPlayer(context: Context) {
        xPlayer = ExoPlayer.Builder(context).build()
    }

    fun setMedia(mediaItems: List<MediaItem>, startIndex: Int) {
        xPlayer?.setMediaItems(mediaItems, startIndex, 0)
        xPlayer?.prepare()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun play(){
        duration.value = xPlayer?.duration!!
        xPlayer?.play()
        xPlayerState.value = PlayerState.PLAYING
    }

    fun pause(){
        xPlayer?.pause()
        xPlayerState.value = PlayerState.PAUSED
    }

    fun resume(){
        play()
    }

    fun stop(){
        xPlayer?.stop()
        xPlayerState.value = PlayerState.STOPPED
    }


    private fun updateProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            while (xPlayer?.isPlaying == true){
                playBackProgress.value = xPlayer?.currentPosition!!
                delay(1000)
            }
        }
    }
}

enum class PlayerState {
    IDLE,
    PLAYING,
    PAUSED,
    STOPPED,
    COMPLETED
}





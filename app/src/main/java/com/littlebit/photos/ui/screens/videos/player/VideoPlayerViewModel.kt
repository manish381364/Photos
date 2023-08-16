package com.littlebit.photos.ui.screens.videos.player

import android.content.Context
import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModel
import androidx.core.content.edit
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.Clock
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@UnstableApi class PlaybackPositionController(private val clock: Clock = Clock.DEFAULT) {
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
        val sharedPreferences = context.getSharedPreferences("exoplayer_shared_prefs", Context.MODE_PRIVATE)
        val playbackPosition = sharedPreferences.getLong(playbackPositionKey, 0)
        val playbackSpeed = sharedPreferences.getFloat(playbackSpeedKey, 1.0f)
        val currentMediaUri = sharedPreferences.getString(currentMediaUriKey, null)
        val lastPlaybackUpdateTime = sharedPreferences.getLong("last_playback_update_time", 0)

        val currentMediaIndex = mediaItems.indexOfFirst { it?.playbackProperties?.uri?.toString() == currentMediaUri }

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


class CustomControlVisibilityObserver(
    private val playerView: PlayerView,
    private val onControlsVisibilityChanged: (Boolean) -> Unit
) : ViewTreeObserver.OnGlobalLayoutListener {

    private val rect = Rect()

    init {
        playerView.viewTreeObserver.addOnGlobalLayoutListener(this)
    }
    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onGlobalLayout() {
        playerView.getGlobalVisibleRect(rect)
        val controlsVisible = rect.bottom == playerView.height
        onControlsVisibilityChanged(controlsVisible)
    }

    fun removeObserver() {
        playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
}




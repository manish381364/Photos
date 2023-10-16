package com.littlebit.photos.ui.screens.audio.player

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

// Implement a MediaSessionService
class PlaybackService(
    private var player: ExoPlayer? = null,
    private var mediaSession: MediaSession? = null,
    private var callback: MediaSession.Callback = MyCallback()
) : MediaSessionService() {
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        val context = this
        // Create a MediaSession
        player?.apply {
            mediaSession = MediaSession.Builder(context, this).setCallback(callback).build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

}

private class MyCallback : MediaSession.Callback {
    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        val connectionResult = super.onConnect(session, controller)
        val sessionCommands =
            connectionResult.availableSessionCommands
                .buildUpon()
                .build()
        return MediaSession.ConnectionResult.accept(
            sessionCommands, connectionResult.availablePlayerCommands
        )
    }

    override fun onPostConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ) {
        // Display a button for the custom command
        val favoriteButton = CommandButton.Builder()
            .setDisplayName("Save to favorites")
            .build()
        session.setCustomLayout(listOf(favoriteButton))

        super.onPostConnect(session, controller)
    }
}
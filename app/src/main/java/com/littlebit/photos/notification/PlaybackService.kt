package com.littlebit.photos.notification

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

// Implement a MediaSessionService
class PlaybackService(
    private var player: ExoPlayer? = null,
    private var mediaSession: MediaSession? = null,
) : MediaSessionService() {

    private var callback = MyCallback()
    override fun onCreate() {
        super.onCreate()
        if(player == null)
            player = ExoPlayer.Builder(this).build()
        // Create a MediaSession
        if(mediaSession == null)
            mediaSession = MediaSession.Builder(this, player!!).setCallback(callback).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    private inner class MyCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val sessionCommands =
                connectionResult.availableSessionCommands
                    .buildUpon()
                    // Add custom commands
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
}
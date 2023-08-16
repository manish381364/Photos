package com.littlebit.photos.ui.screens.videos.player

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.os.PowerManager
import android.view.ViewGroup
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.grid.isLandscape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun VideoScreen(
    navHostController: NavHostController,
    videoViewModel: VideoViewModel,
    videoIndex: Int,
    listIndex: Int
) {
    val videoList = videoViewModel.videoGroups.collectAsState().value
    val videoUriList = videoList[listIndex].videos.map { videoItem ->
        videoItem.uri
    }
    val systemUiController = rememberSystemUiController()
    systemUiController.isSystemBarsVisible = false
    VideoPlayer(
        uriList = videoUriList,
        startIndex = videoIndex,
        navHostController = navHostController
    )
}


@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(uriList: List<Uri?>, startIndex: Int, navHostController: NavHostController) {
    val currentUriIndex = rememberUpdatedState(startIndex)
    val isLandsScape = isLandscape()
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val paddingValues by remember {
        mutableStateOf(PaddingValues(bottom = if (isLandsScape) 0.dp else 10.dp))
    }
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    val playerView = remember {
        PlayerView(context)
    }
    playerView.player = player

    val playbackPositionController = remember {
        PlaybackPositionController()
    }


    val mediaItems = uriList.map {
        val currentItem = MediaItem.fromUri(it!!)
        currentItem
    }.toMutableList()
    player.setMediaItems(mediaItems, currentUriIndex.value, 0)
    player.prepare()
    player.play()

    val powerService = context.getSystemService(Context.POWER_SERVICE)
    val p = powerService as PowerManager
    val wakeLock = p.newWakeLock(
        PowerManager.SCREEN_DIM_WAKE_LOCK,
        "VideoPlayer::WakeLock"
    )


    // AndroidView for displaying the video player

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(paddingValues)
            .focusable()
            .onKeyEvent {
                playerView.dispatchKeyEvent(it.nativeKeyEvent)
            },
        factory = { playerView }
    )


    // Restore playback position if available
    LaunchedEffect(player) {
        playbackPositionController.restorePlaybackPosition(player, context, mediaItems)
        val mediaDuration = player.duration
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    if (player.playWhenReady) {
                        // Player is not paused (playing)
                        systemUiController.isStatusBarVisible = false
                        wakeLock.acquire(mediaDuration)
                    } else {
                        // Player is paused
                        systemUiController.isStatusBarVisible = true
                        wakeLock.release()
                    }
                } else {
                    systemUiController.isStatusBarVisible = true
                }
            }
        })
    }

    // Save playback position when the composable is disposed
    DisposableEffect(player) {
        onDispose {
            playbackPositionController.savePlaybackPosition(
                player,
                context,
                player.currentMediaItem
            )
            player.release()
            systemUiController.isStatusBarVisible = true
        }
    }

    // Handle back press

    BackHandler {
        playerView.player = null
        player.release()
        coroutineScope.launch {
            delay(300)
        }
        navHostController.popBackStack()
    }
}


@Composable
fun VideoPlayer2(uriList: List<Uri?> = emptyList(), startIndex: Int) {
    var currentUriIndex by remember { mutableIntStateOf(startIndex) }
    var videoState by remember { mutableStateOf(VideoState.Stopped) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box {
        AndroidView(
            factory = { context ->
                val videoView = VideoView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        if (isLandscape) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setOnCompletionListener {
                        // Automatically play the next video when the current video finishes
                        currentUriIndex = (currentUriIndex + 1) % uriList.size
                        setVideoURI(uriList[currentUriIndex])
                        start()
                    }
                }
                videoView
            },
            update = { videoView ->
                if (uriList.isNotEmpty()) {
                    if (videoState == VideoState.Playing) {
                        videoView.start()
                    } else {
                        videoView.pause()
                    }
                    videoView.setVideoURI(uriList[currentUriIndex])
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            IconButton(
                onClick = {
                    currentUriIndex = (currentUriIndex - 1 + uriList.size) % uriList.size
                    videoState = VideoState.Playing
                }
            ) {
                Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    videoState = when (videoState) {
                        VideoState.Playing -> VideoState.Paused
                        VideoState.Paused -> VideoState.Playing
                        VideoState.Stopped -> VideoState.Playing
                    }
                }
            ) {
                Icon(
                    imageVector = when (videoState) {
                        VideoState.Playing -> Icons.Default.Pause
                        VideoState.Paused -> Icons.Default.PlayArrow
                        VideoState.Stopped -> Icons.Filled.Stop
                    },
                    contentDescription = when (videoState) {
                        VideoState.Playing -> "Pause"
                        VideoState.Paused -> "Play"
                        VideoState.Stopped -> "Stop"
                    }
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = {
                    currentUriIndex = (currentUriIndex + 1) % uriList.size
                    videoState = VideoState.Playing
                }
            ) {
                Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }
}
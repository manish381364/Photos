package com.littlebit.photos.ui.screens.videos.player

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        VideoPlayer(
            uriList = videoUriList, startIndex = videoIndex, navHostController = navHostController, playerViewModel = viewModel()
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(
    uriList: List<Uri?>,
    startIndex: Int,
    navHostController: NavHostController,
    playerViewModel: VideoPlayerViewModel
) {
    val currentUriIndex = rememberUpdatedState(startIndex)
    val isPlaying = rememberUpdatedState(playerViewModel.isPlaying)
    val playbackPosition = rememberUpdatedState(playerViewModel.playbackPosition)
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    val playerView = remember {
        PlayerView(context)
    }
    playerView.player = player



    val mediaItems = uriList.map {
        val currentItem = MediaItem.fromUri(it!!)
        currentItem
    }.toMutableList()
    player.setMediaItems(mediaItems, currentUriIndex.value, 0)
    player.prepare()
    player.seekTo(playbackPosition.value)

    // Start or pause playback based on the stored state
   if(!isPlaying.value){
       player.play()
   }



    // AndroidView for displaying the video player
    AndroidView(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .focusable()
        .onKeyEvent {
            playerView.dispatchKeyEvent(it.nativeKeyEvent)
        },
        factory = {
            playerView.apply {
                setControllerVisibilityListener(PlayerView.ControllerVisibilityListener {
                    systemUiController.isSystemBarsVisible = it == 0
                })
                setShowRewindButton(false)
                setShowFastForwardButton(false)
                setShowSubtitleButton(true)
            }
        }
    )


    DisposableEffect(player) {
        onDispose {
            player.release()
            systemUiController.isStatusBarVisible = true
            playerViewModel.currentUriIndex = currentUriIndex.value
            playerViewModel.isPlaying = player.isPlaying
            playerViewModel.playbackPosition = player.currentPosition
            coroutineScope.launch {
                delay(300)
                navHostController.popBackStack()
                systemUiController.isStatusBarVisible = true
            }
        }
    }

    // Handle back press
}



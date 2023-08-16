package com.littlebit.photos.ui.screens.videos.player

import android.content.Context
import android.net.Uri
import android.os.PowerManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        VideoPlayer(
            uriList = videoUriList,
            startIndex = videoIndex,
            navHostController = navHostController
        )
    }
}


@Composable
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun VideoPlayer(
    uriList: List<Uri?>,
    startIndex: Int,
    navHostController: NavHostController,
    playerViewModel: VideoPlayerViewModel = viewModel()
) {
    val progress = playerViewModel.playBackProgress.collectAsState()
    val isLandsScape = isLandscape()
    val systemUiController = rememberSystemUiController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val paddingValues by remember {
        mutableStateOf(PaddingValues(bottom = if (isLandsScape) 0.dp else 10.dp))
    }
    playerViewModel.initialiseXPlayer(context)
    val player = playerViewModel.xPlayer
    val playerView = remember {
        PlayerView(context)
    }
    playerView.player = player
    val mediaItems = uriList.map {
        val currentItem = MediaItem.fromUri(it!!)
        currentItem
    }.toMutableList()
    playerViewModel.setMedia(mediaItems, startIndex)
    playerViewModel.play()

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
            .padding(paddingValues)
            .background(Color.Black)
            .focusable()
            .onKeyEvent {
                playerView.dispatchKeyEvent(it.nativeKeyEvent)
            },
        factory = { playerView }
    )


    LaunchedEffect(progress){
        player?.seekTo(progress.value)
    }


    // Save playback position when the composable is disposed
    DisposableEffect(player) {
        onDispose {
            player?.release()
            systemUiController.isStatusBarVisible = true
        }
    }

    // Handle back press

    BackHandler {
        playerView.player = null
        player?.release()
        coroutineScope.launch {
            delay(300)
        }
        navHostController.popBackStack()
    }
}



package com.littlebit.photos.ui.screens.audio.player

import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController


@RequiresApi(34)
@Composable
@androidx.media3.common.util.UnstableApi
fun AudioScreen(
    xAudioViewModel: XAudioViewModel,
    navController: NavHostController,
) {
    val audioThumbNail = xAudioViewModel.currentSelectedAudio.thumbNail
    val progressString = xAudioViewModel.progressString.collectAsStateWithLifecycle().value
    val dominantColor =
        if (audioThumbNail != null) xAudioViewModel.getDominantColor(audioThumbNail) else {
            Color.Magenta.copy(0.7f)
        }
    val backGroundColor = listOf(
        Color.Black.copy(0.9f),
        dominantColor,
        Color.Black.copy(0.9f)
    )
    Surface {
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(backGroundColor))
        ) {
            Column(
                Modifier
                    .padding(WindowInsets.systemBars.asPaddingValues()),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                XPlayerTopBar(
                    xAudioViewModel = xAudioViewModel,
                    navController = navController,
                )
                AudioThumbNail(audioThumbNail)
                MarqueeText(text = xAudioViewModel.currentSelectedAudio.displayName)
                ProgressBar(
                    xAudioViewModel.progress,
                    xAudioViewModel.formatTime(xAudioViewModel.currentSelectedAudio.durationLong),
                    progressString
                ) { xAudioViewModel.onUiEvents(UIEvents.SeekTo(it)) }
                ControlButtons(xAudioViewModel)
            }
        }
    }
}




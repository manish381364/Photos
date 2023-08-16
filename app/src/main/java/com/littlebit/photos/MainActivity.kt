package com.littlebit.photos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.rememberNavController
import com.littlebit.photos.ui.navigation.NavigationGraph
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.audio.player.PlayAudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.theme.PhotosTheme

@RequiresApi(34)
class MainActivity : ComponentActivity() {
    private val photosViewModel: PhotosViewModel by viewModels()
    private val videoViewModel: VideoViewModel by viewModels()
    private val audioViewModel: AudioViewModel by viewModels()
    private val playAudioViewModel = PlayAudioViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val context = LocalContext.current
            val lifecycle = LocalLifecycleOwner.current.lifecycle
            PhotosApp(photosViewModel, videoViewModel, audioViewModel, playAudioViewModel)
            LaunchedEffect(true) {
                photosViewModel.loadMedia(context)
                videoViewModel.refreshVideos(context)
                audioViewModel.loadAudio(context)
            }
            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        photosViewModel.refresh(context)
                        videoViewModel.refreshVideos(context)
                        audioViewModel.loadAudio(context)
                    }
                }

                lifecycle.addObserver(observer)

                // Remove the observer when the effect is disposed.
                onDispose {
                    lifecycle.removeObserver(observer)
                }
            }

        }
    }

    override fun onPause() {
        super.onPause()
        val context = this.applicationContext
        photosViewModel.refresh(context)
        videoViewModel.refreshVideos(context)
        audioViewModel.loadAudio(context)
    }

    override fun onResume() {
        super.onResume()
        val context = this.applicationContext
        photosViewModel.refresh(context)
        videoViewModel.refreshVideos(context)
        audioViewModel.loadAudio(context)
    }

    override fun onRestart() {
        super.onRestart()
        onResume()
    }

    override fun onStart() {
        super.onStart()
        onResume()
    }
}


@RequiresApi(34)
@Composable
fun PhotosApp(
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    audioViewModel: AudioViewModel,
    playAudioViewModel: PlayAudioViewModel,
) {
    val currentTheme = isSystemInDarkTheme()
    val isDarkTheme = remember { mutableStateOf(currentTheme) }
    val navController = rememberNavController()
    val startDestination = Screens.LauncherScreen.route

    PhotosTheme(
        darkTheme = isDarkTheme.value,
        dynamicColor = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavigationGraph(
                navController = navController,
                startDestination = startDestination,
                isDarkTheme = isDarkTheme,
                photosViewModel = photosViewModel,
                videoViewModel = videoViewModel,
                audioViewModel = audioViewModel,
                playAudioViewModel = playAudioViewModel,
            )
        }
    }
}
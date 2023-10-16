@file:Suppress("DEPRECATION")

package com.littlebit.photos

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.rememberNavController
import com.bumptech.glide.annotation.GlideModule
import com.littlebit.photos.ui.navigation.NavigationGraph
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.audio.player.XAudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.settings.SettingsViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.player.VideoPlayerViewModel
import com.littlebit.photos.ui.theme.PhotosTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
@Suppress("DEPRECATION")
@RequiresApi(34)
@GlideModule
@UnstableApi
class MainActivity : ComponentActivity() {

    private val photosViewModel: PhotosViewModel by viewModels()
    private val videoViewModel: VideoViewModel by viewModels()
    private val audioViewModel: AudioViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val videoPlayerViewModel: VideoPlayerViewModel by viewModels()
    private val xAudioViewModel: XAudioViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        settingsViewModel.updateTheme()
        photosViewModel.getData(this)
        videoViewModel.getData(this)
        audioViewModel.getData(this)
        setContent {
            BitMediaApp(photosViewModel, videoViewModel, audioViewModel, settingsViewModel, videoPlayerViewModel, xAudioViewModel)
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, you can now call deletePhoto() again.
                } else {
                    // Permission denied. You can inform the user or handle it accordingly.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}


@RequiresApi(34)
@Composable
@UnstableApi
fun BitMediaApp(
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    audioViewModel: AudioViewModel,
    settingsViewModel: SettingsViewModel,
    videoPlayerViewModel: VideoPlayerViewModel,
    xAudioViewModel: XAudioViewModel
) {
    val currentTheme by settingsViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val systemTheme = isSystemInDarkTheme()
    val navController = rememberNavController()
    val startDestination = Screens.HomeScreen.route

    PhotosTheme(
        darkTheme = currentTheme ?: systemTheme,
        dynamicColor = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavigationGraph(
                navController = navController,
                startDestination = startDestination,
                photosViewModel = photosViewModel,
                videoViewModel = videoViewModel,
                audioViewModel = audioViewModel,
                settingsViewModel = settingsViewModel,
                videoPlayerViewModel = videoPlayerViewModel,
                xAudioViewModel = xAudioViewModel
            )
        }
    }
}


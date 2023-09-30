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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.littlebit.photos.ui.navigation.NavigationGraph
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.audio.player.PlayAudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.theme.PhotosTheme

@Suppress("DEPRECATION")
@RequiresApi(34)
class MainActivity : ComponentActivity() {
    private val photosViewModel: PhotosViewModel by viewModels()
    private val videoViewModel: VideoViewModel by viewModels()
    private val audioViewModel: AudioViewModel by viewModels()
    private val playAudioViewModel: PlayAudioViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        photosViewModel.addPhotosGroupedByDate(applicationContext)
        videoViewModel.addVideosGroupedByDate(applicationContext)
        setContent {
            PhotosApp(photosViewModel, videoViewModel, audioViewModel, playAudioViewModel)
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
fun PhotosApp(
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    audioViewModel: AudioViewModel,
    playAudioViewModel: PlayAudioViewModel,
) {
    val currentTheme = isSystemInDarkTheme()
    val isDarkTheme = remember { mutableStateOf(currentTheme) }
    val navController = rememberNavController()
    val startDestination = Screens.HomeScreen.route
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


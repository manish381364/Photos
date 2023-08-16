package com.littlebit.photos.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.images.grid.ImageGridScreen
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.grid.VideosGridScreen


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel
) {
    val currentScreen = rememberSaveable{ mutableStateOf(Screens.HomeScreen.route) }
    val bottomBarVisibility = remember { mutableStateOf(true) }
    val imageScreenListState = rememberLazyListState()
    val videoScreenListState = rememberLazyListState()
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Crossfade(targetState = currentScreen.value, label = "Screens") { screen ->
            when (screen) {
                Screens.HomeScreen.route -> {
                    AnimatedVisibility(true) {
                        ImageGridScreen(navHostController, photosViewModel, bottomBarVisibility, imageScreenListState)
                    }
                }

                Screens.VideoGridScreen.route -> {
                    AnimatedVisibility(true) {
                        VideosGridScreen(videoViewModel, bottomBarVisibility, videoScreenListState, navHostController)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = bottomBarVisibility.value,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HomeScreenBottomBar(
                Modifier,
                currentScreen,
            )
        }
    }
}










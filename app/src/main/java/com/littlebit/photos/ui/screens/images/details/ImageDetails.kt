package com.littlebit.photos.ui.screens.images.details

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.screens.images.PhotosViewModel

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun ImageDetailsScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    imageIndex: Int,
    listIndex: Int,
) {
    val mediaList = photosViewModel.photoGroups.collectAsState().value
    val appBarsVisible = remember {
        mutableStateOf(true)
    }
    val currentImageIndex = remember {
        mutableIntStateOf(imageIndex)
    }
    BackHandler {
        appBarsVisible.value = true
        navHostController.navigateUp()
    }
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = appBarsVisible.value,
                enter = fadeIn(tween(400)),
                exit = fadeOut(),
            ) {
                ImageDetailsTopBar(navHostController)
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = appBarsVisible.value,
                enter = fadeIn(tween(400)),
                exit = fadeOut()
            ) {
                ImageDetailsBottomBar(
                    photosViewModel,
                    listIndex,
                    currentImageIndex
                )
            }
        },
    ) { padding ->
        Modifier.padding(padding)
        ImageSwiper(
            images = mediaList,
            imageIndex = currentImageIndex,
            listIndex = listIndex,
        ) { appBarsVisible.value = !appBarsVisible.value }
    }
}








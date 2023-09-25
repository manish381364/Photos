package com.littlebit.photos.ui.screens.images.details

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.screens.images.PhotosViewModel


@Composable
fun ImageDetailsScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    imageIndex: Int,
    listIndex: Int,
) {
    val context = LocalContext.current
    val mediaList = photosViewModel.photoGroups.collectAsState().value
    val appBarsVisible = remember {
        mutableStateOf(true)
    }
    val currentImageIndex = remember {
        mutableIntStateOf(imageIndex)
    }
    val currentListIndex = remember {
        mutableIntStateOf(listIndex)
    }
    BackHandler {
        appBarsVisible.value = true
        navHostController.navigateUp()
    }
    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Handle successful deletion
            photosViewModel.removeImageFromGrid(listIndex, currentImageIndex)
            Toast.makeText(context, "Moved to trash", Toast.LENGTH_SHORT).show()
        }
    }
    Surface {
        Box(Modifier.fillMaxSize()) {
            ImageSwiper(
                imageGroups = mediaList,
                imageIndex = currentImageIndex,
                listIndex = currentListIndex.intValue,
            ) { appBarsVisible.value = !appBarsVisible.value }

            AnimatedVisibility(
                visible = appBarsVisible.value,
                enter = fadeIn(tween(400)),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                ImageDetailsTopBar(navHostController)
            }
            AnimatedVisibility(
                visible = appBarsVisible.value,
                enter = fadeIn(tween(400)),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ImageDetailsBottomBar(
                    photosViewModel,
                    listIndex,
                    currentImageIndex,
                    trashLauncher
                )
            }
        }

    }
}








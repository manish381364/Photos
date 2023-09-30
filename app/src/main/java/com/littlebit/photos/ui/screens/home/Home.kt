package com.littlebit.photos.ui.screens.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioListScreen
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.images.grid.ImageGridScreen
import com.littlebit.photos.ui.screens.search.SearchScreen
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.grid.VideosGridScreen


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    audioViewModel: AudioViewModel,
) {
    val context = LocalContext.current
    val currentScreen = rememberSaveable { mutableStateOf(Screens.HomeScreen.route) }
    val showAlertDialog = rememberSaveable { mutableStateOf(false) }
    val bottomBarVisibility = rememberSaveable { mutableStateOf(true) }
    val totalSelectedImages by photosViewModel.selectedImages.collectAsStateWithLifecycle()
    val totalSelectedAudios by audioViewModel.selectedAudios.collectAsStateWithLifecycle()
    val totalSelectedVideos by videoViewModel.selectedVideos.collectAsStateWithLifecycle()
    val photosSelectionInProgress = totalSelectedImages > 0
    val audioSelectionInProgress = totalSelectedAudios > 0
    val videoSelectionInProgress = totalSelectedVideos > 0
    val bottomSheetVisible =
        photosSelectionInProgress || audioSelectionInProgress || videoSelectionInProgress
    val memorySize = getTotalMemorySize(photosViewModel, audioViewModel, videoViewModel, context)
    val imageScreenListState = rememberLazyListState()
    val videoScreenListState = rememberLazyListState()
    val audioScreenListState = rememberLazyListState()
    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        trashResult(
            result,
            audioSelectionInProgress,
            audioViewModel,
            context,
            videoSelectionInProgress,
            videoViewModel,
            photosViewModel
        )
    }

    Box(
        Modifier
            .fillMaxSize()
    ) {
        Crossfade(
            targetState = currentScreen.value,
            label = "Screens",
            animationSpec = tween(300, easing = EaseIn)
        ) { screen ->
            when (screen) {
                Screens.HomeScreen.route -> {
                    AnimatedVisibility(true) {
                        ImageGridScreen(
                            navHostController,
                            photosViewModel,
                            bottomBarVisibility,
                            imageScreenListState,
                            showAlertDialog
                        )
                    }
                }

                Screens.SearchScreen.route -> {
                    AnimatedVisibility(true) {
                        SearchScreen(
                            navHostController,
                            photosViewModel = photosViewModel,
                            videoViewModel = videoViewModel,
                            currentScreen = currentScreen
                        )
                    }
                }

                Screens.AudioListScreen.route -> {
                    AnimatedVisibility(true) {
                        AudioListScreen(navHostController, audioViewModel, audioScreenListState, showAlertDialog)
                    }
                }

                Screens.VideoGridScreen.route -> {
                    AnimatedVisibility(true) {
                        VideosGridScreen(
                            videoViewModel,
                            bottomBarVisibility,
                            videoScreenListState,
                            navHostController,
                            showAlertDialog
                        )
                    }
                }
            }
        }


        FloatingProfileDialog(showAlertDialog, navHostController)

        val onClickUnSelectAll = removeAllSelected(
            audioSelectionInProgress,
            audioViewModel,
            videoSelectionInProgress,
            videoViewModel,
            photosViewModel
        )
        val totalSelected = getTotalSelected(
            audioSelectionInProgress,
            totalSelectedAudios,
            photosSelectionInProgress,
            totalSelectedImages,
            totalSelectedVideos
        )
        AnimatedVisibility(
            visible = bottomSheetVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            FloatingActionButton(
                onClick = {
                    onClickUnSelectAll()
                },
                modifier = Modifier
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                    .width(70.dp)
            ) {
                Row {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = totalSelected.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }


        AnimatedVisibility(
            visible = bottomBarVisibility.value && !bottomSheetVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HomeScreenBottomBar(
                Modifier,
                currentScreen,
                imageScreenListState,
                videoScreenListState,
                audioScreenListState
            )
        }


        AnimatedVisibility(
            visible = bottomSheetVisible,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val sheetState = rememberBottomSheetScaffoldState()
            BottomSheetScaffold(
                sheetContent = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.1f),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onClickUnSelectAll() }) {
                            Icon(imageVector = Icons.Outlined.Close, contentDescription = "")
                        }
                        Column(Modifier.padding(4.dp), horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "$totalSelected Selected",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = memorySize,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        IconButton(onClick = { onClickShareButton(audioSelectionInProgress, audioViewModel, videoSelectionInProgress, videoViewModel, photosViewModel, context, onClickUnSelectAll = onClickUnSelectAll )}) {
                           Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share Button")
                        }
                        IconButton(onClick = { onClickDeleteButton(audioSelectionInProgress, audioViewModel, videoSelectionInProgress, videoViewModel, photosViewModel, context, trashLauncher) }) {
                            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete Button")
                        }
                    }
                },
                sheetPeekHeight = 120.dp,
                scaffoldState = sheetState,
                sheetSwipeEnabled = true,
                sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {}
        }
    }
}


private fun trashResult(
    result: ActivityResult,
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    context: Context,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel
) {
    if (result.resultCode == Activity.RESULT_OK) {
        // Handle successful deletion
        if (audioSelectionInProgress) {
            audioViewModel.removeAudiosFromList(context)
        } else if (videoSelectionInProgress) {
            videoViewModel.removeVideosFromList(context)
        } else {
            photosViewModel.removeImagesFromList(context)
        }
    } else {
        if (audioSelectionInProgress) {
            audioViewModel.unSelectAllAudio()
        } else if (videoSelectionInProgress) {
            videoViewModel.unSelectAllVideos()
        } else {
            photosViewModel.unSelectAllImages()
        }
    }
}

fun onClickDeleteButton(
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel,
    context: Context,
    trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    if(audioSelectionInProgress) {
        audioViewModel.moveToTrashSelectedAudios(context, trashLauncher)
    }
    else if(videoSelectionInProgress){
        videoViewModel.moveToTrashSelectedVideos(context, trashLauncher)
    }
    else{
        photosViewModel.moveToTrashSelectedImages(context, trashLauncher)
    }
}

fun onClickShareButton(
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel,
    context: Context,
    onClickUnSelectAll: () -> Unit
) {
    if(audioSelectionInProgress) {
        val shareIntent = audioViewModel.shareSelectedAudios()
        onClickUnSelectAll()
        context.startActivity(Intent.createChooser(shareIntent, "Share Audios"))
    }
    else if(videoSelectionInProgress){
        val shareIntent = videoViewModel.shareSelectedVideos()
        onClickUnSelectAll()
        context.startActivity(Intent.createChooser(shareIntent, "Share Videos"))
    }
    else{
        val shareIntent = photosViewModel.shareSelectedImages()
        onClickUnSelectAll()
        context.startActivity(Intent.createChooser(shareIntent, "Share Photos"))
    }
}













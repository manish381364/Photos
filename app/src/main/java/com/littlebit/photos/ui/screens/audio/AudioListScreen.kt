package com.littlebit.photos.ui.screens.audio

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.home.profile_image


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AudioListScreen(
    navHostController: NavHostController,
    audioViewModel: AudioViewModel,
    audioScreenListState: LazyListState,
    showAlertDialog: MutableState<Boolean>,
) {

    val context = LocalContext.current
    val launcherForPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            audioViewModel.loadAudio(context)
        }
    }
    LaunchedEffect(Unit) {
        launcherForPermission.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
    }
    val audioList by audioViewModel.audioList.collectAsStateWithLifecycle()
    val isLoading by audioViewModel.isLoading.collectAsStateWithLifecycle()
    if (!isLoading && audioList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Audio Found")
        }
        return
    } else if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface)
        }
        return
    }
    val isSelectionInProcess by audioViewModel.isSelectionInProcess.collectAsStateWithLifecycle()
    val showFileInfo = remember {
        mutableStateOf(false)
    }
    val currentFile = remember {
        mutableStateOf(audioList[0])
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(Spring.StiffnessLow),
        flingAnimationSpec = rememberSplineBasedDecay()
    )
    Surface {
        AnimatedVisibility(
            visible = !showFileInfo.value,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(200, easing = EaseOut)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300, easing = EaseIn)
            )
        ) {
            Scaffold(
                topBar = {
                    AudioScreenTopBar(scrollBehavior, showAlertDialog, audioViewModel)
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    LazyColumn(state = audioScreenListState) {
                        itemsIndexed(audioList) { index, audioFile ->
                            AudioItem(
                                audioFile,
                                onClick = {
                                    if (isSelectionInProcess) {
                                        audioViewModel.setSelectedAudio(index)
                                    } else
                                        navHostController.navigate(Screens.PlayAudioScreen.route + "/${index}") {
                                            launchSingleTop = true
                                        }
                                },
                                audioViewModel,
                                index
                            ) {
                                currentFile.value = audioFile
                                showFileInfo.value = true
                            }
                        }
                    }
                }
            }

        }
        FileInfo(showFileInfo, currentFile)
    }
}

@Composable
private fun FileInfo(
    showFileInfo: MutableState<Boolean>,
    currentFile: MutableState<AudioItem>
) {
    AnimatedVisibility(
        visible = showFileInfo.value,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(200, easing = EaseOut)
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(200, easing = EaseIn)
        )
    ) {
        FileInfo(currentFile) {
            showFileInfo.value = false
        }
        BackHandler {
            showFileInfo.value = false
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AudioScreenTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    showAlertDialog: MutableState<Boolean>,
    audioViewModel: AudioViewModel
) {

    val isImageLoading = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Bit Audios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                AsyncImage(
                    model = profile_image,
                    contentDescription = "",
                    modifier = Modifier
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.BottomEnd)
                        .clickable {
                            showAlertDialog.value = true
                            audioViewModel.refreshAudioList(context)
                        },
                    contentScale = ContentScale.Crop,
                    onLoading = {
                        // Display a circular, indeterminate progress indicator
                        isImageLoading.value = true
                    },
                    onError = {
                        isImageLoading.value = false
                    },
                    onSuccess = {
                        isImageLoading.value = false
                    }
                )
                AnimatedVisibility(
                    visible = isImageLoading.value,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    CircularProgressIndicator(
                        Modifier
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                }

            }
        },
        scrollBehavior = scrollBehavior,
    )
}




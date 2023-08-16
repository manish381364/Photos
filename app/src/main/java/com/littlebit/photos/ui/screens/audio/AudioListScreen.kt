package com.littlebit.photos.ui.screens.audio

import android.Manifest
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AudioListScreen(
    navHostController: NavHostController,
    audioViewModel: AudioViewModel,
    audioScreenListState: LazyListState,
) {

    val context = LocalContext.current
    val notificationManager = context.getSystemService(NotificationManager::class.java)
    val postNotificationPermission =
        rememberPermissionState(permission = Manifest.permission.READ_MEDIA_AUDIO)
    SideEffect {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }
    val audioList by audioViewModel.audioList.collectAsStateWithLifecycle()
    if (audioList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues()),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No Audio Found")
        }
        return
    }
    val isSelectionInProcess = remember {
        mutableStateOf(false)
    }
    var showFileInfo by remember {
        mutableStateOf(false)
    }
    val currentFile = remember {
        mutableStateOf(audioList[0])
    }
    Surface {
        AnimatedVisibility(
            visible = !showFileInfo,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(200, easing = EaseOut)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300, easing = EaseIn)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars)
            ) {
                LazyColumn(state = audioScreenListState) {
                    itemsIndexed(audioList) { index, audioFile ->
                        AudioItem(
                            audioFile,
                            onClick = {
                                if (isSelectionInProcess.value) {
                                    audioViewModel.setSelectedAudio(index, isSelectionInProcess)
                                } else
                                    navHostController.navigate("playAudioScreen/$index") {
                                        launchSingleTop = true
                                    }
                            },
                            isSelectionInProcess,
                            audioViewModel,
                            index
                        ) {
                            currentFile.value = audioFile
                            showFileInfo = true
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showFileInfo,
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
                showFileInfo = false
            }
        }
    }
}




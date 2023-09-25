package com.littlebit.photos.ui.screens.audio

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.navigation.Screens


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AudioListScreen(
    navHostController: NavHostController,
    audioViewModel: AudioViewModel,
    audioScreenListState: LazyListState,
) {

    val context = LocalContext.current
    val launcherForPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            audioViewModel.loadAudio(context)
        }
    }
    LaunchedEffect(Unit){
        launcherForPermission.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
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
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
            Scaffold(
                topBar = {
                    AudioScreenTopBar(scrollBehavior)
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
                                    if (isSelectionInProcess.value) {
                                        audioViewModel.setSelectedAudio(index, isSelectionInProcess)
                                    } else
                                        navHostController.navigate(Screens.PlayAudioScreen.route + "/${index}") {
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

        }
        FileInfo(showFileInfo, currentFile)
    }
}

@Composable
private fun FileInfo(
    showFileInfo: Boolean,
    currentFile: MutableState<Audio>
) {
    var showFileInfo1 = showFileInfo
    AnimatedVisibility(
        visible = showFileInfo1,
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
            showFileInfo1 = false
        }
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AudioScreenTopBar(scrollBehavior: TopAppBarScrollBehavior){
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        title = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
                    Text(text = "Bit Audios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
        },
        scrollBehavior = scrollBehavior,
    )
}




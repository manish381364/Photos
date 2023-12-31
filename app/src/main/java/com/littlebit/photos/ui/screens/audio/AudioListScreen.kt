package com.littlebit.photos.ui.screens.audio

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.model.ScaleTransitionDirection
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.navigation.scaleIntoContainer
import com.littlebit.photos.ui.navigation.scaleOutOfContainer
import com.littlebit.photos.ui.screens.home.ScreenTopBar


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AudioListScreen(
    navHostController: NavHostController,
    audioViewModel: AudioViewModel,
    audioScreenListState: LazyListState,
    showAlertDialog: MutableState<Boolean>,
) {

    val context = LocalContext.current
    val audioList by audioViewModel.audioList.collectAsStateWithLifecycle()
    val isLoading by audioViewModel.isLoading.collectAsStateWithLifecycle()
    val launcherForPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (audioList.isEmpty()) audioViewModel.loadAudio(context)
        }
    }
    LaunchedEffect(Unit) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            launcherForPermission.launch(android.Manifest.permission.READ_MEDIA_AUDIO)
        else launcherForPermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }
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
    val isRefreshing by audioViewModel.isLoading.collectAsStateWithLifecycle()
    val currentFile = remember {
        mutableStateOf(audioList[0])
    }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(Spring.StiffnessLow),
        flingAnimationSpec = rememberSplineBasedDecay()
    )
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            audioViewModel.refreshAudioList(context)
            if (audioViewModel.isSelectionProgress()) {
                audioViewModel.unSelectAllAudio()
            }
        }
    )
    Surface {
        AnimatedVisibility(
            visible = !showFileInfo.value,
            enter = scaleIntoContainer(),
            exit = scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
        ) {
            Scaffold(
                topBar = {
                    ScreenTopBar(scrollBehavior = scrollBehavior, title = "Bit Audios") {
                        showAlertDialog.value = true
                    }
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                Box(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars)
                ) {
                    LazyColumn(
                        state = audioScreenListState, modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ) {
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
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
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
        enter = scaleIntoContainer(),
        exit = scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS),
    ) {
        FileInfo(currentFile) {
            showFileInfo.value = false
        }
        BackHandler {
            showFileInfo.value = false
        }
    }
}








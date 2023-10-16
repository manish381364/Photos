package com.littlebit.photos.ui.screens.images.grid

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.screens.home.ImageGridList
import com.littlebit.photos.ui.screens.home.ScreenTopBar
import com.littlebit.photos.ui.screens.images.PhotosViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ImageGridScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    bottomBarVisibility: MutableState<Boolean>,
    imageScreenListState: LazyListState,
    showAlertDialog: MutableState<Boolean>,
) {
    val context = LocalContext.current
    var lastScrollPosition by remember { mutableIntStateOf(0) }
    val imageGroups by photosViewModel.photoGroups.collectAsStateWithLifecycle()
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
        snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
        flingAnimationSpec = rememberSplineBasedDecay()
    )
    val isRefreshing by photosViewModel.isLoading.collectAsStateWithLifecycle()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            photosViewModel.refresh(context)
            if (photosViewModel.isSelectionInProgress()) photosViewModel.unSelectAllImages()
        }
    )




    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .background(
                Color.Transparent
            ),
        topBar = {
            ScreenTopBar(
                modifier = Modifier,
                scrollBehavior = topAppBarScrollBehavior,
                title = "Bit Photos"
            ) {
                showAlertDialog.value = true
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ImageGridList(
                navHostController,
                photosViewModel,
                imageScreenListState,
                pullRefreshState
            )

            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                scale = true,
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }


    val readImagePermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Permission denied, show a Toast message and navigate to a screen where the user can grant permissions
            Toast.makeText(context, "Grant permission to continue", Toast.LENGTH_SHORT).show()
            // open the app's settings page to allow the user to grant the permissions manually
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            appSettingsIntent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(appSettingsIntent)
        } else if (imageGroups.isEmpty()) photosViewModel.addPhotosGroupedByDate(context)
    }


    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readImagePermission.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            readImagePermission.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }



    LaunchedEffect(imageScreenListState) {
        snapshotFlow { imageScreenListState.firstVisibleItemIndex }
            .collect { firstVisibleItemIndex ->
                val scrollDelta = firstVisibleItemIndex - lastScrollPosition
                lastScrollPosition = firstVisibleItemIndex

                if (scrollDelta > 0) {
                    // Scrolling down
                    bottomBarVisibility.value = false
                } else if (scrollDelta < 0) {
                    // Scrolling up
                    bottomBarVisibility.value = true
                }
            }
    }
}
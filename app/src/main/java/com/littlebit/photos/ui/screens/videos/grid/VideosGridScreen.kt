package com.littlebit.photos.ui.screens.videos.grid

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.littlebit.photos.ui.screens.videos.VideoViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun VideosGridScreen(
    videoViewModel: VideoViewModel,
    bottomBarVisibility: MutableState<Boolean>,
    videoScreenListState: LazyListState,
    navHostController: NavHostController
) {
    var lastScrollPosition by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val list = videoViewModel.videoGroups.collectAsState().value
    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            VideoScreenTopBar(topAppBarScrollBehavior = topAppBarScrollBehavior)
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Surface {
                VideoGridList(
                    videoViewModel = videoViewModel,
                    videoScreenListState,
                    navHostController
                )
            }
        }
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                appSettingsIntent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(appSettingsIntent)
            }
        }

    val permissionState =
        rememberPermissionState(permission = android.Manifest.permission.READ_MEDIA_VIDEO)


    // Request permission when the screen is first composed
    LaunchedEffect(true) {
        if (Build.VERSION.SDK_INT < 33)
            launcher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        else
            launcher.launch(android.Manifest.permission.READ_MEDIA_VIDEO)
    }


    LaunchedEffect(videoScreenListState) {
        snapshotFlow { videoScreenListState.firstVisibleItemIndex }
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

    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) {
            if(list.isEmpty())
                videoViewModel.refreshVideos(context)
        }
    }
}















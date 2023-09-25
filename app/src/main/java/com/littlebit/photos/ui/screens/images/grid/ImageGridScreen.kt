package com.littlebit.photos.ui.screens.images.grid

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.littlebit.photos.ui.screens.home.FloatingProfileDialog
import com.littlebit.photos.ui.screens.home.HomeScreenTopBar
import com.littlebit.photos.ui.screens.home.ImageGridList
import com.littlebit.photos.ui.screens.images.PhotosViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageGridScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    bottomBarVisibility: MutableState<Boolean>,
    imageScreenListState: LazyListState,
) {
    var lastScrollPosition by remember { mutableIntStateOf(0) }
    val list = photosViewModel.photoGroups.collectAsState().value
    val showAlertDialog = remember {
        mutableStateOf(false)
    }
    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()


    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .background(
                Color.Transparent
            ),
        topBar = {
            HomeScreenTopBar(
                modifier = Modifier,
                showAlertDialog,
                photosViewModel = photosViewModel,
                scrollBehavior = topAppBarScrollBehavior
            )
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
            )
        }
    }
    FloatingProfileDialog(showAlertDialog, navHostController)

    val context = LocalContext.current

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
        }
    }


    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readImagePermission.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
        }
        else{
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

    val rememberPermissionState = rememberPermissionState(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    val readPhotosPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(android.Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        null
    }

    LaunchedEffect(Unit){
        if (rememberPermissionState.status.isGranted) {
            if(list.isEmpty()) photosViewModel.refresh(context)
        }
        if(readPhotosPermissionState?.status?.isGranted == true){
            if(list.isEmpty())
                photosViewModel.refresh(context)
        }
    }
}
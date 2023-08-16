package com.littlebit.photos.ui.screens.images.grid

import android.os.Build
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.screens.home.FloatingProfileDialog
import com.littlebit.photos.ui.screens.home.HomeScreenTopBar
import com.littlebit.photos.ui.screens.home.ImageGridList
import com.littlebit.photos.ui.screens.images.PhotosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageGridScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    bottomBarVisibility: MutableState<Boolean>,
    imageScreenListState: LazyListState
) {
    var lastScrollPosition by remember { mutableIntStateOf(0) }
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
                imageScreenListState
            )
        }
    }
    FloatingProfileDialog(showAlertDialog)






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
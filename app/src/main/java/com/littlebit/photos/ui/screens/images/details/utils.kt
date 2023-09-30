package com.littlebit.photos.ui.screens.images.details

import android.net.Uri
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.twotone.CenterFocusStrong
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.images.PhotosViewModel


@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun ImageSwiper(
    imageGroups: List<ImageGroup>,
    imageIndex: MutableIntState,
    listIndex: Int,
    onImageClick: () -> Unit = {},
) {
    var fingersCount by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(
        initialPage = imageIndex.intValue,
    ) {
        imageGroups[listIndex].images.size
    }
    val currentPage = pagerState.currentPage
    var scale by remember { mutableFloatStateOf(1f) }
    val offsetState = remember { mutableStateOf(Offset(0f, 0f)) }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            imageIndex.intValue = page
            pagerState.animateScrollToPage(page)
            scale = 1f
            offsetState.value = Offset(0f, 0f)
        }
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    val isCurrentlyOnSettledPage = pagerState.isScrollInProgress.not()
                    if (isCurrentlyOnSettledPage) {
                        scale *= zoom
                        val minScale = 1f
                        val maxScale = 5f
                        scale = scale.coerceIn(minScale, maxScale)

                        // Calculate new offset based on the image boundaries
                        val scaledWidth = size.width * scale
                        val scaledHeight = size.height * scale

                        val maxOffsetX = (scaledWidth - size.width) / 2
                        val maxOffsetY = (scaledHeight - size.height) / 2.2f

                        offsetState.value = Offset(
                            offsetState.value.x + pan.x * scale,
                            offsetState.value.y + pan.y * scale
                        ).coerceIn(
                            minX = -maxOffsetX,
                            maxX = maxOffsetX,
                            minY = -maxOffsetY,
                            maxY = maxOffsetY
                        )
                    }
                }
            }

    ) {
        val scaleState = animateFloatAsState(targetValue = scale, label = "scale")
        val animateOffset = animateOffsetAsState(targetValue = offsetState.value, label = "offset")
        HorizontalPager(
            pagerState,
            modifier = Modifier
                .fillMaxSize(),
            userScrollEnabled = scale <= 1f && fingersCount <= 1
        ) { page ->
            val imageUri =
                if (page >= 0 && page < imageGroups[listIndex].images.size) imageGroups[listIndex].images[page].uri else Uri.EMPTY
            GlideImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .graphicsLayer(
                        scaleX = if (pagerState.isScrollInProgress && currentPage == page) 1f else scaleState.value,
                        scaleY = if (pagerState.isScrollInProgress && currentPage == page) 1f else scaleState.value,
                        translationX = if (pagerState.isScrollInProgress && currentPage == page) 0f else animateOffset.value.x,
                        translationY = if (pagerState.isScrollInProgress && currentPage == page) 0f else animateOffset.value.y
                    )
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            do {
                                val event = awaitPointerEvent()
                                fingersCount = event.changes.size
                                Log.d("POINTER", "ImageSwiper:  fingersCount: $fingersCount")
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                // Zoom-in or Zoom-out based on current scale
                                val targetScale = if (scale > 1f) 1f else 2f
                                if (scale <= 1f) {
                                    val offsetX = (tapOffset.x - size.width / 2) * (scale - 1)
                                    val offsetY = (tapOffset.y - size.height / 2) * (scale - 1)
                                    val maxOffsetX = (size.width * scale - size.width) / 2
                                    val maxOffsetY = (size.height * scale - size.height) / 2
                                    offsetState.value = Offset(
                                        offsetX.coerceIn(-maxOffsetX, maxOffsetX),
                                        offsetY.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                } else offsetState.value = Offset(0f, 0f)
                                scale = targetScale
                            },
                            onTap = {
                                onImageClick()
                            },
                        )
                    }
                    .fillMaxSize()
                    .padding(1.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }

}

fun Offset.coerceIn(minX: Float, maxX: Float, minY: Float, maxY: Float): Offset {
    val coercedX = x.coerceIn(minX, maxX)
    val coercedY = y.coerceIn(minY, maxY)
    return Offset(coercedX, coercedY)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDetailsTopBar(navHostController: NavHostController) {
    var isPoping by remember {
        mutableStateOf(false)
    }
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = {
                    isPoping = true
                    navHostController.popBackStack(Screens.HomeScreen.route, inclusive = false)
                },
                enabled = !isPoping
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back Button")
            }
        },
        actions = {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Outlined.Cast, contentDescription = "Cast")
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "Options")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        modifier = Modifier.background(
            Brush.verticalGradient(
                listOf(
                    BottomAppBarDefaults.containerColor,
                    BottomAppBarDefaults.containerColor.copy(0.7f),
                    BottomAppBarDefaults.containerColor.copy(0.4f)
                )
            )
        )
    )
}


@Composable
fun ImageDetailsBottomBar(
    photosViewModel: PhotosViewModel,
    listIndex: Int,
    currentImageIndex: MutableIntState,
    trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
) {
    val context = LocalContext.current
    val showDeleteDialog = remember {
        mutableStateOf(false)
    }
    val shareLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}









    BottomAppBar(
        Modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        BottomAppBarDefaults.containerColor.copy(
                            0.4f
                        ),
                        BottomAppBarDefaults.containerColor.copy(0.7f),
                        BottomAppBarDefaults.containerColor
                    )
                )
            )
            .clip(RoundedCornerShape(topStart = 160.dp, topEnd = 160.dp)),
        containerColor = Color.Transparent
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {
            photosViewModel.shareImage(listIndex, currentImageIndex.intValue, shareLauncher)
        }) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share")
        }
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Outlined.Tune, contentDescription = "Edit")
        }
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = {

        }) {
            Icon(imageVector = Icons.TwoTone.CenterFocusStrong, contentDescription = "Lens")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                photosViewModel.moveToTrash(
                    context,
                    listIndex,
                    currentImageIndex.intValue,
                    trashLauncher,
                    showDeleteDialog
                )
            },
        ) {
            Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete")
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    ConfirmDeleteDialog(
        listIndex,
        currentImageIndex,
        photosViewModel,
        showDeleteDialog
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDeleteDialog(
    listIndex: Int,
    imageIndex: MutableIntState,
    photosViewModel: PhotosViewModel,
    showDeleteDialog: MutableState<Boolean>
) {
    val context = LocalContext.current
    AnimatedVisibility(visible = showDeleteDialog.value) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteDialog.value = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "The picture will be deleted?", modifier = Modifier.padding(16.dp))
            }
            HorizontalDivider(Modifier.fillMaxWidth())
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        photosViewModel.deletePhoto(listIndex, imageIndex, context)
                        showDeleteDialog.value = false
                    }, contentAlignment = Alignment.Center
            ) {
                Text(text = "Delete", modifier = Modifier.padding(16.dp))
            }
            HorizontalDivider(Modifier.fillMaxWidth())
            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDeleteDialog.value = false
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Cancel", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
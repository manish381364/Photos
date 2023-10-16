package com.littlebit.photos.ui.screens.images.grid

import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.home.DateWithMarkButton
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.grid.isLandscape

@Suppress("DEPRECATION")
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageItem(
    imageUri: Uri,
    onImageClick: (Uri) -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop,
    image: PhotoItem,
    photosViewModel: PhotosViewModel,
    listIndex: Int,
    index: Int,
) {
    val selectedImages by photosViewModel.selectedImages.collectAsStateWithLifecycle()
    val selectedImageList by photosViewModel.selectedImageList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val imageSize =
        if (isLandscape()) (LocalConfiguration.current.screenWidthDp - 1) / 5 else (LocalConfiguration.current.screenWidthDp - 1) / 3
    val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
    val size by animateDpAsState(
        targetValue = if (image.isSelected) (imageSize - 10).dp else imageSize.dp,
        label = "",
        animationSpec = tween(90, 10)
    )
    val cornerSize by animateDpAsState(
        targetValue = if (image.isSelected) 20.dp else 0.dp,
        label = "",
        animationSpec = tween(90, 10)
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(141.dp)
            .background(if (image.isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
    ) {
        GlideImage(
            model = imageUri,
            contentDescription = null,
            modifier = Modifier
                .padding(PaddingValues(1.dp))
                .size(size)
                .clip(RoundedCornerShape(cornerSize))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            if (selectedImages == 0) {
                                photosViewModel.selectedImages.value++
                                image.isSelected = true
                                selectedImageList[image.id] = Pair(listIndex, index)
                                // Trigger haptic feedback on long press
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(
                                        VibrationEffect.createOneShot(
                                            50, // Duration in milliseconds
                                            VibrationEffect.DEFAULT_AMPLITUDE
                                        )
                                    )
                                } else {
                                    // For older devices
                                    vibrator?.vibrate(50) // Vibrate for 50 milliseconds
                                }
                            }
                        },
                        onTap = {
                            if (selectedImages > 0) {
                                image.isSelected = !image.isSelected
                                if (image.isSelected) {
                                    photosViewModel.selectedImages.value++
                                    selectedImageList[image.id] = Pair(listIndex, index)
                                } else {
                                    photosViewModel.selectedImages.value--
                                    selectedImageList.remove(image.id)
                                }
                            } else onImageClick(imageUri)
                        }
                    )
                },
            contentScale = contentScale,
            transition = CrossFade,
        )
        val icon = if (image.isSelected) Icons.Filled.CheckCircle else Icons.Outlined.Circle
        val tint = if (selectedImages > 0) {
            if (image.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        } else Color.Transparent

        Icon(
            imageVector = icon, contentDescription = "Circle", tint = tint, modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageGroupSection(
    imageGroup: ImageGroup,
    imageIndex: MutableState<Int>,
    listIndex: Int,
    photosViewModel: PhotosViewModel,
    onImageClick: (index: Int) -> Unit
) {
    Column(
        Modifier.fillMaxWidth(1f)
    ) {
        DateWithMarkButton(date = imageGroup.date) {
            photosViewModel.markImages(imageGroup, listIndex)
        }
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            imageGroup.images.forEachIndexed { index, image ->
                ImageItem(
                    imageUri = image.uri!!,
                    {
                        imageIndex.value = index
                        onImageClick(index)
                    },
                    ContentScale.Crop,
                    image,
                    photosViewModel,
                    listIndex = listIndex,
                    index = index
                )
            }
        }
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageGridList(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    scrollState: LazyListState,
    pullRefreshState: PullRefreshState
) {
    val imageGroups by photosViewModel.photoGroups.collectAsStateWithLifecycle()
    val isLoading by photosViewModel.isLoading.collectAsStateWithLifecycle()
    val imageIndex = remember {
        mutableIntStateOf(0)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState),
        state = scrollState,
    ) {
        item {
            Spacer(modifier = Modifier.height(100.dp))
            EmptyMessage(imageGroups, isLoading)
        }
        items(imageGroups.size) { index ->
            val imageGroup = imageGroups[index]
            ImageGroupSection(
                imageGroup,
                imageIndex,
                listIndex = index,
                photosViewModel
            ) { clickedImageIndex ->
                navHostController.navigate(Screens.ImageDetailsScreen.route + "/${clickedImageIndex}/${index}") {
                    launchSingleTop = true
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun EmptyMessage(
    imageGroups: MutableList<ImageGroup>,
    isLoading: Boolean
) {
    if (imageGroups.isEmpty()) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(PaddingValues(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!isLoading) {
                Column(
                    Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Photos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "You can add photos to your phone and they will show up here",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
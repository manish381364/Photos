package com.littlebit.photos.ui.screens.home


import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.outlined.ArrowDropDownCircle
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoCameraBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*



@Composable
fun DateWithMarkButton(
    modifier: Modifier = Modifier,
    date: String,
    onButtonClick: () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(PaddingValues(4.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //format day, date month
        Text(
            text = date,
            style = MaterialTheme.typography.labelLarge
        )
        IconButton(onClick = { onButtonClick() }) {
            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Check Circle")
        }
    }
}


const val profile_image =
    "https://w0.peakpx.com/wallpaper/343/852/HD-wallpaper-iphone-planet-amoled-apple-galaxy-gold-life-space-strange-ultra.jpg"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    onClick: () -> Unit
) {
    val isImageLoading = remember {
        mutableStateOf(false)
    }
    Box {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            ),
            title = {
                Box(Modifier.fillMaxWidth()) {

                    Text(
                        text = title,
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
                                onClick()
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
                                .padding(4.dp)
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            modifier = modifier
        )
        if (scrollBehavior.state.heightOffset == 0f)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                thickness = (0.2).dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
    }
}

@Composable
fun HomeScreenBottomBar(
    modifier: Modifier = Modifier,
    currentScreen: MutableState<String>,
    imageScreenListState: LazyListState,
    videoScreenListState: LazyListState,
    audioScreenListState: LazyListState,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val photosSelected = currentScreen.value == Screens.HomeScreen.route
    val searchSelected = currentScreen.value == Screens.SearchScreen.route
    val audioSelected = currentScreen.value == Screens.AudioScreen.route
    val videoSelected = currentScreen.value == Screens.VideoGridScreen.route
    val photoIcon = if (photosSelected) Icons.Filled.Image else Icons.Outlined.Image
    val searchIcon = if (searchSelected) Icons.Filled.Search else Icons.Outlined.Search
    val audioIcon = if (audioSelected) Icons.Filled.Audiotrack else Icons.Outlined.Audiotrack
    val videoIcon =
        if (videoSelected) Icons.Filled.VideoCameraBack else Icons.Outlined.VideoCameraBack
    val onPhotoIconClick =
        getOnPhotoIconClick(imageScreenListState, photosSelected, coroutineScope, currentScreen)
    val onSearchIconClick = getOnSearchIconClick(keyboardController, currentScreen)
    val onAudioIconClick =
        getOnAudioIconClick(audioScreenListState, audioSelected, coroutineScope, currentScreen)
    val onVideoClickIcon =
        getOnVideoClickIcon(videoScreenListState, videoSelected, coroutineScope, currentScreen)
    BottomAppBar(
        modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer.copy(0.9f),
                shape = RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
            )
            .background(
                Brush.verticalGradient(getBottomBarContainerColor()),
                shape = RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
            ),
        containerColor = Color.Transparent,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = photosSelected,
            onClick = { onPhotoIconClick() },
            icon = { Icon(imageVector = photoIcon, contentDescription = "Image Icon") })

        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = searchSelected,
            onClick = { onSearchIconClick() },
            icon = { Icon(imageVector = searchIcon, contentDescription = "Search Icon") })

        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = audioSelected,
            onClick = { onAudioIconClick() },
            icon = { Icon(imageVector = audioIcon, contentDescription = "Audio Track Icon") })

        Spacer(modifier = Modifier.weight(1f))
        NavigationBarItem(
            selected = videoSelected,
            onClick = { onVideoClickIcon() },
            icon = { Icon(imageVector = videoIcon, contentDescription = "Video Camera Icon") })
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Composable
private fun getBottomBarContainerColor() = listOf(
    BottomAppBarDefaults.containerColor.copy(0.3f),
    BottomAppBarDefaults.containerColor.copy(0.5f),
    BottomAppBarDefaults.containerColor.copy(0.7f),
    BottomAppBarDefaults.containerColor.copy(0.9f),
    BottomAppBarDefaults.containerColor
)

@Composable
private fun getOnVideoClickIcon(
    videoScreenListState: LazyListState,
    videoSelected: Boolean,
    coroutineScope: CoroutineScope,
    currentScreen: MutableState<String>
): () -> Unit = {
    if (videoScreenListState.firstVisibleItemIndex > 2 && videoSelected)
        coroutineScope.launch {
            videoScreenListState.animateScrollToItem(0)
        }
    else currentScreen.value = Screens.VideoGridScreen.route
}

@Composable
private fun getOnAudioIconClick(
    audioScreenListState: LazyListState,
    audioSelected: Boolean,
    coroutineScope: CoroutineScope,
    currentScreen: MutableState<String>
): () -> Unit = {
    if (audioScreenListState.firstVisibleItemIndex > 2 && audioSelected)
        coroutineScope.launch {
            audioScreenListState.animateScrollToItem(0)
        }
    else currentScreen.value = Screens.AudioScreen.route
}

@Composable
private fun getOnSearchIconClick(
    keyboardController: SoftwareKeyboardController?,
    currentScreen: MutableState<String>
): () -> Unit = {
    keyboardController?.show()
    currentScreen.value = Screens.SearchScreen.route
}

@Composable
private fun getOnPhotoIconClick(
    imageScreenListState: LazyListState,
    photosSelected: Boolean,
    coroutineScope: CoroutineScope,
    currentScreen: MutableState<String>
): () -> Unit = {
    if (imageScreenListState.firstVisibleItemIndex > 2 && photosSelected) {
        coroutineScope.launch {
            imageScreenListState.animateScrollToItem(0)
        }
    } else currentScreen.value = Screens.HomeScreen.route
}

@Composable
fun FloatingProfileDialog(
    showAlertDialog: MutableState<Boolean>,
    navHostController: NavHostController
) {
    if (showAlertDialog.value) {
        var yOffset by remember { mutableFloatStateOf(0f) }
        val animateDp = animateDpAsState(targetValue = yOffset.dp, label = "yOffset Animation")

        Dialog(
            onDismissRequest = {
                showAlertDialog.value = false
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PaddingValues(start = 10.dp, end = 10.dp))
                    .offset(y = animateDp.value)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->
                            yOffset += pan.y
                            if (yOffset > 100 || yOffset <= -100) {
                                showAlertDialog.value = false
                            }
                        }
                    },
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(6.dp))
                    ) {
                        IconButton(
                            onClick = {
                                showAlertDialog.value = false
                            },
                            Modifier.align(
                                Alignment.TopStart
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Close Icon",
                            )
                        }
                        Text(
                            text = "010-Media-010",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(
                                Alignment.Center
                            )
                        )
                    }

                    ElevatedCard(
                        Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(10.dp)),
                        shape = MaterialTheme.shapes.extraLarge,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                .clickable { }
                                .padding(PaddingValues(10.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = profile_image,
                                contentDescription = "",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .clickable {

                                    },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "User Name",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Email@xyz.com",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Outlined.ArrowDropDownCircle,
                                contentDescription = "",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .padding(PaddingValues(10.dp))
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onSurface,
                                            MaterialTheme.shapes.large
                                        )
                                        .clip(shape = MaterialTheme.shapes.large)
                                        .clickable {

                                        }
                                ) {
                                    Text(
                                        text = "Manage your Account",
                                        modifier = Modifier.padding(
                                            PaddingValues(8.dp)
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(12.dp))
                    ) {
                        DialogItem(icon = Icons.Outlined.Settings, title = "Photos settings") {
                            showAlertDialog.value = false
                            navHostController.navigate(Screens.SettingsScreen.route) {
                                launchSingleTop = true
                            }
                        }
                        DialogItem(
                            icon = Icons.Outlined.DataExploration,
                            title = "Your data in Photos"
                        ) {

                        }
                        DialogItem(icon = Icons.AutoMirrored.Outlined.Help, title = "help") {

                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DialogItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        Modifier
            .padding(PaddingValues(8.dp))
            .clickable(interactionSource, indication = null) {
                onClick()
            }
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}


fun getTotalMemorySize(
    photosViewModel: PhotosViewModel,
    audioViewModel: AudioViewModel,
    videoViewModel: VideoViewModel,
    context: Context
): String {
    val totalSelectedImages = photosViewModel.selectedImages.value
    val totalSelectedAudios = audioViewModel.selectedAudios.value
    return if (totalSelectedImages > 0) {
        photosViewModel.getSelectedMemorySize(context)
    } else if (totalSelectedAudios > 0) {
        audioViewModel.getSelectedMemorySize(context)
    } else {
        videoViewModel.getSelectedMemorySize(context)
    }
}


fun getTotalSelected(
    audioSelectionInProgress: Boolean,
    totalSelectedAudios: Int,
    photosSelectionInProgress: Boolean,
    totalSelectedImages: Int,
    totalSelectedVideos: Int
) =
    if (audioSelectionInProgress) totalSelectedAudios else if (photosSelectionInProgress) totalSelectedImages else totalSelectedVideos


fun unSelectAllSelected(
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel
): () -> Unit = {
    if (audioSelectionInProgress) {
        audioViewModel.unSelectAllAudio()
    } else if (videoSelectionInProgress) {
        videoViewModel.unSelectAllVideos()
    } else {
        photosViewModel.unSelectAllImages()
    }
}


fun onClickDeleteButton(
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel,
    context: Context,
    trashLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    confirmDelete: MutableState<Boolean>
) {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && Build.VERSION.SDK_INT < Build.VERSION_CODES.R)){
        confirmDelete.value = true
    } else {
        if (audioSelectionInProgress) {
            audioViewModel.moveToTrashSelectedAudios(context, trashLauncher)
        } else if (videoSelectionInProgress) {
            videoViewModel.moveToTrashSelectedVideos(context, trashLauncher)
        } else {
            photosViewModel.moveToTrashSelectedImages(context, trashLauncher)
        }
    }
}

fun onClickShareButton(
    audioSelectionInProgress: Boolean,
    audioViewModel: AudioViewModel,
    videoSelectionInProgress: Boolean,
    videoViewModel: VideoViewModel,
    photosViewModel: PhotosViewModel,
    context: Context,
    onClickUnSelectAll: () -> Unit
) {
    if (audioSelectionInProgress) {
        val shareIntent = audioViewModel.shareSelectedAudios()
        onClickUnSelectAll()
        context.startActivity(Intent.createChooser(shareIntent, "Share Audios"))
    } else if (videoSelectionInProgress) {
        val shareIntent = videoViewModel.shareSelectedVideos()
        onClickUnSelectAll()
        context.startActivity(Intent.createChooser(shareIntent, "Share Videos"))
    } else {
        val shareIntent = photosViewModel.shareSelectedImages()
        onClickUnSelectAll()
        context.startActivity(Intent.createChooser(shareIntent, "Share Photos"))
    }
}
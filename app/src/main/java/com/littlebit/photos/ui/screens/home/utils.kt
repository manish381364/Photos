package com.littlebit.photos.ui.screens.home


import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ArrowDropDownCircle
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DataExploration
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageItem(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    onImageClick: (Uri) -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop,
    isFavorite: Boolean = false,
    showFavorite: Boolean = false,
    onClickFavorite: () -> Unit = {}
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = imageUri,
            contentDescription = null,
            modifier = modifier
                .size(141.dp)
                .padding(1.dp)
                .clickable {
                    onImageClick(imageUri)
                },
            contentScale = contentScale,
            transition = CrossFade
        )
        if (showFavorite) {
            var isFav by rememberSaveable {
                mutableStateOf(isFavorite)
            }
            val animateColor = animateColorAsState(
                targetValue = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurface,
                label = "Favorite Icon Color"
            )
            IconButton(
                onClick = {
                    onClickFavorite()
                    isFav = !isFav
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = animateColor.value),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite Icon"
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageGroupSection(
    imageGroup: ImageGroup,
    imageIndex: MutableState<Int>,
    onImageClick: (index: Int) -> Unit
) {
    // Show date or any other header for the group if desired
    Column(
        Modifier.fillMaxWidth(1f)
    ) {
        DateWithMarkButton(date = imageGroup.date)
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
            Modifier.fillMaxWidth(1f),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            imageGroup.images.forEachIndexed { index, image ->
                ImageItem(
                    modifier = Modifier.weight(1f),
                    imageUri = image.uri!!,
                    {
                        imageIndex.value = index
                        onImageClick(index)
                    },
                    ContentScale.Crop,
                    isFavorite = image.isFavorite,
                ) {
                    image.isFavorite = !image.isFavorite
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ImageGridList(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    scrollState: LazyListState
) {
    var imageGroups by remember {
        mutableStateOf(listOf<ImageGroup>())
    }
    photosViewModel.photoGroups.collectAsState().apply {
        imageGroups = value
    }

    val imageIndex = remember {
        mutableIntStateOf(0)
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxSize(),
        state = scrollState
    ) {
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
        items(imageGroups.size) { index ->
            val imageGroup = imageGroups[index]
            ImageGroupSection(imageGroup, imageIndex) { clickedImageIndex ->
                navHostController.navigate(Screens.ImageDetailsScreen.route + "/${clickedImageIndex}/${index}") {
                    launchSingleTop = true
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (imageGroups.isEmpty()) {
        val showLoading = photosViewModel.isLoading.collectAsState()
        var notFound by rememberSaveable {
            mutableStateOf(false)
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(PaddingValues(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (notFound) {
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
            }
            if (showLoading.value) {
                CircularProgressIndicator(
                    Modifier
                        .align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        LaunchedEffect(Unit) {
            delay(500)
            if (!showLoading.value && imageGroups.isEmpty()) {
                notFound = true
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateWithMarkButton(
    modifier: Modifier = Modifier,
    date: String = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM")).toString()
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
        IconButton(onClick = {

        }) {
            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = "Check Circle")
        }
    }
}


const val profile_image =
    "https://w0.peakpx.com/wallpaper/343/852/HD-wallpaper-iphone-planet-amoled-apple-galaxy-gold-life-space-strange-ultra.jpg"


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenTopBar(
    modifier: Modifier = Modifier,
    showAlertDialog: MutableState<Boolean>,
    photosViewModel: PhotosViewModel,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val isImageLoading = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        title = {
            Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bit Photos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                AsyncImage(
                    model = profile_image,
                    contentDescription = "",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .align(Alignment.BottomEnd)
                        .clickable {
                            showAlertDialog.value = true
                            photosViewModel.refresh(context)
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
                androidx.compose.animation.AnimatedVisibility(visible = isImageLoading.value) {
                    CircularProgressIndicator(
                        Modifier
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
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
    val activeColor = MaterialTheme.colorScheme.primary.copy(0.6f)
    val coroutineScope = rememberCoroutineScope()
    BottomAppBar(
        modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        BottomAppBarDefaults.containerColor.copy(0.3f),
                        BottomAppBarDefaults.containerColor.copy(0.5f),
                        BottomAppBarDefaults.containerColor.copy(0.7f),
                        BottomAppBarDefaults.containerColor.copy(0.9f),
                        BottomAppBarDefaults.containerColor
                    )
                ),
                shape = RoundedCornerShape(topStart = 90.dp, topEnd = 90.dp)
            ),
        containerColor = Color.Transparent
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                if (imageScreenListState.firstVisibleItemIndex > 2 && currentScreen.value == Screens.HomeScreen.route) {
                    coroutineScope.launch {
                        imageScreenListState.animateScrollToItem(0)
                    }
                } else currentScreen.value = Screens.HomeScreen.route
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = if (currentScreen.value == Screens.HomeScreen.route) activeColor else Color.Transparent)
        ) {
            Icon(imageVector = Icons.Filled.Image, contentDescription = "Image Icon")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                keyboardController?.show()
                currentScreen.value = Screens.SearchScreen.route
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = if (currentScreen.value == Screens.SearchScreen.route) activeColor else Color.Transparent)
        ) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = {
                if (imageScreenListState.firstVisibleItemIndex > 2 && currentScreen.value == Screens.AudioListScreen.route)
                    coroutineScope.launch {
                        audioScreenListState.animateScrollToItem(0)
                    }
                else currentScreen.value = Screens.AudioListScreen.route
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = if (currentScreen.value == Screens.AudioListScreen.route) activeColor else Color.Transparent)
        ) {
            val icon = Icons.Outlined.Audiotrack
            Icon(imageVector = icon, contentDescription = "Audio Track Icon")
        }
        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = {
                if (videoScreenListState.firstVisibleItemIndex > 2 && currentScreen.value == Screens.VideoGridScreen.route)
                    coroutineScope.launch {
                        videoScreenListState.animateScrollToItem(0)
                    }
                else currentScreen.value = Screens.VideoGridScreen.route
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = if (currentScreen.value == Screens.VideoGridScreen.route) activeColor else Color.Transparent),
        ) {
            Icon(
                imageVector = Icons.Outlined.VideoCameraBack,
                contentDescription = "Video Camera Icon"
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
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
                            text = "010-Bit-010",
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





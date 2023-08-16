package com.littlebit.photos.ui.screens.home


import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.VideoCameraBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ImageItem(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    onImageClick: (Uri) -> Unit = {},
    contentScale: ContentScale = ContentScale.Crop
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        val imagePainter = // Enable crossroad animation
            rememberAsyncImagePainter(
                ImageRequest // Add any image transformations
                    // Enable disk caching
                    // Enable memory caching
                    .Builder(LocalContext.current).data(data = imageUri)
                    .apply(block = fun ImageRequest.Builder.() {
                        crossfade(true) // Enable crossroad animation
//                        transformations(RoundedCornersTransformation(8f)) // Add any image transformations
                        diskCachePolicy(CachePolicy.ENABLED) // Enable disk caching
                        memoryCachePolicy(CachePolicy.ENABLED) // Enable memory caching
                    }).build()
            )
        Image(
            painter = imagePainter,
            contentDescription = null,
            modifier = modifier
                .size(141.dp)
                .padding(1.dp)
                .clickable {
                    onImageClick(imageUri)
                },
            contentScale = contentScale,
        )

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
                ImageItem(modifier = Modifier.weight(1f), imageUri = image, {
                    imageIndex.value = index
                    onImageClick(index)
                }, ContentScale.Crop)
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

    val state: MutableTransitionState<Boolean> =
        MutableTransitionState<Boolean>(true)
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BottomAppBarDefaults.containerColor.copy(0.7f),
            scrolledContainerColor = BottomAppBarDefaults.containerColor.copy(0.7f),

        ),
        title = {
            AnimatedVisibility(
                visibleState = state,
                enter = slideInVertically(),
                exit = slideOutVertically()
            ) {

                Row(
                    modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    BottomAppBarDefaults.containerColor,
                                    BottomAppBarDefaults.containerColor.copy(0.7f),
                                    BottomAppBarDefaults.containerColor.copy(
                                        0.4f
                                    )
                                )
                            )
                        )
                        .padding(PaddingValues(10.dp)),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "Bit Photos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        AsyncImage(
                            model = profile_image,
                            contentDescription = "",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    showAlertDialog.value = true
                                    photosViewModel.loadMedia(context)
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
) {
    BottomAppBar(
        modifier
            .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
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
            ),
        containerColor = Color.Transparent
    ) {
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { currentScreen.value = Screens.HomeScreen.route }) {
            Icon(imageVector = Icons.Filled.Image, contentDescription = "Image Icon")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search Icon")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Outlined.Group, contentDescription = "Sharing Icon")
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {
            currentScreen.value = Screens.VideoGridScreen.route
        }) {
            Icon(
                imageVector = Icons.Outlined.VideoCameraBack,
                contentDescription = "Video Camera Icon"
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun FloatingProfileDialog(showAlertDialog: MutableState<Boolean>) {
    if (showAlertDialog.value) {
        var yOffset by remember { mutableFloatStateOf(0f) }
        val animateDp = animateDpAsState(targetValue = yOffset.dp, label = "yOffset Animation")

        Dialog(
            onDismissRequest = {
                showAlertDialog.value = false
            },
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = animateDp.value)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->
                            yOffset += pan.y
                            if (yOffset > 100 || yOffset <= -100) {
                                showAlertDialog.value = false
                            }
                        }
                    },
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.elevatedCardElevation()
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(PaddingValues(6.dp))
                    ) {
                        IconButton(
                            onClick = { /*TODO*/ },
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

                }
            }
        }
    }
}





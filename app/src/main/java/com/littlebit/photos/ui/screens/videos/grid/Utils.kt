package com.littlebit.photos.ui.screens.videos.grid

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.home.DateWithMarkButton
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import kotlin.reflect.KProperty


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreenTopBar(
    topAppBarScrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        scrollBehavior = topAppBarScrollBehavior,
        title = {
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Bit Videos",
                    style =  MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    )
}

private operator fun Any.getValue(nothing: Nothing?, property: KProperty<*>): Any {
    TODO("Not yet implemented")
}


@OptIn(ExperimentalLayoutApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun VideoGridList(
    videoViewModel: VideoViewModel,
    scrollState: LazyListState,
    navHostController: NavHostController
) {
    val videoGroups = videoViewModel.videoGroups.collectAsState()
    val isLoading = videoViewModel.isLoading.collectAsState()
    if (videoGroups.value.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            if(isLoading.value){
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurface)
            }
            else
                Text(text = "No Videos Found", style = MaterialTheme.typography.titleLarge)
        }
    } else {
        LazyColumn(state = scrollState) {
            videoGroups.value.forEachIndexed { groupIndex, videoGroup ->
                item {
                    Column {
                        DateWithMarkButton(date = videoGroup.date)
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(Modifier.fillMaxWidth()) {
                            videoGroup.videos.forEachIndexed { index, video ->
                                VideoGridItem(
                                    video
                                ) {
                                    navHostController.navigate(Screens.VideoScreen.route + "/$index/$groupIndex")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun VideoGridItem(
    video: VideoItem,
    onClick: () -> Unit,
) {
    val width = if (isLandscape()) screenWidthInDp() / 4 else screenWidthInDp() / 2
    val height = if (isLandscape()) screenWidthInDp() / 4 else screenWidthInDp() / 2
    if (video.uri != null) {
        Box(contentAlignment = Alignment.Center) {
            GlideImage(
                model = video.thumbnail,
                contentDescription = "Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(width)
                    .height(height)
                    .padding(PaddingValues(2.dp))
                    .clickable { onClick() },
                transition = CrossFade
            )
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                Text(
                    text = video.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier
                        .width(width - 58.dp)
                )
                IconButton(
                    onClick = {
                        onClick()
                    },
                    modifier = Modifier
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "Play",
                    )
                }
            }

            Text(
                text = video.size, modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}


@Composable
fun screenWidthInDp(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
}


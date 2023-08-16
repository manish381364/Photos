package com.littlebit.photos.ui.previews

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.littlebit.photos.ui.screens.home.DateWithMarkButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview(showBackground = true)
fun HomeScreenTopBarPreview() {
//    HomeScreenTopBar(modifier = Modifier.Companion.align(Alignment.TopCenter))
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
@Preview(showBackground = true)
fun DateWithMarkButtonPreview() {
    DateWithMarkButton()
}


@Preview(showBackground = true)
@Composable
fun FlowRowTestPreview() {

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {

        for (image in (0..100)) {
            item {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    val imagePainter = // Enable crossroad animation
                        rememberAsyncImagePainter(
                            ImageRequest // Add any image transformations
                                // Enable disk caching
                                // Enable memory caching
                                .Builder(LocalContext.current)
                                .data(data = "https://picsum.photos/200/300")
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
                        modifier = Modifier
                            .size(141.dp)
                            .padding(1.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }

}
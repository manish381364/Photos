package com.littlebit.photos.model

import android.graphics.Bitmap
import android.net.Uri


data class ImageGroup(
    val date: String,
    val images: MutableList<PhotoItem>,
)


data class VideoGroup(
    val date: String,
    val videos: MutableList<VideoItem>,
)

data class VideoItem(
    val id: Long,
    val displayName: String,
    val dateAdded: Long,
    val uri: Uri? = null,
    val thumbnail: Bitmap? = null,
    val size: String,
    val isFavorite: Boolean = false
)


data class PhotoItem(
    val id: Long,
    val displayName: String,
    val dateAdded: Long,
    val uri: Uri? = null,
    val size: String,
    var isFavorite: Boolean = false
)

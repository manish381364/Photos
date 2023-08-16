package com.littlebit.photos.model

import android.graphics.Bitmap
import android.net.Uri

data class ImageGroup(val date: String, val images: MutableList<Uri>)
data class VideoGroup(val date: String, val videos: MutableList<VideoItem>)
data class VideoItem(
    val id: Long,
    val displayName: String,
    val dateAdded: Long,
    val uri: Uri? = null,
    val thumbnail: Bitmap? = null,
    val size: String
)
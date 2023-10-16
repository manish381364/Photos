package com.littlebit.photos.model

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


data class PhotoItem(
    val id: Long,
    val displayName: String,
    val dateAdded: Long,
    val uri: Uri?,
    val size: Long,
    var isSelected: Boolean = false,
)

data class ImageGroup(
     val date: String,
     val images: MutableList<PhotoItem>
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
    val size: Long,
    var isSelected: Boolean = false,
    val duration: Long
)

data class AudioItem(
    val id: Long,
    val displayName: String,
    val path: String,
    val duration: String,
    val size: Long,
    val isMusic: Boolean,
    val uri: Uri,
    val thumbNail: ImageBitmap? = null,
    val dateAdded: String = "",
    var isSelected: MutableState<Boolean> = mutableStateOf(false)
)

data class SearchItem(
    val title: String,
    val type: String,
    val url: Uri?,
    val videoItem: VideoItem? = null,
    val photoItem: PhotoItem? = null,
    val audioItem: AudioItem? = null,
    val listIndex: Int? = null,
    val index: Int? = null
)

@Entity(tableName = "theme_preference")
data class ThemePreference(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val isDarkTheme: Boolean
)

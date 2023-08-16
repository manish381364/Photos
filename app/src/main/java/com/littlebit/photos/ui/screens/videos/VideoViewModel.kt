package com.littlebit.photos.ui.screens.videos

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow


class VideoViewModel : ViewModel() {
    val videos = MutableStateFlow<List<VideoItem>>(listOf())
    val videoGroups = MutableStateFlow<List<VideoGroup>>(listOf())

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun loadVideos(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            videos.value = fetchVideoGroups(context)
        }
    }

    private fun formatDate(dateAdded: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return sdf.format(Date(dateAdded * 1000))
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchVideoGroups(context: Context): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        val contentResolver = context.contentResolver
        val query = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateAddedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            val videoList = mutableListOf<VideoItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val size = cursor.getLong(sizeColumn)
                videoList.add(VideoItem(id, displayName, dateAdded, contentUri, contentResolver.loadThumbnail(contentUri, Size(640, 480), null), formatFileSize(size)))
            }
            val groupedVideos = videoList.groupBy {
                formatDate(it.dateAdded)
            }
            val sortedVideoGroup = groupedVideos.map { (date, videos) ->
                VideoGroup(date, videos.toMutableList())
            }
            videoGroups.value = sortedVideoGroup
            videos.addAll(videoList)
        }
        return videos
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "N/A"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return String.format("%.2f %s", size / 1024.0.pow(digitGroups.toDouble()), units[digitGroups])
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun refreshVideos(context: Context) {
        loadVideos(context)
        fetchVideoGroups(context)
    }

}


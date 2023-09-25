package com.littlebit.photos.model.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.ui.screens.audio.Audio
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.log10
import kotlin.math.pow

class MediaRepository {
    fun getImageDateAdded(contentResolver: ContentResolver, imageUri: Uri): Long {
        val projection = arrayOf(MediaStore.Images.Media.DATE_ADDED)
        contentResolver.query(imageUri, projection, null, null, null)?.use { cursor ->
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            if (cursor.moveToFirst()) {
                return cursor.getLong(dateAddedColumn)
            }
        }

        return 0L
    }

    fun getImagesGroupedByDate(
        contentResolver: ContentResolver,
        isLoading: MutableStateFlow<Boolean>
    ): Pair<MutableList<ImageGroup>, MutableList<PhotoItem>> {
        isLoading.value = true
        val photosList = mutableListOf<PhotoItem>()
        val imagesGroupedByDate: MutableList<ImageGroup>
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val photoItem = PhotoItem(id, displayName, dateAdded, contentUri, size.toString())
                photosList.add(photoItem)
            }
        }

        val groupedImages = photosList.groupBy {
            val dateAdded = getImageDateAdded(contentResolver, it.uri!!)
            formatDate(dateAdded)
        }
        val sortedList = groupedImages.map { (date, imagesForDate) ->
            ImageGroup(date, imagesForDate.toMutableList())
        }
        imagesGroupedByDate = sortedList.toMutableList()
        isLoading.value = false
        return Pair(imagesGroupedByDate, photosList)
    }


    fun addImagesGroupedByDate(
        contentResolver: ContentResolver,
        photoGroups: MutableStateFlow<MutableList<ImageGroup>>,
        photos: MutableStateFlow<MutableList<PhotoItem>>,
        isLoading: MutableStateFlow<Boolean>
    ) {
        isLoading.value = true
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val photoItem = PhotoItem(id, displayName, dateAdded, contentUri, size.toString())
                photos.value.add(photoItem)
                if (photoGroups.value.isNotEmpty()) {
                    if (photoGroups.value[photoGroups.value.size - 1].date != formatDate(dateAdded))
                        photoGroups.value.add(
                            ImageGroup(
                                formatDate(dateAdded),
                                mutableListOf(photoItem)
                            )
                        )
                    else
                        photoGroups.value[photoGroups.value.size - 1].images.add(photoItem)
                } else {
                    photoGroups.value.add(
                        ImageGroup(
                            formatDate(dateAdded),
                            mutableListOf(photoItem)
                        )
                    )
                }
                if (photoGroups.value.size > 1) {
                    isLoading.value = false
                }
            }
        }
    }


    //Audio Repository
    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAudioList(context: Context): MutableList<Audio> {
        val list = mutableListOf<Audio>()
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val isMusicColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getString(durationColumn)
                val isMusic = cursor.getInt(isMusicColumn)
                val path = cursor.getString(pathColumn)
                val uri = Uri.withAppendedPath(queryUri, id.toString())
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                var thumbnailBitmap: ImageBitmap? = null
                try {
                    val thumbnailBytes = retriever.embeddedPicture
                    Log.d("EMBEDDED_PICTURE", "getAudioList: IN TRY")
                    thumbnailBitmap = thumbnailBytes?.let {
                        loadThumbnailImage(it)
                    }
                } catch (e: Exception) {
                    // Handle the exception, e.g., log it or show an error message
                    Log.d("EMBEDDED_PICTURE", "getAudioList: ${e.message}")
                } finally {
                    retriever.release()
                }

                val audio = Audio(
                    id,
                    displayName,
                    path,
                    formatDuration(duration.toLong()),
                    formatFileSize(size),
                    isMusic == 1,
                    uri,
                    thumbnailBitmap,
                    formatAddedTime(dateAdded)
                )
                list.add(audio)
            }
        }
        return list
    }

    private fun loadThumbnailImage(thumbnailBytes: ByteArray): ImageBitmap? {
        val bitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.size)
        return bitmap?.asImageBitmap()
    }

    private fun formatFileSize(size: Long): String {
        if (size <= 0) return "N/A"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            "%.2f %s",
            size / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    private fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        if (hours > 0)
            return String.format("%02d h : %02d m : %02d s", hours, minutes, seconds)
        return String.format("%02d m : %02d s", minutes, seconds)
    }

    private fun formatAddedTime(dateAddedMillis: Long): String {
        val currentTimeMillis = System.currentTimeMillis()
        val timeDifferenceMillis = currentTimeMillis - (dateAddedMillis * 1000)

        val seconds = timeDifferenceMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days == 0L -> when (hours) {
                0L -> when (minutes) {
                    0L -> when {
                        seconds < 5 -> "just now"
                        seconds < 60 -> "$seconds seconds ago"
                        else -> "minute ago"
                    }

                    else -> if (minutes > 1) "$minutes minutes ago" else "$minutes minute ago"
                }

                else -> if (hours > 1) "$hours hours ago" else "$hours hour ago"
            }

            days == 1L -> "yesterday"
            days < 7 -> "$days days ago"
            else -> {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.format(Date(dateAddedMillis * 1000))
            }
        }
    }


    // Video Repository

    fun formatDate(dateAdded: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return sdf.format(Date(dateAdded * 1000))
    }


    fun loadVideos(context: Context, isLoading: MutableStateFlow<Boolean>): Pair<MutableList<VideoItem>, MutableList<VideoGroup>> {
        isLoading.value = true
        val videos = mutableListOf<VideoItem>()
        var videoGroup = mutableListOf<VideoGroup>()
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
                val thumbnail = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentResolver.loadThumbnail(contentUri, Size(640, 480), null)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
                videoList.add(
                    VideoItem(
                        id,
                        displayName,
                        dateAdded,
                        contentUri,
                        thumbnail,
                        formatFileSize(size)
                    )
                )
            }
            val groupedVideos = videoList.groupBy {
                formatDate(it.dateAdded)
            }
            val sortedVideoGroup = groupedVideos.map { (date, videos) ->
                VideoGroup(date, videos.toMutableList())
            }
            videoGroup = sortedVideoGroup.toMutableList()
            videos.addAll(videoList)
        }
        isLoading.value = false
        return Pair(videos, videoGroup)
    }


    fun addVideoGroups(
        context: Context,
        videoGroups: MutableStateFlow<MutableList<VideoGroup>>,
        isLoading: MutableStateFlow<Boolean>
    ){
        isLoading.value = true
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
                val thumbnail = try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentResolver.loadThumbnail(contentUri, Size(640, 480), null)
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
                val item = VideoItem(
                    id,
                    displayName,
                    dateAdded,
                    contentUri,
                    thumbnail,
                    formatFileSize(size)
                )
                videoList.add(item)

                if (videoGroups.value.isEmpty()) videoGroups.value.add(VideoGroup(formatDate(dateAdded), mutableListOf(item)))
                else{
                    if(videoGroups.value[videoGroups.value.size - 1].date != formatDate(dateAdded)){
                        videoGroups.value.add(VideoGroup(formatDate(dateAdded), mutableListOf(item)))
                    }
                    else
                        videoGroups.value[videoGroups.value.size - 1].videos.add(item)
                }
                if(videoGroups.value.size > 1){
                    isLoading.value = false
                }
            }
        }
    }
}
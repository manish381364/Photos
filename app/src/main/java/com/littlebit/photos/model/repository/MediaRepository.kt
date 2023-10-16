package com.littlebit.photos.model.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.littlebit.photos.model.AudioItem
import com.littlebit.photos.model.ImageGroup
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.VideoGroup
import com.littlebit.photos.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class MediaRepository(
     private val applicationContext: Context
) {
    private fun getImageDateAdded(contentResolver: ContentResolver, imageUri: Uri): Long {
        val projection = arrayOf(MediaStore.Images.Media.DATE_MODIFIED)
        contentResolver.query(imageUri, projection, null, null, null)?.use { cursor ->
            val dateAddedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            if (cursor.moveToFirst()) {
                return cursor.getLong(dateAddedColumn)
            }
        }

        return 0L
    }

    fun getImagesGroupedByDate(
        contentResolver: ContentResolver = applicationContext.contentResolver,
        isLoading: MutableStateFlow<Boolean>
    ): Pair<MutableList<ImageGroup>, MutableList<PhotoItem>> {
        isLoading.value = true
        val photosList = mutableListOf<PhotoItem>()
        val imagesGroupedByDate: MutableList<ImageGroup>
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val photoItem = PhotoItem(id, displayName, dateAdded, contentUri, size)
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
        contentResolver: ContentResolver = applicationContext.contentResolver,
        photoGroups: MutableStateFlow<MutableList<ImageGroup>>,
        photos: MutableStateFlow<MutableList<PhotoItem>>,
        isLoading: MutableStateFlow<Boolean>
    ) {
        isLoading.value = true
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)


            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val photoItem = PhotoItem(id, displayName, dateAdded, contentUri, size)

                photos.value.add(photoItem)

                val formattedDate = formatDate(dateAdded) // Use formatDate function here

                if (photoGroups.value.isNotEmpty() &&
                    photoGroups.value.last().date != formattedDate
                ) {
                    photoGroups.value.add(
                        ImageGroup(
                            formattedDate,
                            mutableListOf(photoItem)
                        )
                    )
                } else if (photoGroups.value.isNotEmpty()) {
                    photoGroups.value.last().images.add(photoItem)
                } else {
                    photoGroups.value.add(
                        ImageGroup(
                            formattedDate,
                            mutableListOf(photoItem)
                        )
                    )
                }

                if (photoGroups.value.size > 1) {
                    isLoading.value = false
                }
            }
        }
        isLoading.value = false
    }


    //Audio Repository

    fun getAudioList(context: Context = applicationContext): MutableList<AudioItem> {
        val list = mutableListOf<AudioItem>()
        val contentResolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
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

                val audio = AudioItem(
                    id,
                    displayName,
                    path,
                    formatDuration(duration.toLong()),
                    duration.toLong(),
                    size,
                    isMusic = isMusic == 1,
                    uri = uri,
                    thumbNail = thumbnailBitmap,
                    dateAdded = formatDate(dateAdded),
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


    private fun formatDuration(duration: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        if (hours > 0)
            return String.format("%02d h : %02d m : %02d s", hours, minutes, seconds)
        return String.format("%02d m : %02d s", minutes, seconds)
    }


    // Video Repository

    private fun formatDate(dateAdded: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return sdf.format(Date(dateAdded * 1000))
    }


    fun loadVideos(
        context: Context = applicationContext,
        isLoading: MutableStateFlow<Boolean>
    ): Pair<MutableList<VideoItem>, MutableList<VideoGroup>> {
        isLoading.value = true
        val videos = mutableListOf<VideoItem>()
        var videoGroup = mutableListOf<VideoGroup>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
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
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            val videoList = mutableListOf<VideoItem>()
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Android 10 (API level 29) and above, you can use loadThumbnail
                    try {
                        contentResolver.loadThumbnail(
                            contentUri,
                            Size(640, 480),
                            null
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    // For lower Android versions, use MediaStore.Video.Thumbnails
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, contentUri)
                        val frame = retriever.frameAtTime
                        if (frame != null) {
                            // Resize the bitmap to a smaller dimension to avoid OutOfMemoryError
                            val targetWidth = 300
                            val targetHeight = 200
                            Bitmap.createScaledBitmap(frame, targetWidth, targetHeight, true)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    } finally {
                        retriever.release()
                    }
                }
                videoList.add(
                    VideoItem(
                        id,
                        displayName,
                        dateAdded,
                        contentUri,
                        thumbnail,
                        size,
                        duration = duration
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


    fun addVideosGroupedByDate(
        videoGroups: MutableStateFlow<MutableList<VideoGroup>>,
        videos: MutableStateFlow<MutableList<VideoItem>>,
        isLoading: MutableStateFlow<Boolean>,
        context: Context = applicationContext
    ) {
        val contentResolver = context.contentResolver
        isLoading.value = true
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)


            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                val displayName = cursor.getString(displayNameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val size = cursor.getLong(sizeColumn)
                val duration = cursor.getLong(durationColumn)
                val thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // For Android 10 (API level 29) and above, you can use loadThumbnail
                    try {
                        contentResolver.loadThumbnail(
                            contentUri,
                            Size(640, 480),
                            null
                        )
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    // For lower Android versions, use MediaStore.Video.Thumbnails
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, contentUri)
                        val frame = retriever.frameAtTime
                        if (frame != null) {
                            // Resize the bitmap to a smaller dimension to avoid OutOfMemoryError
                            val targetWidth = 300
                            val targetHeight = 200
                            Bitmap.createScaledBitmap(frame, targetWidth, targetHeight, true)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    } finally {
                        retriever.release()
                    }
                }

                val videoItem = VideoItem(id, displayName, dateAdded, contentUri, thumbnail, size, duration = duration)

                videos.value.add(videoItem)

                val formattedDate = formatDate(dateAdded) // Use formatDate function here

                if (videoGroups.value.isNotEmpty() &&
                    videoGroups.value.last().date != formattedDate
                ) {
                    videoGroups.value.add(
                        VideoGroup(
                            formattedDate,
                            mutableListOf(videoItem)
                        )
                    )
                } else if (videoGroups.value.isNotEmpty()) {
                    videoGroups.value.last().videos.add(videoItem)
                } else {
                    videoGroups.value.add(
                        VideoGroup(
                            formattedDate,
                            mutableListOf(videoItem)
                        )
                    )
                    isLoading.value = false
                }
            }
        }
        isLoading.value = false
    }
}
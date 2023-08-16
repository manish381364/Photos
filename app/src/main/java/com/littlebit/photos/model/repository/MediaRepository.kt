package com.littlebit.photos.model.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.littlebit.photos.model.ImageGroup
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaRepository {
    private var imagesGroupedByDate = mutableListOf<ImageGroup>()
    private fun getImageDateAdded(contentResolver: ContentResolver, imageUri: Uri): Long {
        val projection = arrayOf(MediaStore.Images.Media.DATE_ADDED)
        contentResolver.query(imageUri, projection, null, null, null)?.use { cursor ->
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            if (cursor.moveToFirst()) {
                return cursor.getLong(dateAddedColumn)
            }
        }

        return 0L
    }
    private fun formatDate(dateAdded: Long): String {
        val sdf = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
        return sdf.format(Date(dateAdded * 1000))
    }

    fun getImagesGroupedByDate(contentResolver: ContentResolver): List<ImageGroup> {
        val images = mutableListOf<Uri>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        contentResolver.query(queryUri, projection, null, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri = Uri.withAppendedPath(queryUri, id.toString())
                images.add(contentUri)
            }
        }

        val groupedImages = images.groupBy {
            val dateAdded = getImageDateAdded(contentResolver, it)
            formatDate(dateAdded)
        }
        val sortedList = groupedImages.map { (date, imagesForDate) ->
            ImageGroup(date, imagesForDate.toMutableList())
        }
        imagesGroupedByDate.addAll(sortedList)
        return sortedList
    }
}
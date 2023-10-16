package com.littlebit.photos.model

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
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
    val id: Long = 0L,
    val displayName: String = "Sample song",
    val path: String = "path to some where",
    val duration: String = "00:00",
    val durationLong: Long = 0L,
    val size: Long = 0L,
    val isMusic: Boolean = true,
    val uri: Uri = Uri.EMPTY,
    val thumbNail: ImageBitmap? = null,
    val dateAdded: String = "",
    var isSelected: Boolean = false,
    var isPlaying: Boolean = false,
    val artist: String = "Unknown"
)  : Parcelable {

    // Parcelable Creator
    @Suppress("DEPRECATION")
    companion object CREATOR : Parcelable.Creator<AudioItem> {
        override fun createFromParcel(parcel: Parcel): AudioItem {
            // Read the parcel and reconstruct the object
            return AudioItem(
                id = parcel.readLong(),
                displayName = parcel.readString() ?: "Sample song",
                path = parcel.readString() ?: "path to some where",
                duration = parcel.readString() ?: "00:00",
                durationLong = parcel.readLong(),
                size = parcel.readLong(),
                isMusic = parcel.readInt() == 1,
                uri = parcel.readParcelable(Uri::class.java.classLoader) ?: Uri.EMPTY,
                // Read other properties...
                isSelected = parcel.readInt() == 1,
                artist = parcel.readString() ?: "Unknown"
            )
        }

        override fun newArray(size: Int): Array<AudioItem?> {
            return arrayOfNulls(size)
        }
    }

    // Describe the contents of the Parcelable
    override fun describeContents(): Int {
        return 0
    }

    // Write object data to the parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(displayName)
        parcel.writeString(path)
        parcel.writeString(duration)
        parcel.writeLong(durationLong)
        parcel.writeLong(size)
        parcel.writeInt(if (isMusic) 1 else 0)
        parcel.writeParcelable(uri, flags)
        // Write other properties...
        parcel.writeInt(if (isSelected) 1 else 0)
        parcel.writeString(artist)
    }
}

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

package com.littlebit.photos.ui.screens.search

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.littlebit.photos.model.PhotoItem
import com.littlebit.photos.model.VideoItem
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModel : ViewModel() {

    fun getSearchItems(
        photosViewModel: PhotosViewModel,
        videoViewModel: VideoViewModel,
        inputText: String
    ): List<SearchItem> {
        val searchItems = MutableStateFlow(mutableListOf<SearchItem>())
        val photos = photosViewModel.photoGroups.value
        val videos = videoViewModel.videoGroups.value
        photos.forEachIndexed() { listIndex, imageGroup ->
            imageGroup.images.forEachIndexed() { index, photoItem ->
                if (photoItem.displayName.contains(inputText.trim(), ignoreCase = true)){
                    searchItems.value.add(
                        SearchItem(
                            photoItem.displayName.trim(),
                            "image",
                            photoItem.uri,
                            photoItem = photoItem,
                            listIndex = listIndex,
                            index = index
                        )
                    )
                }
            }
        }
        videos.forEachIndexed { listIndex, videoGroup ->
            videoGroup.videos.forEachIndexed() { index, videoItem ->
                if(videoItem.displayName.contains(inputText.trim(), ignoreCase = true)) {
                    searchItems.value.add(
                        SearchItem(
                            videoItem.displayName.trim(),
                            "video",
                            videoItem.uri,
                            videoItem = videoItem,
                            listIndex = listIndex,
                            index = index
                        )
                    )
                }
            }
        }
        return searchItems.value
    }

    data class SearchItem(
        val title: String,
        val type: String,
        val url: Uri?,
        val videoItem: VideoItem? = null,
        val photoItem: PhotoItem? = null,
        val listIndex: Int? = null,
        val index: Int? = null
    )

}
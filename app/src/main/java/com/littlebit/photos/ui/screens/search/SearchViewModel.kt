package com.littlebit.photos.ui.screens.search

import androidx.lifecycle.ViewModel
import com.littlebit.photos.model.SearchItem
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModel : ViewModel() {

    fun getSearchItems(
        photosViewModel: PhotosViewModel,
        videoViewModel: VideoViewModel,
        audioViewModel: AudioViewModel,
        inputText: String
    ): List<SearchItem> {

        val searchItems = MutableStateFlow(mutableListOf<SearchItem>())
        val photos = photosViewModel.photoGroups.value
        val videos = videoViewModel.videoGroups.value
        val audios = audioViewModel.audioList.value
        photos.forEachIndexed { listIndex, imageGroup ->
            imageGroup.images.forEachIndexed { index, photoItem ->
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
            videoGroup.videos.forEachIndexed { index, videoItem ->
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
        audios.forEachIndexed { index, audioItem ->
            if(audioItem.displayName.contains(inputText.trim(), ignoreCase = true)) {
                searchItems.value.add(
                    SearchItem(
                        audioItem.displayName.trim(),
                        "audio",
                        audioItem.uri,
                        audioItem = audioItem,
                        index = index
                    )
                )
            }
        }
        return searchItems.value
    }



}
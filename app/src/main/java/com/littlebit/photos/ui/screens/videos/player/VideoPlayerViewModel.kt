package com.littlebit.photos.ui.screens.videos.player

import androidx.lifecycle.ViewModel


class VideoPlayerViewModel : ViewModel() {
    var currentUriIndex: Int = 0
    var isPlaying: Boolean = false
    var playbackPosition: Long = 0L
}






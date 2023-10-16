package com.littlebit.photos.ui.screens.audio.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOn
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.ShuffleOn
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavHostController
import com.littlebit.photos.ui.screens.audio.FileInfo
import com.littlebit.photos.ui.screens.videos.grid.isLandscape

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MarqueeText(text: String) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, end = 14.dp)
            .basicMarquee(animationMode = MarqueeAnimationMode.WhileFocused)
            .focusRequester(focusRequester)
            .focusable()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { focusRequester.requestFocus() },
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Visible
    )
}



@Composable
fun AudioThumbNail(
    audioThumbNail: ImageBitmap?,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp)
    ) {
        if (audioThumbNail == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (isLandscape()) 0.5f else 0.7f)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.White.copy(0.4f))
            ) {
                Icon(
                    imageVector = Icons.Outlined.Audiotrack,
                    contentDescription = "Audio Track",
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.Center),
                    tint = Color.Magenta.copy(0.7f)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (isLandscape()) 0.5f else 0.7f)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.White.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = audioThumbNail,
                    contentDescription = "Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                )
            }
        }
    }
}


@Composable
fun XPlayerTopBar(
    modifier: Modifier = Modifier,
    xAudioViewModel: XAudioViewModel,
    navController: NavHostController,
) {
    val showDropDownMenu = rememberSaveable {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    Row(
        modifier
            .fillMaxWidth()
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            navController.popBackStack()
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back Button"
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = {
            xAudioViewModel.shareIntent(context)
        }) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = "Share Button")
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = { showDropDownMenu.value = true }) {
            XPlayerTopBarMenu(
                showDropDownMenu,
                xAudioViewModel
            )
            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "More Options")
        }
    }

}


@Composable
fun XPlayerTopBarMenu(
    showDropDownMenu: MutableState<Boolean> = mutableStateOf(true),
    xAudioViewModel: XAudioViewModel
) {
    val context = LocalContext.current
    var showFileInfo by remember {
        mutableStateOf(false)
    }
    val menuItems = listOf(
        Pair("Open with") {
            xAudioViewModel.openWith(context)
            showDropDownMenu.value = false
        },
        Pair("File info") {
            showFileInfo = true
            showDropDownMenu.value = false
        },
        Pair("Playback speed") {}
    )

    DropdownMenu(
        expanded = showDropDownMenu.value,
        onDismissRequest = { showDropDownMenu.value = false },
        offset = DpOffset(0.dp, (-30).dp)
    ) {
        menuItems.forEach { menuItem ->
            DropdownMenuItem(
                text = { Text(text = menuItem.first) },
                onClick = { menuItem.second.invoke() },
                modifier = Modifier.padding(end = 50.dp, top = 5.dp)
            )
        }
    }

    AnimatedVisibility(
        showFileInfo,
        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(900)),
        exit = slideOutHorizontally()
    ) {
        Dialog(
            onDismissRequest = { showFileInfo = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                SecureFlagPolicy.Inherit,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = true
            )
        ) {
            FileInfo(currentFile = xAudioViewModel.currentSelectedAudio) {
                showFileInfo = false
            }
        }
    }
}


@Composable
fun ControlButtons(
    xAudioViewModel: XAudioViewModel,
) {
    val playPauseIcon =
        if (!xAudioViewModel.isPlaying) Icons.Filled.PlayCircle else Icons.Filled.PauseCircle
    val repeatIcon =
        if (xAudioViewModel.isListLooping) Icons.Outlined.RepeatOn else if (xAudioViewModel.isCurrentRepeat) Icons.Outlined.RepeatOne else Icons.Outlined.Repeat
    val shuffleIcon =
        if (xAudioViewModel.isShuffling) Icons.Outlined.ShuffleOn else Icons.Outlined.Shuffle
    Row(
        Modifier
            .fillMaxWidth()
            .padding(4.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = {
            xAudioViewModel.shuffle()
        }, modifier = Modifier.size(70.dp)) {
            Icon(
                imageVector = shuffleIcon,
                contentDescription = "Shuffle Button",
                modifier = Modifier.size(25.dp)
            )
        }

        IconButton(onClick = {
            xAudioViewModel.onUiEvents(UIEvents.SeekToPrevious)
        }, modifier = Modifier.size(50.dp)) {
            Icon(
                imageVector = Icons.Outlined.SkipPrevious,
                contentDescription = "Skip Previous",
                modifier = Modifier.size(45.dp)
            )
        }

        IconButton(
            onClick = {
                xAudioViewModel.onUiEvents(UIEvents.PlayPause)
            },
            modifier = Modifier.size(90.dp)
        ) {
            Icon(
                imageVector = playPauseIcon,
                contentDescription = "Play Pause",
                modifier = Modifier.size(60.dp)
            )
        }

        IconButton(onClick = {
            xAudioViewModel.onUiEvents(UIEvents.SeekToNext)
        }, modifier = Modifier.size(50.dp)) {
            Icon(
                imageVector = Icons.Outlined.SkipNext,
                contentDescription = "Skip Next",
                modifier = Modifier.size(40.dp)
            )
        }
        IconButton(onClick = {
            xAudioViewModel.repeat()
        }, modifier = Modifier.size(70.dp)) {
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat Button",
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressBar(
    progress: Float,
    timeDuration: String,
    currentTime: String,
    onValueChange: (Float) -> Unit,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
    ) {
        Text(text = currentTime, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.width(4.dp))
        Slider(
            value = if (progress in 0f..100f) progress else 0f,
            onValueChange = { onValueChange(it) },
            valueRange = 0f..100f,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource,
                    colors = SliderDefaults.colors(thumbColor = Color.White),
                    thumbSize = DpSize(17.dp, 17.dp)
                )
            },
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    colors = SliderDefaults.colors(activeTrackColor = Color.White),
                    modifier = Modifier.fillMaxWidth((0.89).toFloat())
                )
            },
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = timeDuration, style = MaterialTheme.typography.bodySmall)
    }
}
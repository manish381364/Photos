package com.littlebit.photos.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel

@Composable
fun SearchScreen(
    navHostController: NavHostController,
    searchViewModel: SearchViewModel = viewModel(),
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    currentScreen: MutableState<String>
) {
    val searchItems = rememberSaveable {
        mutableStateOf(listOf<SearchViewModel.SearchItem>())
    }
    val isInputFieldEmpty = remember {
        mutableStateOf(true)
    }
    Surface {
        Scaffold(
            topBar = {
                SearchTopBar(
                    searchViewModel,
                    photosViewModel,
                    videoViewModel,
                    searchItems,
                    currentScreen,
                    isInputFieldEmpty
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                SearchContent(searchItems, navHostController, isInputFieldEmpty)
            }
        }
    }
}


@Composable
fun SearchTopBar(
    searchViewModel: SearchViewModel,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    searchItems: MutableState<List<SearchViewModel.SearchItem>>,
    currentScreen: MutableState<String>,
    isInputFieldEmpty: MutableState<Boolean>
) {
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    LaunchedEffect(key1 = currentScreen.value == Screens.SearchScreen.route) {
        focusRequester.requestFocus()
    }
    val inputText = rememberSaveable {
        mutableStateOf("")
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(WindowInsets.systemBars.asPaddingValues()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        OutlinedTextField(
            value = inputText.value,
            onValueChange = {
                inputText.value = it
                if (it.isNotEmpty()) {
                    searchItems.value = searchViewModel.getSearchItems(
                        photosViewModel,
                        videoViewModel,
                        inputText.value
                    )
                } else {
                    searchItems.value = listOf()
                }
                isInputFieldEmpty.value = it.isEmpty()
            },
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth()
                .padding(16.dp),
            label = {
                Text(
                    "Search",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    searchItems.value = searchViewModel.getSearchItems(
                        photosViewModel,
                        videoViewModel,
                        inputText.value
                    )
                }
            ),
            leadingIcon = {
                IconButton(
                    onClick = {
                        keyboardController?.hide()
                        currentScreen.value = Screens.HomeScreen.route
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back Button"
                    )
                }
            },
            trailingIcon = {
                if (inputText.value.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            inputText.value = ""
                            searchItems.value = listOf()
                        },
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close Button")
                    }
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

@Composable
fun SearchContent(
    searchItems: MutableState<List<SearchViewModel.SearchItem>>,
    navHostController: NavHostController,
    isInputFieldEmpty: MutableState<Boolean>
) {
    if (searchItems.value.isEmpty() && !isInputFieldEmpty.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Not Found", style = MaterialTheme.typography.titleLarge)
        }
    }

    LazyColumn {
        items(searchItems.value) {
            SearchItemRow(it) {
                if (it.type == "image") {
                    navHostController.navigate(Screens.ImageDetailsScreen.route + "/${it.index}/${it.listIndex}") {
                        launchSingleTop = true
                    }
                } else {
                    navHostController.navigate(Screens.VideoScreen.route + "/${it.index}/${it.listIndex}") {
                        launchSingleTop = true
                    }
                }
            }
        }
    }
}

@Composable
fun SearchItemRow(it: SearchViewModel.SearchItem, onClick: () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            it.title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.7f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 3
        )
        if (it.type == "image") {
            AsyncImage(
                model = it.url,
                contentDescription = it.title,
                modifier = Modifier
                    .padding(16.dp)
                    .size(98.dp)
            )
        } else {
            AsyncImage(
                model = it.videoItem?.thumbnail,
                contentDescription = it.title,
                modifier = Modifier
                    .size(98.dp)
            )
        }
    }
}

package com.littlebit.photos.ui.navigation

sealed class Screens(val route: String) {
    data object HomeScreen : Screens("homeScreen")
    data object LauncherScreen : Screens("launcherScreen")
    data object Settings : Screens("settings")
    data object ImageDetailsScreen : Screens("imageDetailsScreen")
    data object  VideoScreen : Screens("videoScreen")
    data object  VideoGridScreen : Screens("videoGridScreen")
    data object SearchScreen : Screens("searchScreen")
    data object AudioListScreen : Screens("audioScreen")
    data object PlayAudioScreen : Screens("playAudioScreen")
}
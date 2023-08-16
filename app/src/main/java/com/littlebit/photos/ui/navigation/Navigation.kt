package com.littlebit.photos.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.littlebit.photos.ui.screens.home.HomeScreen
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.images.details.ImageDetailsScreen
import com.littlebit.photos.ui.screens.launcher.LauncherScreen
import com.littlebit.photos.ui.screens.settings.SettingsScreen
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.player.VideoScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    isDarkTheme: MutableState<Boolean>,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            Screens.LauncherScreen.route,
        ) {
            LauncherScreen(navHostController = navController, photosViewModel, videoViewModel)
        }
        composable(
            Screens.HomeScreen.route,
        ) {
            HomeScreen(navHostController = navController, photosViewModel, videoViewModel)
        }
        composable(
            Screens.Settings.route
        ) {
            SettingsScreen(navHostController = navController, isDarkTheme = isDarkTheme)
        }
        composable(
            Screens.ImageDetailsScreen.route + "/{imageIndex}/{listIndex}",
            arguments = listOf(
                navArgument("imageIndex") { type = NavType.IntType },
                navArgument("listIndex") { type = NavType.IntType }
            ),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(400),
                    initialOffsetX = { it }
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(400),
                    targetOffsetX = { it }
                )
            },
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ImageDetailsScreen(
                    navHostController = navController,
                    photosViewModel = photosViewModel,
                    imageIndex = it.arguments?.getInt("imageIndex") ?: 0,
                    listIndex = it.arguments?.getInt("listIndex") ?: 0,
                )
            }
        }

        composable(
            Screens.VideoScreen.route + "/{videoIndex}/{listIndex}",
            arguments = listOf(
                navArgument("videoIndex") { type = NavType.IntType },
                navArgument("listIndex") { type = NavType.IntType }
            ),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(400),
                    initialOffsetX = { it }
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(400),
                    targetOffsetX = { it }
                )
            },
        ){
            val videoIndex = it.arguments?.getInt("videoIndex") ?: 0
            val listIndex = it.arguments?.getInt("listIndex") ?: 0
            VideoScreen(navHostController = navController, videoViewModel = videoViewModel, videoIndex = videoIndex, listIndex = listIndex)
        }
    }
}
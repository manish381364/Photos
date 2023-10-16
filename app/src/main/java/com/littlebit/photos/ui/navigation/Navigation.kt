package com.littlebit.photos.ui.navigation

import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.littlebit.photos.model.ScaleTransitionDirection
import com.littlebit.photos.ui.screens.audio.AudioViewModel
import com.littlebit.photos.ui.screens.audio.player.AudioScreen
import com.littlebit.photos.ui.screens.audio.player.XAudioViewModel
import com.littlebit.photos.ui.screens.home.HomeScreen
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.screens.images.details.ImageDetailsScreen
import com.littlebit.photos.ui.screens.settings.SettingsScreen
import com.littlebit.photos.ui.screens.settings.SettingsViewModel
import com.littlebit.photos.ui.screens.videos.VideoViewModel
import com.littlebit.photos.ui.screens.videos.player.VideoPlayerViewModel
import com.littlebit.photos.ui.screens.videos.player.VideoScreen

@RequiresApi(34)
@Composable
@UnstableApi
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel,
    audioViewModel: AudioViewModel,
    settingsViewModel: SettingsViewModel,
    videoPlayerViewModel: VideoPlayerViewModel,
    xAudioViewModel: XAudioViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(
            Screens.HomeScreen.route,
            enterTransition = {
                scaleIntoContainer()
            },
            exitTransition = {
                scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            },
            popExitTransition = {
                scaleOutOfContainer()
            }
        ) {
            HomeScreen(
                navHostController = navController,
                photosViewModel,
                videoViewModel,
                audioViewModel,
                xAudioViewModel
            )
        }
        composable(
            Screens.ImageDetailsScreen.route + "/{imageIndex}/{listIndex}",
            arguments = listOf(
                navArgument("imageIndex") { type = NavType.IntType },
                navArgument("listIndex") { type = NavType.IntType }
            ),
            enterTransition = {
                scaleIntoContainer()
            },
            exitTransition = {
                scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            },
            popExitTransition = {
                scaleOutOfContainer()
            },
        ) {
            ImageDetailsScreen(
                navHostController = navController,
                photosViewModel = photosViewModel,
                imageIndex = it.arguments?.getInt("imageIndex") ?: 0,
                listIndex = it.arguments?.getInt("listIndex") ?: 0,
            )
        }

        composable(
            Screens.VideoScreen.route + "/{videoIndex}/{listIndex}",
            arguments = listOf(
                navArgument("videoIndex") { type = NavType.IntType },
                navArgument("listIndex") { type = NavType.IntType }
            ),
            enterTransition = {
                scaleIntoContainer()
            },
            exitTransition = {
                scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            },
            popExitTransition = {
                scaleOutOfContainer()
            },
        ) {
            val videoIndex = it.arguments?.getInt("videoIndex") ?: 0
            val listIndex = it.arguments?.getInt("listIndex") ?: 0
            VideoScreen(
                navHostController = navController,
                videoViewModel = videoViewModel,
                videoIndex = videoIndex,
                listIndex = listIndex,
                videoPlayerViewModel = videoPlayerViewModel
            )
        }


        composable(
            route = Screens.XAudioScreen.route,
            enterTransition = {
                scaleIntoContainer()
            },
            exitTransition = {
                scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            },
            popExitTransition = {
                scaleOutOfContainer()
            }
        ) {
            AudioScreen(xAudioViewModel = xAudioViewModel, navController)
        }

        composable(
            Screens.SettingsScreen.route,
            enterTransition = {
                scaleIntoContainer()
            },
            exitTransition = {
                scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
            },
            popEnterTransition = {
                scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
            },
            popExitTransition = {
                scaleOutOfContainer()
            }
        ) {
            SettingsScreen(navHostController = navController, settingsViewModel)
        }
    }
}


fun scaleIntoContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
    initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
): EnterTransition {
    return scaleIn(
        animationSpec = tween(220, delayMillis = 90),
        initialScale = initialScale
    ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
}

fun scaleOutOfContainer(
    direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
    targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition {
    return scaleOut(
        animationSpec = tween(
            durationMillis = 220,
            delayMillis = 90
        ), targetScale = targetScale
    ) + fadeOut(tween(delayMillis = 90))
}
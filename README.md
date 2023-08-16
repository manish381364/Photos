# Photos App - Displaying Photos and Videos with Jetpack Compose
<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="App_Icon" width="30">
Photos App Logo

The Photos App is a modern and user-friendly Android application developed using Jetpack Compose and Kotlin, following the MVVM (Model-View-ViewModel) architecture. It allows users to effortlessly browse and view both photos and videos from their device's internal storage. The app leverages the power of Jetpack Compose, a modern UI toolkit, to provide a seamless and engaging user experience.

## Key Features
- **Intuitive Navigation**: The app offers an intuitive and smooth navigation experience with a bottom navigation bar, allowing users to seamlessly switch between the Photos and Videos screens.

- **Dynamic UI Components**: Jetpack Compose's declarative syntax is used to create dynamic UI components, ensuring a consistent and visually appealing user interface across different devices.

- **Photos Grid**: The Photos Grid screen beautifully presents the user's photos in a grid layout, allowing for easy browsing and selection. Jetpack Compose animations enhance the overall interaction.

- **Videos Grid**: Users can explore and watch their favorite videos using the Videos Grid screen, providing a fluid and responsive video playback experience.


## Code Sample - HomeScreen

```kotlin
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel
) {
    val currentScreen = rememberSaveable{ mutableStateOf(Screens.HomeScreen.route) }
    val bottomBarVisibility = remember { mutableStateOf(true) }
    val imageScreenListState = rememberLazyListState()
    val videoScreenListState = rememberLazyListState()
    Box(
        Modifier
            .fillMaxSize()
    ) {
        Crossfade(targetState = currentScreen.value, label = "Screens") { screen ->
            when (screen) {
                Screens.HomeScreen.route -> {
                    AnimatedVisibility(true) {
                        ImageGridScreen(navHostController, photosViewModel, bottomBarVisibility, imageScreenListState)
                    }
                }

                Screens.VideoGridScreen.route -> {
                    AnimatedVisibility(true) {
                        VideosGridScreen(videoViewModel, bottomBarVisibility, videoScreenListState, navHostController)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = bottomBarVisibility.value,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            HomeScreenBottomBar(
                Modifier,
                navHostController,
                currentScreen,
                visibility = bottomBarVisibility
            )
        }
    }
}

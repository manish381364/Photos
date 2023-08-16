package com.littlebit.photos.ui.screens.launcher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.littlebit.photos.R
import com.littlebit.photos.ui.screens.images.PhotosViewModel
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.screens.videos.VideoViewModel


fun checkPermissionStatus(context: ComponentActivity, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LauncherScreen(
    navHostController: NavHostController,
    photosViewModel: PhotosViewModel,
    videoViewModel: VideoViewModel
) {
    val showLogo = remember{
        mutableStateOf(true)
    }
    val context = LocalContext.current
    val componentActivity = context as ComponentActivity
    val permissionToCheck = Manifest.permission.READ_MEDIA_IMAGES
    remember { checkPermissionStatus(componentActivity, permissionToCheck) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, navigate to the HomeScreen
            photosViewModel.loadMedia(context)
            navHostController.navigate(Screens.HomeScreen.route) {
                popUpTo(Screens.LauncherScreen.route) {
                    inclusive = true
                }
            }
        } else {
            // Permission denied, show a Toast message and navigate to a screen where the user can grant permissions
            Toast.makeText(context, "Grant permission to continue", Toast.LENGTH_SHORT).show()

           // open the app's settings page to allow the user to grant the permissions manually
            val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            appSettingsIntent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(appSettingsIntent)
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted, fetch video URIs
            videoViewModel.refreshVideos(context)
        } else {
            // Handle permission denied
            Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if(showLogo.value)
            AsyncImage(model = R.mipmap.ic_launcher, contentDescription = "Logo" )
        else{
            if(checkPermissionStatus(componentActivity, permissionToCheck).not()){
                val appSettingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { _ ->
                    // Check if the user has granted the permission after returning from the settings activity
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission granted, navigate to the HomeScreen
                        navHostController.navigate(Screens.HomeScreen.route) {
                            popUpTo(Screens.LauncherScreen.route) {
                                inclusive = true
                            }
                        }
                    } else {
                        // Permission not granted, show a Toast message
                        Toast.makeText(context, "Permission not granted. Please grant permission to continue.", Toast.LENGTH_SHORT).show()
                    }
                }
                Button(
                    onClick = {
                        val appSettingsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        appSettingsIntent.data = Uri.fromParts("package", context.packageName, null)

                        // Start the settings activity for a result
                        appSettingsLauncher.launch(appSettingsIntent)
                    },
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(text = "Open App Settings")
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        // Wait for 200 milliseconds
        kotlinx.coroutines.delay(200)
        showLogo.value = false
        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        launcher.launch(Manifest.permission.READ_MEDIA_VIDEO)
        if(checkPermissionStatus(componentActivity, permissionToCheck)){
            navHostController.navigate(Screens.HomeScreen.route) {
                popUpTo(Screens.LauncherScreen.route) {
                    inclusive = true
                }
            }
        }
    }
}

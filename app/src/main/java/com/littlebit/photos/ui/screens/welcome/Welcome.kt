package com.littlebit.photos.ui.screens.welcome
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.littlebit.photos.R
import com.littlebit.photos.ui.navigation.Screens
import com.littlebit.photos.ui.theme.Typography

@Composable
fun WelcomeScreen(navHostController: NavHostController,) {
    val statusBarHeight = WindowInsets.statusBars.getTop(LocalDensity.current)
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Blue.copy(0.5f))
            .padding(top = statusBarHeight.dp)
            .verticalScroll(rememberScrollState()), Arrangement.Center, Alignment.CenterHorizontally) {
            WelcomeImageVector()
            WelcomeMessage()
            Spacer(Modifier.height(20.dp))
            LoginSignUpButtons(navHostController)
    }
}

@Composable
fun LoginSignUpButtons(navHostController: NavHostController) {
    Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
        ElevatedButton(onClick = {
            navHostController.navigate(Screens.LoginScreen.route)
        }) {
            Text(text = "Login")
        }
        Spacer(Modifier.fillMaxWidth(0.2f))
        ElevatedButton(onClick = {
            navHostController.navigate(Screens.SignUpScreen.route)
        }) {
            Text(text = "Sign Up")
        }
    }
}

@Composable
fun WelcomeMessage() {
    Text(
        text = "Welcome to Photos \n     Discover your Medias here...",
        Modifier.padding(8.dp),
        fontSize = 20.sp,
        letterSpacing = 3.sp,
        fontFamily = Typography.titleLarge.fontFamily,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun WelcomeImageVector() {
    Image(
        painter = painterResource(id = R.drawable.welcome_image_asset_foreground),
        contentDescription = "Welcome Image",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxSize(1f)
    )
}
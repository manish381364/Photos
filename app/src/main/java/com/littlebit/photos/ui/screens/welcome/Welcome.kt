package com.littlebit.photos.ui.screens.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun Welcome() {
    Column(Modifier.fillMaxSize()) {
        WelcomeImageVector()
        WelcomeMessage()
        LoginSignUpButtons()
    }
}

@Composable
fun LoginSignUpButtons() {
    Row(Modifier.fillMaxWidth(0.8f)) {
        ElevatedButton(modifier = Modifier.padding(2.dp).fillMaxWidth(0.3f), onClick = {}) {
            Text(text = "Login")
        }
        Spacer(Modifier.width(2.dp))
        ElevatedButton(modifier = Modifier.padding(2.dp).fillMaxWidth(0.3f), onClick = {}) {
            Text(text = "Sign Up")
        }
    }
}

@Composable
fun WelcomeMessage() {
    Text("Welcome to Photos \n Discover your Medias here")
}

@Composable
fun WelcomeImageVector() {
    
}
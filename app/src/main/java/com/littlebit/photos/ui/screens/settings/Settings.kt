package com.littlebit.photos.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    isDarkTheme: MutableState<Boolean>
){
    Scaffold(
        topBar = {
            SettingsScreenTopBar()
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

        }
    }
}

@Composable
fun SettingsScreenTopBar() {
    TODO("Not yet implemented")
}

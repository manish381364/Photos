package com.littlebit.photos.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun SettingsScreen(
    navHostController: NavHostController,
    isDarkTheme: MutableState<Boolean>
) {
    Scaffold(
        topBar = {
            SettingsScreenTopBar(navHostController)
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ThemeSetting(
                    title = "Dark Theme",
                    isDarkTheme
                )
            }
        }
    }
}


@Composable
fun ThemeSetting(title: String, isDarkTheme: MutableState<Boolean>) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Switch(checked = isDarkTheme.value, onCheckedChange = { isDarkTheme.value = it })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenTopBar(navHostController: NavHostController) {
    TopAppBar(
        title = {
            Text(text = "Settings", style = MaterialTheme.typography.titleLarge)
        },
        navigationIcon = {
            IconButton(onClick = { navHostController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

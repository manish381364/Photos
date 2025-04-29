package com.littlebit.photos

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.littlebit.photos.ui.navigation.Screens

fun getStartDestination(context: Context): String {
    val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isFirstTime = sharedPreferences.getBoolean("is_first_time", true)
    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null

    return when {
        isFirstTime -> {
            // Set first-time flag to false
            sharedPreferences.edit().putBoolean("is_first_time", false).apply()
            Screens.WelcomeScreen.route
        }
        isLoggedIn -> Screens.HomeScreen.route
        else -> Screens.LoginScreen.route
    }
}

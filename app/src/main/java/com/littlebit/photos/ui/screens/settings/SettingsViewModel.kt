package com.littlebit.photos.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebit.photos.model.ThemePreference
import com.littlebit.photos.model.repository.AppDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appDatabase: AppDatabase
) : ViewModel() {
    val isDarkTheme: MutableStateFlow<Boolean?> = MutableStateFlow(false)
    fun updateTheme() {
        viewModelScope.launch {
            val themePreference = appDatabase.themePreferenceDao().getThemePreference()
            Log.d("THEME_PREFERENCE", "updateTheme: $themePreference")
            isDarkTheme.value = themePreference?.isDarkTheme
        }
    }
    fun setThemePreference(currentTheme: Boolean) {
        viewModelScope.launch {
            appDatabase.themePreferenceDao().insertOrUpdate(ThemePreference(1, currentTheme))
            isDarkTheme.value = currentTheme
            val theme = appDatabase.themePreferenceDao().getThemePreference()
            Log.d("THEME_PRE", "setThemePreference:  $theme")
        }
    }
}
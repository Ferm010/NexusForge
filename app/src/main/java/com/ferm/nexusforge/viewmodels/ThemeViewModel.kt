package com.ferm.nexusforge.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.core.content.edit

private const val PREFS_NAME = "theme_prefs"
private const val KEY_DARK_MODE = "dark_mode"

class ThemeViewModel : ViewModel() {
    
    var isDarkTheme by mutableStateOf(false)
        private set
    
    fun init(context: Context, systemDarkTheme: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isDarkTheme = prefs.getBoolean(KEY_DARK_MODE, systemDarkTheme)
    }
    
    fun toggleTheme(context: Context) {
        isDarkTheme = !isDarkTheme
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkTheme).apply()
    }
}

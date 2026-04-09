package com.ferm.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ThemeViewModel : ViewModel() {
    
    var isDarkTheme by mutableStateOf<Boolean?>(null)
        private set
    
    fun initTheme(systemDarkTheme: Boolean) {
        if (isDarkTheme == null) {
            isDarkTheme = systemDarkTheme
        }
    }
    
    fun toggleTheme() {
        isDarkTheme = !(isDarkTheme ?: false)
    }
    
    fun setTheme(isDark: Boolean) {
        isDarkTheme = isDark
    }
}

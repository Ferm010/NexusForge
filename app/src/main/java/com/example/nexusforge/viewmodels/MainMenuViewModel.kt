package com.example.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainMenuViewModel : ViewModel() {
    
    // Search state
    var searchQuery by mutableStateOf("")
    
    fun clearSearch() {
        searchQuery = ""
    }
}

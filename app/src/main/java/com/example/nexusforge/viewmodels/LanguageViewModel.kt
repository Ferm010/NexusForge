package com.example.nexusforge.viewmodels

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.nexusforge.backend.LocaleHelper

class LanguageViewModel : ViewModel() {
    
    private var _currentLanguage: String? = null
    
    var currentLanguage: String
        get() = _currentLanguage ?: "ru"
        private set(value) { _currentLanguage = value }
    
    fun getLanguages(): List<LocaleHelper.LanguageOption> {
        return LocaleHelper.supportedLanguages
    }
    
    fun getCurrentLanguageCode(context: Context): String {
        return LocaleHelper.getLocale(context)
    }
    
    fun initLanguage(context: Context) {
        if (_currentLanguage == null) {
            _currentLanguage = LocaleHelper.getLocale(context)
        }
    }
    
    fun setLanguage(context: Context, languageCode: String) {
        LocaleHelper.setLocale(context, languageCode)
        _currentLanguage = languageCode
        
        val activity = context as? Activity
        activity?.recreate()
    }
}

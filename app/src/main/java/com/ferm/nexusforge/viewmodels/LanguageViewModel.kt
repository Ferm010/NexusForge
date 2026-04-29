package com.ferm.nexusforge.viewmodels

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import com.ferm.nexusforge.backend.LocaleHelper

class LanguageViewModel : ViewModel() {
    
    private var _currentLanguage: String? = null
    
    var currentLanguage: String
        get() = _currentLanguage ?: "ru"
        private set(value) { _currentLanguage = value }

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

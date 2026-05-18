package com.ferm.nexusforge.viewmodels

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import com.ferm.nexusforge.backend.LocaleHelper
import com.ferm.nexusforge.MainActivity

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
        
        AppCompatDelegate.setApplicationLocales(
            androidx.core.os.LocaleListCompat.forLanguageTags(languageCode)
        )
        
        val activity = context as? Activity
        if (activity != null) {
            activity.recreate()
        } else {
            val intent = android.content.Intent(context, MainActivity::class.java).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }
}

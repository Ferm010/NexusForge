package com.ferm.nexusforge.backend

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    
    private const val PREFS_NAME = "locale_prefs"
    private const val KEY_LANGUAGE = "language"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun setLocale(context: Context, language: String) {
        getPrefs(context).edit().putString(KEY_LANGUAGE, language).apply()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(language)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(language)
            )
        }
    }
    
    fun getLocale(context: Context): String {
        return getPrefs(context).getString(KEY_LANGUAGE, "ru") ?: "ru"
    }
    
    fun applyLocale(context: Context, language: String? = null) {
        val lang = language ?: getLocale(context)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
                .applicationLocales = LocaleList.forLanguageTags(lang)
        } else {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(lang)
            )
        }
    }
    
    fun onAttach(context: Context): Context {
        val lang = getLocale(context)
        return updateResources(context, lang)
    }
    
    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        val config = context.resources.configuration
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        return context.createConfigurationContext(config)
    }
    
    val supportedLanguages = listOf(
        LanguageOption("ru", "Русский", "🇷🇺"),
        LanguageOption("en", "English", "🇬🇧"),
        LanguageOption("de", "Deutsch", "🇩🇪"),
        LanguageOption("es", "Español", "🇪🇸"),
        LanguageOption("fr", "Français", "🇫🇷")
    )
    
    data class LanguageOption(
        val code: String,
        val name: String,
        val flag: String
    )
}

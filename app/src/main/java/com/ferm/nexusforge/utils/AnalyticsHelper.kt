package com.ferm.nexusforge.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    
    // События поиска
    fun logSearchMods(query: String, resultsCount: Int) {
        val bundle = Bundle().apply {
            putString("search_query", query)
            putInt("results_count", resultsCount)
        }
        firebaseAnalytics.logEvent("search_mods", bundle)
    }
    
    // События создания сборки
    fun logModpackCreated(modpackName: String, modsCount: Int, minecraftVersion: String) {
        val bundle = Bundle().apply {
            putString("modpack_name", modpackName)
            putInt("mods_count", modsCount)
            putString("minecraft_version", minecraftVersion)
        }
        firebaseAnalytics.logEvent("modpack_created", bundle)
    }
    
    // События добавления мода
    fun logModAdded(modName: String, modpackName: String) {
        val bundle = Bundle().apply {
            putString("mod_name", modName)
            putString("modpack_name", modpackName)
        }
        firebaseAnalytics.logEvent("mod_added", bundle)
    }
    
    // События сохранения в избранное
    fun logProjectFavorited(projectName: String, projectType: String) {
        val bundle = Bundle().apply {
            putString("project_name", projectName)
            putString("project_type", projectType) // "mod" или "modpack"
        }
        firebaseAnalytics.logEvent("project_favorited", bundle)
    }
    
    // События удаления из избранного
    fun logProjectUnfavorited(projectName: String, projectType: String) {
        val bundle = Bundle().apply {
            putString("project_name", projectName)
            putString("project_type", projectType)
        }
        firebaseAnalytics.logEvent("project_unfavorited", bundle)
    }
    
    // События экспорта сборки
    fun logModpackExported(modpackName: String, modsCount: Int, exportMethod: String) {
        val bundle = Bundle().apply {
            putString("modpack_name", modpackName)
            putInt("mods_count", modsCount)
            putString("export_method", exportMethod) // "zip", "google_drive", etc
        }
        firebaseAnalytics.logEvent("modpack_exported", bundle)
    }
    
    // События просмотра деталей проекта
    fun logProjectViewed(projectName: String, projectType: String) {
        val bundle = Bundle().apply {
            putString("project_name", projectName)
            putString("project_type", projectType)
        }
        firebaseAnalytics.logEvent("project_viewed", bundle)
    }
    
    // События смены версии Minecraft
    fun logMinecraftVersionChanged(version: String) {
        val bundle = Bundle().apply {
            putString("minecraft_version", version)
        }
        firebaseAnalytics.logEvent("minecraft_version_changed", bundle)
    }
    
    // События смены mod loader
    fun logModLoaderChanged(loader: String) {
        val bundle = Bundle().apply {
            putString("mod_loader", loader)
        }
        firebaseAnalytics.logEvent("mod_loader_changed", bundle)
    }
    
    // События входа пользователя
    fun logUserSignIn(authMethod: String) {
        val bundle = Bundle().apply {
            putString("auth_method", authMethod) // "email", "google", etc
        }
        firebaseAnalytics.logEvent("user_sign_in", bundle)
    }
    
    // События выхода пользователя
    fun logUserSignOut() {
        firebaseAnalytics.logEvent("user_sign_out", Bundle())
    }
    
    // События ошибок
    fun logError(errorType: String, errorMessage: String) {
        val bundle = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", errorMessage)
        }
        firebaseAnalytics.logEvent("app_error", bundle)
    }
    
    // События открытия экрана
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}

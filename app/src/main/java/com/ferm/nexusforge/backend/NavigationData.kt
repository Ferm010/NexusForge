package com.ferm.nexusforge.backend

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
@Serializable
sealed interface Destination : NavKey {



    @Serializable data object RegPage : Destination
    @Serializable data object EulaPage : Destination
    @Serializable data object AuthPassPage : Destination
    @Serializable data object RegPassPage : Destination
    @Serializable data object NameRegPage : Destination
    @Serializable data class ForgotPasswordPage(val email: String = "") : Destination
    @Serializable data object MainMenu : Destination
    @Serializable data object FavoritePage : Destination
    @Serializable data object ProfilePage : Destination
    @Serializable data object SettingsPage : Destination
    @Serializable data object TechnicalPage : Destination
    @Serializable data object LanguagePage : Destination
    @Serializable data object FAQPage : Destination
    @Serializable data object OssLicensesPage : Destination
    @Serializable data object CreateModpackPage : Destination
    @Serializable data object SelectGenerationMethodPage : Destination
    @Serializable data class GenerateModpackPage(val method: String) : Destination
    @Serializable data class ProjectDetailsPage(val projectId: String) : Destination
    @Serializable data class ModpackEditorPage(val modpackId: String) : Destination
    @Serializable data object TemplatesListPage : Destination
    @Serializable data object CreateTemplatePage : Destination
    @Serializable data class EditTemplatePage(val templateId: String) : Destination
}


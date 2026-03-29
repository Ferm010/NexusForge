package com.example.nexusforge.backend

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
@Serializable
sealed interface Destination : NavKey {


    // I. АВТОРИЗАЦИЯ И РЕГИСТРАЦИЯ

    @Serializable data object RegPage : Destination
    @Serializable data object EulaPage : Destination
    @Serializable data object AuthPassPage : Destination
    @Serializable data object RegPassPage : Destination
    @Serializable data object NameRegPage : Destination
    @Serializable data object MainMenu : Destination
    @Serializable data object SearchPage: Destination
    @Serializable data object FavoritePage : Destination
    @Serializable data object ProfilePage : Destination
    @Serializable data object SettingsPage : Destination
    @Serializable data object CreateAlert : Destination

    /* @Serializable data object ResetPassPage : Destination





    // ДЕТАЛИ И КОНТЕНТ (Content Flow)


    // Страницы деталей - ОБЯЗАТЕЛЬНО требуют ID
    @Serializable data object ModpackPage : Destination

    @Serializable data object ModPage : Destination

    // Страница с информацией о созданном модпаке (тоже нужен ID)
    @Serializable data object ModpackCustomPage : Destination

    // Окно смены языка
    @Serializable data object ChangeLanguagePage : Destination

    // Окно техподдержки
    @Serializable data object TechnicalPage : Destination


    // АЛЕРТЫ И НИЖНИЕ ЛИСТЫ (Dialogs & Sheets)


    // Лист с фильтрами
    @Serializable data object SearchFilterSheets: Destination

    // Лист смены данных: передает начальные данные для заполнения полей
    @Serializable data object ChangeDataSheets : Destination

    // Алерт (всплывающее окно) выбора создания сборки/шаблона
    @Serializable data object CreateAlert : Destination

    // Алерт удаления аккаунта
    @Serializable data object DeleteAccAlert : Destination

    // Алерт выхода из аккаунта
    @Serializable data object ExitAccAlert : Destination */
}



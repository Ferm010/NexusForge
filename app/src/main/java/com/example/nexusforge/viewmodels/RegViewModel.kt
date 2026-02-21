package com.example.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel


class RegViewModel : ViewModel(){
    private val emailPattern = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$"
    )
    var email by mutableStateOf("")
    private set
    var isError by mutableStateOf(false)
    private set
    var password by mutableStateOf("")
    var userName by mutableStateOf("")
    var isGoogleFlow by mutableStateOf(false)

    private val registeredEmails = mutableSetOf<String>()

    // Вычисляемое свойство: кнопка активна только если email валиден и нет ошибки
    val isContinueEnabled: Boolean
        get() = email.matches(emailPattern)

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        // Простая валидация (можно заменить на Regex)
        isError = newEmail.isNotEmpty() && !newEmail.matches(emailPattern)
    }

    fun checkEmailExists(email: String): Boolean = email in registeredEmails

    fun handleGoogleSignIn() {
        isGoogleFlow = true
    }
}
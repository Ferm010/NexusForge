package com.example.nexusforge.viewmodels

import android.content.Context
import android.util.Log
import com.example.nexusforge.backend.NetworkUtils
import kotlinx.coroutines.delay

class EmailValidator {
    
    private val emailPattern = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}$"
    )

    fun isValidFormat(email: String): Boolean {
        return email.matches(emailPattern)
    }

    suspend fun validate(email: String, context: Context): ValidationResult {
        delay(200)
        
        Log.d("EmailValidator", "Валидация $email")

        // 1. Проверка формата
        if (!isValidFormat(email)) {
            return ValidationResult.Error("Введите корректный email")
        }

        // 2. Реальная проверка доступа к интернету
        Log.d("EmailValidator", "Чек интернет доступ")
        val hasInternet = NetworkUtils.hasInternetAccess()
        Log.d("EmailValidator", "Интернет: $hasInternet")
        if (!hasInternet) {
            return ValidationResult.Error("Нет подключения к интернету")
        }

        // 3. Проверка существования домена
        Log.d("EmailValidator", "Чек домен")
        val domainExists = NetworkUtils.checkEmailDomainExists(email)
        Log.d("EmailValidator", "Успешно $domainExists")
        if (!domainExists) {
            return ValidationResult.Error("Домен не существует. Проверьте правильность email.")
        }
        
        Log.d("EmailValidator", "Успешно")
        return ValidationResult.Success
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

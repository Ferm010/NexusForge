package com.ferm.nexusforge.viewmodels

import android.content.Context
import com.ferm.nexusforge.R
import com.ferm.nexusforge.backend.NetworkUtils
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
        
        // 1. Проверка формата
        if (!isValidFormat(email)) {
            return ValidationResult.Error(context.getString(R.string.error_email_invalid))
        }

        // 2. Реальная проверка доступа к интернету
        val hasInternet = NetworkUtils.hasInternetAccess()
        if (!hasInternet) {
            return ValidationResult.Error(context.getString(R.string.error_no_internet))
        }

        // 3. Проверка существования домена
        val domainExists = NetworkUtils.checkEmailDomainExists(email)
        if (!domainExists) {
            return ValidationResult.Error(context.getString(R.string.error_domain_not_exists))
        }
        
        return ValidationResult.Success
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

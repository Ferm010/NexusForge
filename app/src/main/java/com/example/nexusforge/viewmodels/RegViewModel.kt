package com.example.nexusforge.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusforge.backend.NetworkUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RegViewModel : ViewModel() {
    
    private val emailValidator = EmailValidator()
    private val authRepository = AuthRepository()
    
    // UI State
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
    var userName by mutableStateOf("")
    var isGoogleFlow by mutableStateOf(false)
    
    var isValidatingEmail by mutableStateOf(false)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    
    private var validationJob: Job? = null
    
    init {
        authRepository.currentUser?.let { user ->
            userName = user.displayName ?: ""
        }
    }
    
    val isContinueEnabled: Boolean
        get() = emailValidator.isValidFormat(email) && !isValidatingEmail && emailError == null

    // Email валидация
    fun onEmailChanged(newEmail: String, context: Context) {
        email = newEmail
        emailError = null
        
        validationJob?.cancel()
        
        if (emailValidator.isValidFormat(newEmail)) {
            isValidatingEmail = true
            validationJob = viewModelScope.launch {
                when (val result = emailValidator.validate(newEmail, context)) {
                    is ValidationResult.Success -> {
                        isValidatingEmail = false
                        emailError = null
                    }
                    is ValidationResult.Error -> {
                        isValidatingEmail = false
                        emailError = result.message
                    }
                }
            }
        } else {
            isValidatingEmail = false
        }
    }

    // Навигация
    fun checkEmailAndNavigate(
        context: Context,
        onExists: () -> Unit,
        onGoogleOnly: () -> Unit,
        onNotExists: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError("Нет подключения к интернету")
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.checkEmailExists(email)) {
                is EmailExistsResult.Exists -> onExists()
                is EmailExistsResult.GoogleOnly -> onGoogleOnly()
                is EmailExistsResult.NotExists -> onNotExists()
                is EmailExistsResult.Error -> onError(result.message)
            }
        }
    }

    // Аутентификация

    private var lastAttemptTime = 0L
    private var attemptCount = 0
    
    fun signInWithEmail(
        context: Context,
        enteredPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError("Нет подключения к интернету")
            return
        }
        
        // Rate limiting
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttemptTime < 3000 && attemptCount >= 3) {
            onError("Слишком много попыток. Подождите.")
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, enteredPassword)) {
                is AuthResult.Success -> {
                    attemptCount = 0
                    onSuccess()
                }
                is AuthResult.Error -> {
                    attemptCount++
                    lastAttemptTime = currentTime
                    onError(result.message)
                }
            }
        }
    }
    
    fun registerUser(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError("Нет подключения к интернету")
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.registerUser(email, password, userName)) {
                is AuthResult.Success -> onSuccess()
                is AuthResult.Error -> onError(result.message)
            }
        }
    }
    
    fun signInWithGoogle(
        context: Context,
        idToken: String,
        onSuccess: (isNewUser: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError("Нет подключения к интернету")
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is GoogleSignInResult.Success -> {
                    isGoogleFlow = true
                    if (!result.isNewUser) {
                        userName = result.displayName
                    }
                    onSuccess(result.isNewUser)
                }
                is GoogleSignInResult.Error -> onError(result.message)
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
        email = ""
        password = ""
        userName = ""
        isGoogleFlow = false
    }
}

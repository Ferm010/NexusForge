package com.ferm.nexusforge.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.backend.NetworkUtils
import com.ferm.nexusforge.backend.errorCodeToString
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RegViewModel : ViewModel() {
    
    private val emailValidator = EmailValidator()
    private val authRepository = AuthRepository()
    
    // UI State
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var userName by mutableStateOf("")
    var userPhotoUrl by mutableStateOf<String?>(null)
        private set
    var isGoogleFlow by mutableStateOf(false)
    
    // Auth password state
    var authPassword by mutableStateOf("")
    
    var isValidatingEmail by mutableStateOf(false)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    
    private var validationJob: Job? = null
    
    init {
        authRepository.currentUser?.let { user ->
            userName = user.displayName ?: ""
            userPhotoUrl = user.photoUrl?.toString()
        }
    }
    
    fun refreshUserData() {
        authRepository.currentUser?.let { user ->
            userName = user.displayName ?: ""
            userPhotoUrl = user.photoUrl?.toString()
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
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.checkEmailExists(email)) {
                is EmailExistsResult.Exists -> onExists()
                is EmailExistsResult.GoogleOnly -> onGoogleOnly()
                is EmailExistsResult.NotExists -> onNotExists()
                is EmailExistsResult.Error -> onError(errorCodeToString(context, result.errorCode))
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
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        // Rate limiting
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttemptTime < 3000 && attemptCount >= 3) {
            onError(errorCodeToString(context, "ERROR_TOO_MANY_REQUESTS"))
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, enteredPassword)) {
                is AuthResult.Success -> {
                    attemptCount = 0
                    refreshUserData()
                    onSuccess()
                }
                is AuthResult.Error -> {
                    attemptCount++
                    lastAttemptTime = currentTime
                    onError(errorCodeToString(context, result.errorCode))
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
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.registerUser(email, password, userName)) {
                is AuthResult.Success -> {
                    refreshUserData()
                    onSuccess()
                }
                is AuthResult.Error -> onError(errorCodeToString(context, result.errorCode))
            }
        }
    }
    
    fun signInWithGoogle(
        context: Context,
        idToken: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("RegViewModel", "signInWithGoogle called with idToken: ${idToken.take(20)}...")
        
        if (!NetworkUtils.isNetworkAvailable(context)) {
            android.util.Log.e("RegViewModel", "No network available")
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        viewModelScope.launch {
            android.util.Log.d("RegViewModel", "Calling authRepository.signInWithGoogle")
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is GoogleSignInResult.Success -> {
                    android.util.Log.d("RegViewModel", "Google Sign-In Success: isNewUser=${result.isNewUser}, displayName=${result.displayName}")
                    isGoogleFlow = true
                    if (!result.isNewUser) {
                        userName = result.displayName
                    }
                    refreshUserData()
                    onSuccess(result.isNewUser)
                }
                is GoogleSignInResult.Error -> {
                    android.util.Log.e("RegViewModel", "Google Sign-In Error: ${result.errorCode}")
                    onError(errorCodeToString(context, result.errorCode))
                }
            }
        }
    }
    
    fun signOut() {
        authRepository.signOut()
        email = ""
        password = ""
        confirmPassword = ""
        userName = ""
        userPhotoUrl = null
        isGoogleFlow = false
        authPassword = ""
    }
    
    fun isGoogleSignIn(): Boolean = authRepository.isGoogleSignIn()
    
    fun updateDisplayName(
        newName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = authRepository.updateDisplayName(newName)) {
                is UpdateResult.Success -> {
                    userName = newName
                    onSuccess()
                }
                is UpdateResult.Error -> onError(result.errorCode)
            }
        }
    }
    
    fun updateEmail(
        newEmail: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = authRepository.updateEmail(newEmail, password)) {
                is UpdateResult.Success -> {
                    email = newEmail
                    onSuccess()
                }
                is UpdateResult.Error -> onError(result.errorCode)
            }
        }
    }
    
    fun deleteAccount(
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount(password)) {
                is UpdateResult.Success -> {
                    signOut()
                    onSuccess()
                }
                is UpdateResult.Error -> onError(result.errorCode)
            }
        }
    }
}

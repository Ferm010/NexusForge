package com.ferm.nexusforge.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.backend.NetworkUtils
import com.ferm.nexusforge.backend.errorCodeToString
import com.ferm.nexusforge.repository.FirestoreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class RegViewModel : ViewModel() {
    private val emailValidator: EmailValidator by lazy {
        EmailValidator()
    }
    private val authRepository: AuthRepository by lazy {
        AuthRepository()
    }
    private val firestoreRepository: FirestoreRepository by lazy {
        FirestoreRepository()
    }
    private var context: Context? = null
    
    // UI State
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var userName by mutableStateOf("")
    var userPhotoUrl by mutableStateOf<String?>(null)
        private set
    var isGoogleFlow by mutableStateOf(false)

    private var internalEmail by mutableStateOf("")

    var authPassword by mutableStateOf("")
    
    var isValidatingEmail by mutableStateOf(false)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set

    var displayEmail by mutableStateOf("")
        private set
    var isSigningIn by mutableStateOf(false)
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

    // Навигация с проверкой методов авторизации
    fun checkEmailAndNavigate(
        context: Context,
        onExists: () -> Unit,
        onNotExists: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        viewModelScope.launch {
            android.util.Log.d("RegViewModel", "=== Starting email check for: $email ===")
            
            when (val result = authRepository.checkEmailExists(email)) {
                is EmailCheckResult.NewUser -> {
                    // Новый пользователь - идем на EULA
                    android.util.Log.d("RegViewModel", "Result: NEW USER - navigate to EULA")
                    onNotExists()
                }
                is EmailCheckResult.ExistingUser -> {
                    android.util.Log.d("RegViewModel", "Result: EXISTING USER with method: ${result.authMethod}")
                    when (result.authMethod) {
                        AuthMethod.EMAIL_PASSWORD -> {
                            // Аккаунт создан через email/пароль - идем на ввод пароля
                            android.util.Log.d("RegViewModel", "Action: Navigate to password page")
                            displayEmail = email
                            isSigningIn = true
                            onExists()
                        }
                        AuthMethod.GOOGLE -> {
                            // Аккаунт создан через Google - показываем ошибку
                            android.util.Log.d("RegViewModel", "Action: Show error - account exists with Google")
                            onError(errorCodeToString(context, "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"))
                        }
                        AuthMethod.OTHER -> {
                            // Другой метод авторизации
                            android.util.Log.d("RegViewModel", "Action: Show error - account exists with other method")
                            onError(errorCodeToString(context, "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL"))
                        }
                    }
                }
                is EmailCheckResult.Error -> {
                    onError(errorCodeToString(context, result.errorCode))
                }
            }
            android.util.Log.d("RegViewModel", "=== Email check completed ===")
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
        // Rate limiting
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttemptTime < 3000 && attemptCount >= 3) {
            onError(errorCodeToString(context, "ERROR_TOO_MANY_REQUESTS"))
            return
        }
        
        isSigningIn = true
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(email, enteredPassword)) {
                is AuthResult.Success -> {
                    attemptCount = 0
                    isSigningIn = false
                    refreshUserData()
                    onSuccess()
                }
                is AuthResult.Error -> {
                    attemptCount++
                    lastAttemptTime = currentTime
                    isSigningIn = false
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
                    val profileResult = firestoreRepository.createUserProfile(email, userName)
                    if (profileResult.isSuccess) {
                        refreshUserData()
                        onSuccess()
                    } else {
                        onError(errorCodeToString(context, "ERROR_FIRESTORE_WRITE"))
                    }
                }
                is AuthResult.Error -> onError(errorCodeToString(context, result.errorCode))
            }
        }
    }
    
    fun signInWithGoogle(
        context: Context,
        idToken: String,
        email: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        viewModelScope.launch {
            android.util.Log.d("RegViewModel", "Email from credential: '$email'")
            
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is GoogleSignInResult.Success -> {
                    
                    isGoogleFlow = true
                    internalEmail = email
                    userName = result.displayName

                    if (result.isNewUser) {
                        android.util.Log.d("RegViewModel", "Creating Firestore profile for new Google user")
                        android.util.Log.d("RegViewModel", "Passing email to Firestore: '$email'")
                        val profileResult = firestoreRepository.createUserProfile(email, result.displayName)
                        if (profileResult.isFailure) {
                            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
                            return@launch
                        }
                    }
                    
                    refreshUserData()
                    onSuccess(result.isNewUser)
                }
                is GoogleSignInResult.Error -> {
                    
                    // Firebase автоматически вернет ошибку если email уже используется с другим методом
                    if (result.errorCode == "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL") {
                        android.util.Log.d("RegViewModel", "Account exists with email/password method")
                    }
                    
                    onError(errorCodeToString(context, result.errorCode))
                }
            }
            android.util.Log.d("RegViewModel", "=== Google Sign-In completed ===")
        }
    }
    
    fun signOut() {
        firestoreRepository.clearAllListeners()
        authRepository.signOut()
        email = ""
        password = ""
        confirmPassword = ""
        userName = ""
        userPhotoUrl = null
        isGoogleFlow = false
        authPassword = ""
        displayEmail = ""
        isSigningIn = false
    }
    
    fun isGoogleSignIn(): Boolean = authRepository.isGoogleSignIn()
    
    fun setContext(ctx: Context) {
        context = ctx
    }
    
    fun updateDisplayName(
        newName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        context?.let { ctx ->
            if (!NetworkUtils.isNetworkAvailable(ctx)) {
                onError(errorCodeToString(ctx, "ERROR_NETWORK_REQUEST_FAILED"))
                return
            }
        }
        
        viewModelScope.launch {
            when (val result = authRepository.updateDisplayName(newName)) {
                is UpdateResult.Success -> {
                    userName = newName
                    onSuccess()
                }
                is UpdateResult.Error -> {
                    context?.let { ctx ->
                        onError(errorCodeToString(ctx, result.errorCode))
                    } ?: onError(result.errorCode)
                }
            }
        }
    }
    

    fun deleteAccount(
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        context?.let { ctx ->
            if (!NetworkUtils.isNetworkAvailable(ctx)) {
                onError(errorCodeToString(ctx, "ERROR_NETWORK_REQUEST_FAILED"))
                return
            }
        }
        
        viewModelScope.launch {
            when (val result = authRepository.deleteAccount(password)) {
                is UpdateResult.Success -> {
                    signOut()
                    onSuccess()
                }
                is UpdateResult.Error -> {
                    context?.let { ctx ->
                        onError(errorCodeToString(ctx, result.errorCode))
                    } ?: onError(result.errorCode)
                }
            }
        }
    }
    
    fun sendPasswordResetEmail(
        context: Context,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onError(errorCodeToString(context, "ERROR_NETWORK_REQUEST_FAILED"))
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.sendPasswordResetEmail(email)) {
                is SendEmailResult.Success -> onSuccess()
                is SendEmailResult.Error -> onError(errorCodeToString(context, result.errorCode))
            }
        }
    }
    
    fun validateSession(
        context: Context,
        onValid: () -> Unit,
        onInvalid: () -> Unit
    ) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            onInvalid()
            return
        }
        
        viewModelScope.launch {
            val isValid = authRepository.validateSession()
            if (isValid) {
                onValid()
            } else {
                signOut()
                onInvalid()
            }
        }
    }
}

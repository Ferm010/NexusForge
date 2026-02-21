package com.example.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusforge.backend.toRussianMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class RegViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

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

    init {
        // Если пользователь уже авторизован, восстанавливаем его имя
        auth.currentUser?.let { user ->
            userName = user.displayName ?: ""
        }
    }

    // Вычисляемое свойство: кнопка активна только если email валиден
    val isContinueEnabled: Boolean
        get() = email.matches(emailPattern)

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        isError = newEmail.isNotEmpty() && !newEmail.matches(emailPattern)
    }

    /**
     * Проверяет существование аккаунта через Firebase и вызывает нужный callback.
     * Примечание: требует отключения защиты от перебора email в Firebase Console
     * (Authentication → Settings → User account protection → Email enumeration protection → OFF).
     */
    fun checkEmailAndNavigate(onExists: () -> Unit, onNotExists: () -> Unit) {
        viewModelScope.launch {
            try {
                @Suppress("DEPRECATION")
                val methods = auth.fetchSignInMethodsForEmail(email).await()
                if (methods.signInMethods?.isNotEmpty() == true) {
                    onExists()
                } else {
                    onNotExists()
                }
            } catch (e: Exception) {
                // При ошибке направляем как нового пользователя
                onNotExists()
            }
        }
    }

    /**
     * Вход существующего пользователя через email/пароль.
     */
    fun signInWithEmail(
        enteredPassword: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, enteredPassword).await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.toRussianMessage())
            }
        }
    }

    /**
     * Регистрация нового пользователя через email/пароль с последующим обновлением имени.
     */
    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val profileUpdates = userProfileChangeRequest { displayName = userName }
                result.user?.updateProfile(profileUpdates)?.await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.toRussianMessage())
            }
        }
    }

    /**
     * Выход из аккаунта: разрывает сессию Firebase и сбрасывает локальное состояние.
     */
    fun signOut() {
        auth.signOut()
        email = ""
        password = ""
        userName = ""
        isGoogleFlow = false
    }

    /**
     * Вход/регистрация через Google. isNewUser = true означает новый аккаунт.
     */
    fun signInWithGoogle(
        idToken: String,
        onSuccess: (isNewUser: Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false
                isGoogleFlow = true
                if (!isNewUser) {
                    userName = result.user?.displayName ?: ""
                }
                onSuccess(isNewUser)
            } catch (e: Exception) {
                onError(e.toRussianMessage())
            }
        }
    }
}

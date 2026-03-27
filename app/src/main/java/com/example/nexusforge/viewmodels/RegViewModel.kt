package com.example.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusforge.backend.toRusError
import com.google.firebase.auth.EmailAuthProvider
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
        auth.currentUser?.let { user ->
            userName = user.displayName ?: ""
        }
    }

    val isContinueEnabled: Boolean
        get() = email.matches(emailPattern)

    fun onEmailChanged(newEmail: String) {
        email = newEmail
        isError = newEmail.isNotEmpty() && !newEmail.matches(emailPattern)
    }

    fun checkEmailAndNavigate(
        onExists: () -> Unit,
        onGoogleOnly: () -> Unit,
        onNotExists: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                @Suppress("DEPRECATION")
                val methods = auth.fetchSignInMethodsForEmail(email).await()
                val list = methods.signInMethods ?: emptyList()
                when {
                    list.contains("google.com") && !list.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) -> onGoogleOnly()
                    list.isNotEmpty() -> onExists()
                    else -> onNotExists()
                }
            } catch (e: Exception) {
                // как нового пользователя
                onNotExists()
            }
        }
    }
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
                onError(e.toRusError())
            }
        }
    }

    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val profileUpdates = userProfileChangeRequest { displayName = userName }
                result.user?.updateProfile(profileUpdates)?.await()
                onSuccess()
            } catch (e: Exception) {
                onError(e.toRusError())
            }
        }
    }


    fun signOut() {
        auth.signOut()
        email = ""
        password = ""
        userName = ""
        isGoogleFlow = false
    }

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
                onError(e.toRusError())
            }
        }
    }
}
package com.example.nexusforge.viewmodels

import com.example.nexusforge.backend.toRusError
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    
    private val auth = FirebaseAuth.getInstance()
    
    val currentUser get() = auth.currentUser
    
    /**
     * Проверка существования email в Firebase
     */
    suspend fun checkEmailExists(email: String): EmailExistsResult {
        return try {
            @Suppress("DEPRECATION")
            val methods = auth.fetchSignInMethodsForEmail(email).await()
            val list = methods.signInMethods ?: emptyList()
            
            when {
                list.contains("google.com") && !list.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) -> 
                    EmailExistsResult.GoogleOnly
                list.isNotEmpty() -> 
                    EmailExistsResult.Exists
                else -> 
                    EmailExistsResult.NotExists
            }
        } catch (e: Exception) {
            EmailExistsResult.Error(e.toRusError())
        }
    }
    
    /**
     * Вход по email и паролю
     */
    suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.toRusError())
        }
    }
    
    /**
     * Регистрация нового пользователя
     */
    suspend fun registerUser(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val profileUpdates = userProfileChangeRequest { this.displayName = displayName }
            result.user?.updateProfile(profileUpdates)?.await()
            AuthResult.Success
        } catch (e: Exception) {
            AuthResult.Error(e.toRusError())
        }
    }
    
    /**
     * Вход через Google
     */
    suspend fun signInWithGoogle(idToken: String): GoogleSignInResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            val displayName = result.user?.displayName ?: ""
            
            GoogleSignInResult.Success(isNewUser, displayName)
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.toRusError())
        }
    }
    
    /**
     * Выход
     */
    fun signOut() {
        auth.signOut()
    }
}

sealed class EmailExistsResult {
    object Exists : EmailExistsResult()
    object NotExists : EmailExistsResult()
    object GoogleOnly : EmailExistsResult()
    data class Error(val message: String) : EmailExistsResult()
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class GoogleSignInResult {
    data class Success(val isNewUser: Boolean, val displayName: String) : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

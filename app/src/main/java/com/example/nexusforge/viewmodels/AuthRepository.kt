package com.example.nexusforge.viewmodels

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            EmailExistsResult.Error(errorCode ?: "ERROR_GENERIC")
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
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            AuthResult.Error(errorCode ?: "ERROR_GENERIC")
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
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            AuthResult.Error(errorCode ?: "ERROR_GENERIC")
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
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            GoogleSignInResult.Error(errorCode ?: "ERROR_GENERIC")
        }
    }
    
    /**
     * Выход
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Проверка: авторизован ли пользователь через Google
     */
    fun isGoogleSignIn(): Boolean {
        val user = currentUser ?: return false
        val providers = user.providerData.map { it.providerId }
        return providers.contains("google.com")
    }
    
    /**
     * Обновление имени пользователя
     */
    suspend fun updateDisplayName(newName: String): UpdateResult {
        return try {
            val user = currentUser ?: return UpdateResult.Error("ERROR_USER_NOT_FOUND")
            val profileUpdates = userProfileChangeRequest { displayName = newName }
            user.updateProfile(profileUpdates)?.await()
            UpdateResult.Success
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            UpdateResult.Error(errorCode ?: "ERROR_GENERIC")
        }
    }
    
    /**
     * Обновление email пользователя (требует повторную авторизацию)
     */
    suspend fun updateEmail(newEmail: String, password: String): UpdateResult {
        return try {
            val user = currentUser ?: return UpdateResult.Error("ERROR_USER_NOT_FOUND")
            val credential = EmailAuthProvider.getCredential(user.email ?: "", password)
            user.reauthenticate(credential)?.await()
            user.updateEmail(newEmail)?.await()
            UpdateResult.Success
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            UpdateResult.Error(errorCode ?: "ERROR_GENERIC")
        }
    }
    
    /**
     * Удаление аккаунта
     */
    suspend fun deleteAccount(password: String): UpdateResult {
        return try {
            val user = currentUser ?: return UpdateResult.Error("ERROR_USER_NOT_FOUND")
            val credential = EmailAuthProvider.getCredential(user.email ?: "", password)
            user.reauthenticate(credential)?.await()
            user.delete()?.await()
            UpdateResult.Success
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            UpdateResult.Error(errorCode ?: "ERROR_GENERIC")
        }
    }
}

sealed class EmailExistsResult {
    object Exists : EmailExistsResult()
    object NotExists : EmailExistsResult()
    object GoogleOnly : EmailExistsResult()
    data class Error(val errorCode: String) : EmailExistsResult()
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val errorCode: String) : AuthResult()
}

sealed class GoogleSignInResult {
    data class Success(val isNewUser: Boolean, val displayName: String) : GoogleSignInResult()
    data class Error(val errorCode: String) : GoogleSignInResult()
}

sealed class UpdateResult {
    object Success : UpdateResult()
    data class Error(val errorCode: String) : UpdateResult()
}

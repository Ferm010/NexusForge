package com.ferm.nexusforge.viewmodels

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository {
    
    private val auth = FirebaseAuth.getInstance()
    
    val currentUser get() = auth.currentUser
    suspend fun checkEmailExists(email: String): EmailCheckResult {
        return try {
            android.util.Log.d("AuthRepository", "Checking if email exists: $email")
            
            // Генерируем случайный временный пароль
            val tempPassword = "TempCheck${System.currentTimeMillis()}!@#"
            
            try {
                // Пытаемся создать аккаунт с временным паролем
                val result = auth.createUserWithEmailAndPassword(email, tempPassword).await()
                
                // Если создание успешно - это новый email, удаляем временный аккаунт
                android.util.Log.d("AuthRepository", "Email is new - deleting temp account")
                result.user?.delete()?.await()
                auth.signOut()
                
                EmailCheckResult.NewUser
                
            } catch (e: FirebaseAuthException) {
                android.util.Log.d("AuthRepository", "Create account error: ${e.errorCode}")
                
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> {
                        // Email уже используется
                        // Не можем точно определить метод без входа, поэтому считаем что EMAIL_PASSWORD
                        // Если это Google аккаунт, Firebase вернет ошибку при попытке входа
                        android.util.Log.d("AuthRepository", "Email already in use  EMAIL_PASSWORD")
                        EmailCheckResult.ExistingUser(AuthMethod.EMAIL_PASSWORD)
                    }
                    "ERROR_INVALID_EMAIL" -> {
                        EmailCheckResult.Error("ERROR_INVALID_EMAIL")
                    }
                    else -> {
                        EmailCheckResult.Error(e.errorCode ?: "ERROR_GENERIC")
                    }
                }
            }
        } catch (_: Exception) {
            EmailCheckResult.Error("ERROR_GENERIC")
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
     * Вход через Google с проверкой конфликта методов авторизации
     */
    suspend fun signInWithGoogle(idToken: String): GoogleSignInResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            val displayName = result.user?.displayName ?: ""
            val email = result.user?.email.orEmpty()
            
            android.util.Log.d("AuthRepository", "Firebase user email: '${result.user?.email}'")
            android.util.Log.d("AuthRepository", "Firebase user displayName: '${result.user?.displayName}'")
            android.util.Log.d("AuthRepository", "Returning email: '$email'")
            
            GoogleSignInResult.Success(isNewUser, displayName, email)
        } catch (e: FirebaseAuthException) {
            // Проверяем конфликт методов авторизации
            when (e.errorCode) {
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> {
                    GoogleSignInResult.Error("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")
                }
                else -> {
                    GoogleSignInResult.Error(e.errorCode ?: "ERROR_GENERIC")
                }
            }
        } catch (_: Exception) {
            GoogleSignInResult.Error("ERROR_GENERIC")
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
            user.updateProfile(profileUpdates).await()
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
            user.reauthenticate(credential).await()
            user.delete().await()
            UpdateResult.Success
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            UpdateResult.Error(errorCode ?: "ERROR_GENERIC")
        }
    }
    
    /**
     * Отправка письма для восстановления пароля
     */
    suspend fun sendPasswordResetEmail(email: String): SendEmailResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            SendEmailResult.Success
        } catch (e: Exception) {
            val errorCode = (e as? FirebaseAuthException)?.errorCode
            SendEmailResult.Error(errorCode ?: "ERROR_GENERIC")
        }
    }
    
    /**
     * Проверка валидности текущей сессии
     */
    suspend fun validateSession(): Boolean {
        return try {
            val user = currentUser ?: return false
            user.reload().await()
            val tokenResult = user.getIdToken(false).await()
            tokenResult.token != null
        } catch (_: Exception) {
            false
        }
    }
    

}

// Enum для методов авторизации
enum class AuthMethod {
    EMAIL_PASSWORD,
    GOOGLE,
    OTHER
}

// Результат проверки email
sealed class EmailCheckResult {
    object NewUser : EmailCheckResult()
    data class ExistingUser(val authMethod: AuthMethod) : EmailCheckResult()
    data class Error(val errorCode: String) : EmailCheckResult()
}

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val errorCode: String) : AuthResult()
}

sealed class GoogleSignInResult {
    data class Success(val isNewUser: Boolean, val displayName: String, val email: String) : GoogleSignInResult()
    data class Error(val errorCode: String) : GoogleSignInResult()
}

sealed class UpdateResult {
    object Success : UpdateResult()
    data class Error(val errorCode: String) : UpdateResult()
}

sealed class SendEmailResult {
    object Success : SendEmailResult()
    data class Error(val errorCode: String) : SendEmailResult()
}

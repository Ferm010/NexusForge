package com.ferm.nexusforge.repository

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repository для работы с Google Drive API
 * Управляет авторизацией и загрузкой файлов в Google Drive
 */
class GoogleDriveRepository(private val context: Context) {

    private val _uploadProgress = MutableStateFlow<UploadProgress>(UploadProgress.Idle)
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress.asStateFlow()

    private var driveService: Drive? = null

    /**
     * Проверяет наличие интернет-соединения
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo?.isConnected == true
        }
    }

    /**
     * Проверяет, авторизован ли пользователь для доступа к Google Drive
     */
    fun isAuthorized(): Boolean {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            account != null && GoogleSignIn.hasPermissions(
                account,
                Scope(DriveScopes.DRIVE_FILE)
            )
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Создает Intent для запроса авторизации Google Drive
     */
    fun getAuthorizationIntent(): Intent {
        return try {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE_FILE))
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(context, signInOptions)
            client.signInIntent
        } catch (e: Exception) {
            Intent()
        }
    }

    /**
     * Инициализирует Drive Service после успешной авторизации
     */
    fun initializeDriveService(account: GoogleSignInAccount) {
        try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            driveService = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("NexusForge")
                .build()
        } catch (e: Exception) {
            driveService = null
        }
    }

    /**
     * Загружает ZIP файл в Google Drive
     * @param zipFile файл для загрузки
     * @param fileName имя файла в Google Drive
     * @return ID загруженного файла или null в случае ошибки
     */
    suspend fun uploadZipToDrive(zipFile: File, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Проверка интернет-соединения
            if (!isNetworkAvailable()) {
                _uploadProgress.value = UploadProgress.Error("No internet connection")
                return@withContext Result.failure(Exception("No internet connection"))
            }
            
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized. Please authorize first.")
            )

            // Проверка существования файла
            if (!zipFile.exists()) {
                _uploadProgress.value = UploadProgress.Error("File not found")
                return@withContext Result.failure(Exception("File not found: ${zipFile.path}"))
            }

            _uploadProgress.value = UploadProgress.Uploading(0f)

            // Метаданные файла
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                mimeType = "application/zip"
            }

            val mediaContent = FileContent("application/zip", zipFile)

            // Загрузка файла
            val file = service.files().create(fileMetadata, mediaContent)
                .setFields("id, name, webViewLink")
                .execute()

            _uploadProgress.value = UploadProgress.Success(file.id, file.webViewLink)

            Result.success(file.id)
        } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
            _uploadProgress.value = UploadProgress.Error("Authorization required. Please sign in again.")
            Result.failure(e)
        } catch (e: java.net.UnknownHostException) {
            _uploadProgress.value = UploadProgress.Error("Network error: Unable to reach Google Drive")
            Result.failure(e)
        } catch (e: java.net.SocketTimeoutException) {
            _uploadProgress.value = UploadProgress.Error("Upload timeout. Please check your connection.")
            Result.failure(e)
        } catch (e: Exception) {
            _uploadProgress.value = UploadProgress.Error(e.message ?: "Unknown error occurred")
            Result.failure(e)
        }
    }

    /**
     * Сбрасывает состояние прогресса загрузки
     */
    fun resetProgress() {
        _uploadProgress.value = UploadProgress.Idle
    }
}

/**
 * Состояния процесса загрузки в Google Drive
 */
sealed class UploadProgress {
    object Idle : UploadProgress()
    data class Uploading(val progress: Float) : UploadProgress()
    data class Success(val fileId: String, val webViewLink: String?) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}

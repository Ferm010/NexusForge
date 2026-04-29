package com.ferm.nexusforge.repository

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.FileContent
import com.google.api.client.http.HttpRequestInitializer
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

@Suppress("DEPRECATION")
class GoogleDriveRepository(private val context: Context) {

    private val _uploadProgress = MutableStateFlow<UploadProgress>(UploadProgress.Idle)
    val uploadProgress: StateFlow<UploadProgress> = _uploadProgress.asStateFlow()

    private var driveService: Drive? = null

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getAuthorizationIntent(): Intent = try {
        val webClientId = "380570057451-s8tckfhsh2frakmosma1di2ein26kq0u.apps.googleusercontent.com"
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestServerAuthCode(webClientId)
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(context, signInOptions)
        client.signOut()
        client.signInIntent
    } catch (_: Exception) {
        Intent()
    }

    fun initializeDriveService(account: GoogleSignInAccount) {
        driveService = if (account.account == null) {
            null
        } else {
            Thread {
                try {
                    val accessToken = getAccessToken(account)
                    driveService = if (accessToken != null) {
                        createDriveService(accessToken)
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    driveService = null
                }
            }.start()
            null
        }
    }

    private fun getAccessToken(account: GoogleSignInAccount): String? = try {
        GoogleAuthUtil.getToken(context, account.account!!, "oauth2:${DriveScopes.DRIVE_FILE}")
    } catch (_: Exception) {
        null
    }

    private fun createDriveService(accessToken: String): Drive {
        val requestInitializer = HttpRequestInitializer { request ->
            request.connectTimeout = 60000
            request.readTimeout = 120000
            request.headers.authorization = "Bearer $accessToken"
        }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer
        )
            .setApplicationName("NexusForge")
            .build()
    }

    suspend fun uploadZipToDrive(zipFile: File, fileName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (!isNetworkAvailable()) {
                _uploadProgress.value = UploadProgress.Error("No internet connection")
                return@withContext Result.failure(Exception("No internet connection"))
            }
            
            val service = driveService ?: return@withContext Result.failure(
                IllegalStateException("Drive service not initialized")
            )

            if (!zipFile.exists()) {
                _uploadProgress.value = UploadProgress.Error("File not found")
                return@withContext Result.failure(Exception("File not found: ${zipFile.path}"))
            }

            _uploadProgress.value = UploadProgress.Uploading(0f)

            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = fileName
                mimeType = "application/zip"
            }

            val file = service.files().create(fileMetadata, FileContent("application/zip", zipFile))
                .setFields("id, name, webViewLink")
                .execute()

            // Даём доступ к файлу по ссылке
            makeFilePublic(service, file.id)

            _uploadProgress.value = UploadProgress.Success(file.id, file.webViewLink)
            Result.success(file.id)
        } catch (e: com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException) {
            _uploadProgress.value = UploadProgress.Error("Authorization required")
            Result.failure(e)
        } catch (e: java.net.SocketTimeoutException) {
            _uploadProgress.value = UploadProgress.Error("Upload timeout")
            Result.failure(e)
        } catch (e: Exception) {
            _uploadProgress.value = UploadProgress.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    private fun makeFilePublic(service: Drive, fileId: String) {
        try {
            val permission = com.google.api.services.drive.model.Permission().apply {
                kind = "drive#permission"
                type = "anyone"
                role = "reader"
            }
            service.permissions().create(fileId, permission)
                .setFields("id")
                .execute()
        } catch (_: Exception) {
            // Игнорируем ошибки при установке прав доступа
        }
    }
}

sealed class UploadProgress {
    object Idle : UploadProgress()
    data class Uploading(val progress: Float) : UploadProgress()
    data class Success(val fileId: String, val webViewLink: String?) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}

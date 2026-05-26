package com.ferm.nexusforge.backend

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException

object NetworkUtils {

     // Проверка наличия интернет-соединения
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        //(WiFi/Mobile/Ethernet)
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

     // Реальная проверка доступа к интернету через HTTP запрос
    suspend fun hasInternetAccess(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                withTimeout(2000) {
                    val url = URL("https://www.google.com")
                    val connection = url.openConnection() as? HttpURLConnection
                        ?: return@withTimeout false
                    connection.connectTimeout = 1000
                    connection.readTimeout = 1000
                    connection.requestMethod = "HEAD"
                    connection.connect()
                    val responseCode = connection.responseCode
                    connection.disconnect()
                    responseCode == 200 || responseCode == 204
                }
            }
        } catch (_: Exception) {
            false
        }
    }


     // Проверка доменна
    suspend fun checkEmailDomainExists(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val domain = email.substringAfter("@", "")
                if (domain.isEmpty()) {
                    return@withContext false
                }

                // Добавить таймаут для быстрой проверки
                withTimeout(2000) {
                    val addresses = InetAddress.getAllByName(domain)
                    addresses.isNotEmpty()
                }
            } catch (_: UnknownHostException) {
                false
            } catch (_: TimeoutCancellationException) {
                true
            } catch (_: Exception) {
                true
            }
        }
    }
}

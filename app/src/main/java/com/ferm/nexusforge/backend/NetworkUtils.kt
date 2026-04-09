package com.ferm.nexusforge.backend

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException

object NetworkUtils {
    
    /**
     * Проверка наличия интернет-соединения
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            // Проверяем только наличие транспорта (WiFi/Mobile/Ethernet)
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    /**
     * Реальная проверка доступа к интернету через HTTP запрос
     */
    suspend fun hasInternetAccess(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                withTimeout(2000) {
                    val url = URL("https://www.google.com")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 1000
                    connection.readTimeout = 1000
                    connection.requestMethod = "HEAD"
                    connection.connect()
                    val responseCode = connection.responseCode
                    connection.disconnect()
                    responseCode == 200 || responseCode == 204
                }
            }
        } catch (e: Exception) {
            false  // Тихо возвращаем false, без логов
        }
    }


    /**
     * Проверка доменна
     */
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
            } catch (e: UnknownHostException) {
                android.util.Log.d("NetworkUtils", "Domain not found: ${e.message}")
                false
            } catch (e: TimeoutCancellationException) {
                android.util.Log.d("NetworkUtils", "DNS timeout")
                true
            } catch (e: Exception) {
                android.util.Log.e("NetworkUtils", "DNS error: ${e.message}", e)
                true
            }
        }
    }

    /**
     * Получить тип соединения (WiFi, Mobile, etc.)
     */
    fun getConnectionType(context: Context): ConnectionType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return ConnectionType.NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return ConnectionType.NONE
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NONE

            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                else -> ConnectionType.OTHER
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return ConnectionType.NONE
            @Suppress("DEPRECATION")
            return when (networkInfo.type) {
                ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                ConnectivityManager.TYPE_MOBILE -> ConnectionType.MOBILE
                ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                else -> ConnectionType.OTHER
            }
        }
    }

    enum class ConnectionType {
        WIFI, MOBILE, ETHERNET, OTHER, NONE
    }
}

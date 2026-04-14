package com.ferm.nexusforge.backend

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.ferm.nexusforge.BuildConfig
import java.security.MessageDigest

object SecurityCheck {
    
    // SHA-256 хеш вашей подписи
    private const val EXPECTED_SIGNATURE = "cb18947b4d94ad821ea0b3e5ecf96b96697632dc01a7b21f715adededbad6b84"
    
    /**
     * Проверка целостности приложения
     * Возвращает true если приложение не модифицировано
     * В debug режиме проверка пропускается
     */
    fun verifyAppIntegrity(context: Context): Boolean {
        // В debug режиме пропускаем проверку подписи
        if (BuildConfig.DEBUG) {
            return true
        }
        
        return try {
            val signatures = getSignatures(context)
            val currentSignature = signatures.firstOrNull()?.let { 
                hashSignature(it) 
            } ?: return false
            
            currentSignature == EXPECTED_SIGNATURE
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Проверка на root/эмулятор
     * В debug режиме проверка пропускается
     */
    fun isDeviceSecure(): Boolean {
        // В debug режиме пропускаем проверку
        if (BuildConfig.DEBUG) {
            return true
        }
        
        return !isRooted() && !isEmulator()
    }
    
    private fun getSignatures(context: Context): Array<Signature> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            packageInfo.signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            @Suppress("DEPRECATION")
            packageInfo.signatures ?: emptyArray()
        }
    }
    
    private fun hashSignature(signature: Signature): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(signature.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    private fun isRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        
        return paths.any { java.io.File(it).exists() }
    }
    
    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }
    
    /**
     * Получить текущий хеш подписи (для первой настройки)
     */
    fun getCurrentSignatureHash(context: Context): String {
        return try {
            val signatures = getSignatures(context)
            signatures.firstOrNull()?.let { hashSignature(it) } ?: "NO_SIGNATURE"
        } catch (e: Exception) {
            "ERROR: ${e.message}"
        }
    }
}

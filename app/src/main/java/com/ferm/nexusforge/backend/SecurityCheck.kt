package com.ferm.nexusforge.backend

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

object SecurityCheck {
    
    // SHA-256 хеш вашей подписи (заполните после первой сборки)
    private const val EXPECTED_SIGNATURE = "YOUR_SIGNATURE_HASH_HERE"
    
    /**
     * Проверка целостности приложения
     * Возвращает true если приложение не модифицировано
     */
    fun verifyAppIntegrity(context: Context): Boolean {
        if (EXPECTED_SIGNATURE == "YOUR_SIGNATURE_HASH_HERE") {
            // В debug режиме пропускаем проверку
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
     */
    fun isDeviceSecure(): Boolean {
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

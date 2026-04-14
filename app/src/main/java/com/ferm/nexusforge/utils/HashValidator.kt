package com.ferm.nexusforge.utils

import java.io.File
import java.security.MessageDigest

object HashValidator {
    
    /**
     * Проверяет SHA-256 хеш файла
     * @param file Файл для проверки
     * @param expectedHash Ожидаемый SHA-256 хеш (hex строка)
     * @return true если хеш совпадает, false иначе
     */
    fun validateSHA256(file: File, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val calculatedHash = calculateSHA256(file)
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }
    
    /**
     * Проверяет SHA-512 хеш файла
     * @param file Файл для проверки
     * @param expectedHash Ожидаемый SHA-512 хеш (hex строка)
     * @return true если хеш совпадает, false иначе
     */
    fun validateSHA512(file: File, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val calculatedHash = calculateSHA512(file)
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }
    
    /**
     * Проверяет SHA-1 хеш файла
     * @param file Файл для проверки
     * @param expectedHash Ожидаемый SHA-1 хеш (hex строка)
     * @return true если хеш совпадает, false иначе
     */
    fun validateSHA1(file: File, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val calculatedHash = calculateSHA1(file)
        return calculatedHash.equals(expectedHash, ignoreCase = true)
    }
    
    /**
     * Вычисляет SHA-256 хеш файла
     */
    fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Вычисляет SHA-512 хеш файла
     */
    fun calculateSHA512(file: File): String {
        val digest = MessageDigest.getInstance("SHA-512")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Вычисляет SHA-1 хеш файла
     */
    fun calculateSHA1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

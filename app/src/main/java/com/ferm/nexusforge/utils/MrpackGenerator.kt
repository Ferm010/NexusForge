package com.ferm.nexusforge.utils

import android.content.Context
import com.ferm.nexusforge.data.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class MrpackGenerator(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }
    
    /**
     * Генерирует .mrpack файл
     */
    suspend fun generateMrpack(
        modpackName: String,
        modpackDescription: String,
        minecraftVersion: String,
        modLoader: String,
        modLoaderVersion: String?,
        mods: List<ModReference>,
        onProgress: (Int, String) -> Unit
    ): File? {
        try {
            // Создаем временную директорию
            val tempDir = File(context.cacheDir, "mrpack_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()
            
            onProgress(0, "Preparing modpack structure...")
            
            // Создаем структуру директорий
            val overridesDir = File(tempDir, "overrides")
            overridesDir.mkdirs()
            
            // Собираем информацию о модах
            val files = mutableListOf<ModrinthFile>()
            
            mods.forEachIndexed { index, mod ->
                onProgress(index + 1, "Processing ${mod.title}...")
                
                android.util.Log.d("MrpackGenerator", "Processing mod: ${mod.title}")
                android.util.Log.d("MrpackGenerator", "  downloadUrl: ${mod.downloadUrl}")
                android.util.Log.d("MrpackGenerator", "  sha1: ${mod.sha1}")
                android.util.Log.d("MrpackGenerator", "  sha512: ${mod.sha512}")
                
                // Получаем информацию о файле мода
                val downloadUrl = mod.downloadUrl
                if (downloadUrl == null || downloadUrl.isEmpty()) {
                    android.util.Log.w("MrpackGenerator", "Skipping ${mod.title} - no download URL")
                    return@forEachIndexed
                }
                
                // Проверяем наличие хешей
                var sha1 = mod.sha1
                var sha512 = mod.sha512
                
                // Если хешей нет, вычисляем их (скачиваем файл)
                if (sha1.isNullOrEmpty() || sha512.isNullOrEmpty()) {
                    android.util.Log.w("MrpackGenerator", "Missing hashes for ${mod.title}, attempting to download and calculate...")
                    try {
                        val url = java.net.URL(downloadUrl)
                        val connection = url.openConnection()
                        connection.connectTimeout = 30000
                        connection.readTimeout = 30000
                        connection.connect()
                        
                        val tempFile = File.createTempFile("mod_", ".jar", context.cacheDir)
                        connection.getInputStream().use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        
                        sha1 = calculateSHA1(tempFile)
                        sha512 = calculateSHA512(tempFile)
                        tempFile.delete()
                        
                        android.util.Log.d("MrpackGenerator", "Calculated hashes for ${mod.title}: SHA1=$sha1, SHA512=$sha512")
                    } catch (e: Exception) {
                        android.util.Log.e("MrpackGenerator", "Failed to calculate hashes for ${mod.title}: ${e.message}")
                        e.printStackTrace()
                        return@forEachIndexed
                    }
                }
                
                // Финальная проверка - если хешей всё ещё нет, пропускаем мод
                if (sha1.isNullOrEmpty() || sha512.isNullOrEmpty()) {
                    android.util.Log.e("MrpackGenerator", "Skipping ${mod.title} - unable to obtain hashes (sha1=$sha1, sha512=$sha512)")
                    return@forEachIndexed
                }
                
                android.util.Log.d("MrpackGenerator", "Adding ${mod.title} to mrpack with hashes: SHA1=$sha1, SHA512=$sha512")
                
                files.add(
                    ModrinthFile(
                        path = "mods/${mod.fileName ?: "${mod.projectId}.jar"}",
                        hashes = ModrinthHashes(
                            sha1 = sha1,
                            sha512 = sha512
                        ),
                        env = ModrinthEnv(
                            client = "required",
                            server = "optional"
                        ),
                        downloads = listOf(downloadUrl),
                        fileSize = mod.fileSize ?: 0L
                    )
                )
            }
            
            // Создаем dependencies
            val dependencies = createDependencies(minecraftVersion, modLoader, modLoaderVersion)
            
            // Создаем modrinth.index.json
            val index = ModrinthIndex(
                formatVersion = 1,
                game = "minecraft",
                versionId = "1.0.0",
                name = modpackName,
                summary = modpackDescription,
                files = files,
                dependencies = dependencies
            )
            
            onProgress(mods.size + 1, "Creating index file...")
            
            // Записываем index в файл
            val indexFile = File(tempDir, "modrinth.index.json")
            val jsonString = json.encodeToString(index)
            
            // Логируем JSON для отладки
            android.util.Log.d("MrpackGenerator", "Generated JSON:")
            android.util.Log.d("MrpackGenerator", jsonString)
            
            indexFile.writeText(jsonString)
            
            // Создаем .mrpack (ZIP архив)
            onProgress(mods.size + 2, "Creating .mrpack archive...")
            
            val outputFile = File(
                context.getExternalFilesDir(null),
                "${modpackName.replace(" ", "_")}.mrpack"
            )
            
            ZipOutputStream(FileOutputStream(outputFile)).use { zipOut ->
                // Добавляем modrinth.index.json
                addFileToZip(zipOut, indexFile, "modrinth.index.json")
                
                // Добавляем overrides директорию (если есть файлы)
                if (overridesDir.exists() && overridesDir.listFiles()?.isNotEmpty() == true) {
                    addDirectoryToZip(zipOut, overridesDir, "overrides")
                }
            }
            
            // Удаляем временные файлы
            tempDir.deleteRecursively()
            
            onProgress(mods.size + 3, "Complete!")
            
            return outputFile
            
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    private fun createDependencies(
        minecraftVersion: String,
        modLoader: String,
        modLoaderVersion: String?
    ): ModrinthDependencies {
        // Если версия не указана, используем пустую строку вместо null
        val loaderVer = if (modLoaderVersion.isNullOrEmpty()) "" else modLoaderVersion
        
        return when (modLoader.lowercase()) {
            "forge" -> ModrinthDependencies(
                minecraft = minecraftVersion,
                forge = loaderVer
            )
            "fabric" -> ModrinthDependencies(
                minecraft = minecraftVersion,
                fabricLoader = loaderVer
            )
            "quilt" -> ModrinthDependencies(
                minecraft = minecraftVersion,
                quiltLoader = loaderVer
            )
            "neoforge" -> ModrinthDependencies(
                minecraft = minecraftVersion,
                neoforge = loaderVer
            )
            else -> ModrinthDependencies(
                minecraft = minecraftVersion
            )
        }
    }
    
    private fun addFileToZip(zipOut: ZipOutputStream, file: File, entryName: String) {
        zipOut.putNextEntry(ZipEntry(entryName))
        file.inputStream().use { input ->
            input.copyTo(zipOut)
        }
        zipOut.closeEntry()
    }
    
    private fun addDirectoryToZip(zipOut: ZipOutputStream, dir: File, basePath: String) {
        dir.listFiles()?.forEach { file ->
            val entryName = "$basePath/${file.name}"
            if (file.isDirectory) {
                addDirectoryToZip(zipOut, file, entryName)
            } else {
                addFileToZip(zipOut, file, entryName)
            }
        }
    }
    
    private fun calculateSHA1(file: File): String {
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
    
    private fun calculateSHA512(file: File): String {
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
}

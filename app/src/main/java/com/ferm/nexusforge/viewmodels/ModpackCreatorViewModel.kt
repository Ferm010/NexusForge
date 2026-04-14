package com.ferm.nexusforge.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.data.ModReference
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.data.ModpackMod
import com.ferm.nexusforge.data.ModDependencyInfo
import com.ferm.nexusforge.network.ModrinthApi
import com.ferm.nexusforge.repository.FirestoreRepository
import com.ferm.nexusforge.repository.GoogleDriveRepository
import com.ferm.nexusforge.utils.MrpackGenerator
import com.ferm.nexusforge.utils.NetworkChecker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Comparator
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class Modpack(
    val id: String = "",
    val name: String = "",
    val minecraftVersion: String = "",
    val mods: List<ModpackMod> = emptyList(),
    val authorId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class ModpackCreatorViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(ModpackCreatorState())
    val state: StateFlow<ModpackCreatorState> = _state.asStateFlow()
    
    // Ленивая инициализация - создаются только при первом использовании
    private val firestoreRepository: FirestoreRepository by lazy {
        FirestoreRepository()
    }
    private var googleDriveRepository: GoogleDriveRepository? = null
    private var networkChecker: NetworkChecker? = null
    
    fun initializeNetworkChecker(context: Context) {
        networkChecker = NetworkChecker(context)
    }
    
    fun initializeGoogleDrive(context: Context) {
        googleDriveRepository = GoogleDriveRepository(context)
    }
    
    fun getGoogleDriveRepository(): GoogleDriveRepository? = googleDriveRepository
    
    init {
        loadMinecraftVersions()
    }
    
    fun loadMinecraftVersions() {
        viewModelScope.launch {
            try {
                if (networkChecker?.isNetworkAvailable() == false) {
                    _state.value = _state.value.copy(
                        error = "Проблема сети. Проверьте подключение к интернету.",
                        availableMinecraftVersions = listOf("1.21", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.18.2", "1.18.1", "1.17.1", "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1")
                    )
                    return@launch
                }
                
                val versions = withContext(Dispatchers.IO) {
                    ModrinthApi.retrofitService.getGameVersions()
                }
                val mcVersions = versions
                    .filter { it.versionType == "release" }
                    .map { it.version }
                    .distinct()
                    .sortedWith(compareVersions)
                _state.value = _state.value.copy(
                    availableMinecraftVersions = mcVersions
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Проблема сети. Проверьте подключение к интернету.",
                    availableMinecraftVersions = listOf("1.21", "1.20.4", "1.20.2", "1.20.1", "1.19.4", "1.19.3", "1.19.2", "1.19.1", "1.18.2", "1.18.1", "1.17.1", "1.16.5", "1.16.4", "1.16.3", "1.16.2", "1.16.1")
                )
            }
        }
    }
    
    private val compareVersions = compareByDescending<String> { version ->
        val parts = version.split(".")
        val major = parts.getOrNull(0)?.replace("1.", "")?.toIntOrNull() ?: 0
        val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
        major * 10000 + minor * 100 + patch
    }
    
    fun searchMods() {
        val query = _state.value.searchQuery
        val minecraftVersion = _state.value.selectedMinecraftVersion
        val modLoader = _state.value.selectedModLoader
        
        if (query.length < 2 || minecraftVersion.isEmpty()) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true)
            try {
                if (networkChecker?.isNetworkAvailable() == false) {
                    _state.value = _state.value.copy(
                        isSearching = false,
                        error = "Проблема сети. Проверьте подключение к интернету."
                    )
                    return@launch
                }
                
                val facetsBuilder = StringBuilder("[[\"versions:$minecraftVersion\"],[\"categories:$modLoader\"]]")
                val response = withContext(Dispatchers.IO) {
                    ModrinthApi.retrofitService.searchProjects(
                        query = query,
                        facets = facetsBuilder.toString(),
                        limit = 40
                    )
                }
                _state.value = _state.value.copy(
                    searchResults = response.hits,
                    isSearching = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = "Проблема сети. Проверьте подключение к интернету."
                )
            }
        }
    }
    
    fun updateModLoader(modLoader: String) {
        if (_state.value.selectedMods.isNotEmpty() && _state.value.selectedModLoader != modLoader) {
            _state.value = _state.value.copy(showModLoaderWarning = true, pendingModLoader = modLoader)
        } else {
            _state.value = _state.value.copy(selectedModLoader = modLoader)
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    fun confirmModLoaderChange() {
        _state.value = _state.value.copy(
            selectedModLoader = _state.value.pendingModLoader,
            selectedMods = emptyList(),
            showModLoaderWarning = false,
            pendingModLoader = ""
        )
    }
    
    fun cancelModLoaderChange() {
        _state.value = _state.value.copy(
            showModLoaderWarning = false,
            pendingModLoader = ""
        )
    }
    
    fun addMod(mod: ModrinthProject) {
        viewModelScope.launch {
            addModInternal(mod)
        }
    }
    
    suspend fun addModSuspend(mod: ModrinthProject) {
        addModInternal(mod)
    }
    
    private suspend fun addModInternal(mod: ModrinthProject) {
        var downloadUrl = ""
        var modVersion = ""
        var fileName: String? = null
        var fileSize: Long? = null
        var sha1: String? = null
        var sha512: String? = null
        
        try {
            if (networkChecker?.isNetworkAvailable() == false) {
                _state.value = _state.value.copy(
                    error = "Проблема сети. Проверьте подключение к интернету."
                )
                return
            }
            
            val versions = withContext(Dispatchers.IO) {
                ModrinthApi.retrofitService.getProjectVersions(mod.actualProjectId)
            }
            
            val mcVersion = _state.value.selectedMinecraftVersion
            val modLoader = _state.value.selectedModLoader
            
            val matchingVersion = versions.firstOrNull { version ->
                val versionsList = version.gameVersion.ifEmpty { version.gameVersions ?: emptyList() }
                val hasMatchingVersion = versionsList.any { it == mcVersion }
                val hasMatchingLoader = version.loaders.any { it.equals(modLoader, ignoreCase = true) }
                hasMatchingVersion && hasMatchingLoader
            }
            
            if (matchingVersion != null) {
                val file = matchingVersion.files.firstOrNull()
                downloadUrl = file?.url ?: ""
                modVersion = matchingVersion.versionNumber
                fileName = file?.filename
                fileSize = file?.size
                
                // Получаем хеши из API
                sha1 = file?.hashes?.get("sha1")
                sha512 = file?.hashes?.get("sha512")
            } else {
                // Try to get first available version
                val firstVersion = versions.firstOrNull()
                if (firstVersion != null) {
                    val file = firstVersion.files.firstOrNull()
                    downloadUrl = file?.url ?: ""
                    modVersion = firstVersion.versionNumber
                    fileName = file?.filename
                    fileSize = file?.size
                    sha1 = file?.hashes?.get("sha1")
                    sha512 = file?.hashes?.get("sha512")
                }
            }
        } catch (e: Exception) {
            // Ошибка при получении версий
        }
        
        val currentMods = _state.value.selectedMods.toMutableList()
        if (currentMods.none { it.projectId == mod.actualProjectId }) {
            currentMods.add(
                ModpackMod(
                    projectId = mod.actualProjectId,
                    name = mod.title,
                    version = modVersion,
                    downloadUrl = downloadUrl,
                    iconUrl = mod.iconUrl,
                    fileName = fileName,
                    fileSize = fileSize,
                    sha1 = sha1,
                    sha512 = sha512
                )
            )
            _state.value = _state.value.copy(selectedMods = currentMods)
            
            // Получаем и добавляем зависимости
            addDependencies(mod.actualProjectId)
        }
    }
    
    private fun addDependencies(projectId: String) {
        viewModelScope.launch {
            try {
                val deps = withContext(Dispatchers.IO) {
                    ModrinthApi.retrofitService.getDependencies(projectId)
                }
                
                // Получаем обязательные зависимости
                val requiredDeps = mutableListOf<ModDependencyInfo>()
                for (version in deps.versions) {
                    for (dep in version.dependencies) {
                        if (dep.dependencyType == "required" && dep.projectId != null) {
                            if (requiredDeps.none { it.projectId == dep.projectId }) {
                                requiredDeps.add(dep)
                            }
                        }
                    }
                }
                
                // Добавляем каждую зависимость
                for (dep in requiredDeps) {
                    val depProject = deps.projects.find { it.actualProjectId == dep.projectId }
                    if (depProject != null) {
                        addModDependency(depProject)
                    }
                }
            } catch (e: Exception) {
                // Зависимости не найдены или ошибка
            }
        }
    }
    
    private suspend fun addModDependency(mod: ModrinthProject) {
        var downloadUrl = ""
        var modVersion = ""
        var fileName: String? = null
        var fileSize: Long? = null
        var sha1: String? = null
        var sha512: String? = null
        
        try {
            val versions = withContext(Dispatchers.IO) {
                ModrinthApi.retrofitService.getProjectVersions(mod.actualProjectId)
            }
            
            val mcVersion = _state.value.selectedMinecraftVersion
            val modLoader = _state.value.selectedModLoader
            
            val matchingVersion = versions.firstOrNull { version ->
                val versionsList = version.gameVersion.ifEmpty { version.gameVersions ?: emptyList() }
                val hasMatchingVersion = versionsList.any { it == mcVersion }
                val hasMatchingLoader = version.loaders.any { it.equals(modLoader, ignoreCase = true) }
                hasMatchingVersion && hasMatchingLoader
            } ?: versions.firstOrNull()
            
            if (matchingVersion != null) {
                val file = matchingVersion.files.firstOrNull()
                downloadUrl = file?.url ?: ""
                modVersion = matchingVersion.versionNumber
                fileName = file?.filename
                fileSize = file?.size
                sha1 = file?.hashes?.get("sha1")
                sha512 = file?.hashes?.get("sha512")
            }
        } catch (e: Exception) {
            // Ошибка при получении версий зависимости
        }
        
        val currentMods = _state.value.selectedMods.toMutableList()
        if (currentMods.none { it.projectId == mod.actualProjectId }) {
            currentMods.add(
                ModpackMod(
                    projectId = mod.actualProjectId,
                    name = mod.title,
                    version = modVersion,
                    downloadUrl = downloadUrl,
                    iconUrl = mod.iconUrl,
                    fileName = fileName,
                    fileSize = fileSize,
                    sha1 = sha1,
                    sha512 = sha512
                )
            )
            _state.value = _state.value.copy(selectedMods = currentMods)
        }
    }
    
    fun removeMod(projectId: String) {
        val currentMods = _state.value.selectedMods.toMutableList()
        currentMods.removeAll { it.projectId == projectId }
        _state.value = _state.value.copy(selectedMods = currentMods)
    }
    
    fun clearAllMods() {
        _state.value = _state.value.copy(selectedMods = emptyList())
    }
    
    fun addModDirectly(mod: ModpackMod) {
        val currentMods = _state.value.selectedMods.toMutableList()
        if (currentMods.none { it.projectId == mod.projectId }) {
            currentMods.add(mod)
            _state.value = _state.value.copy(selectedMods = currentMods)
        }
    }
    
    fun updateModpackName(name: String) {
        // БЕЗОПАСНОСТЬ: Санитизируем имя модпака для предотвращения path traversal атак
        val sanitizedName = com.ferm.nexusforge.utils.InputSanitizer.sanitizeModpackName(name)
        _state.value = _state.value.copy(modpackName = sanitizedName)
    }
    
    fun updateMinecraftVersion(version: String) {
        if (_state.value.selectedMods.isNotEmpty() && _state.value.selectedMinecraftVersion != version) {
            _state.value = _state.value.copy(showVersionWarning = true, pendingVersion = version)
        } else {
            _state.value = _state.value.copy(
                selectedMinecraftVersion = version,
                searchResults = emptyList(),
                searchQuery = ""
            )
        }
    }
    
    fun confirmVersionChange() {
        _state.value = _state.value.copy(
            selectedMinecraftVersion = _state.value.pendingVersion,
            selectedMods = emptyList(),
            searchResults = emptyList(),
            searchQuery = "",
            showVersionWarning = false,
            pendingVersion = ""
        )
    }
    
    fun cancelVersionChange() {
        _state.value = _state.value.copy(
            showVersionWarning = false,
            pendingVersion = ""
        )
    }
    
    fun clearSearchResults() {
        _state.value = _state.value.copy(
            searchResults = emptyList(),
            searchQuery = ""
        )
    }
    
    fun resetState() {
        _state.value = ModpackCreatorState(
            availableMinecraftVersions = _state.value.availableMinecraftVersions
        )
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
    
    fun generateModpack(context: Context, onComplete: (String) -> Unit) {
        val currentState = _state.value
        if (currentState.modpackName.isEmpty() || 
            currentState.selectedMinecraftVersion.isEmpty() || 
            currentState.selectedMods.isEmpty()) {
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)
            
            val modpackId = if (currentState.modpackId.isNotEmpty()) {
                currentState.modpackId
            } else {
                UUID.randomUUID().toString()
            }
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            
            try {
                withContext(Dispatchers.IO) {
                    val zipFile = createZipFile(currentState)
                    shareZipFile(context, zipFile, currentState.modpackName)
                }
                
                val modpackData = hashMapOf(
                    "id" to modpackId,
                    "name" to currentState.modpackName,
                    "minecraftVersion" to currentState.selectedMinecraftVersion,
                    "modLoader" to currentState.selectedModLoader,
                    "mods" to currentState.selectedMods.map { hashMapOf(
                        "projectId" to it.projectId,
                        "title" to it.name,
                        "version" to it.version,
                        "downloadUrl" to it.downloadUrl,
                        "iconUrl" to it.iconUrl,
                        "fileName" to it.fileName,
                        "fileSize" to it.fileSize,
                        "sha1" to it.sha1,
                        "sha512" to it.sha512
                    )},
                    "authorId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "isCustom" to true,
                    "isFavorite" to true
                )
                
                firestoreRepository.saveCustomModpack(modpackId, modpackData)
                
                _state.value = _state.value.copy(isGenerating = false)
                onComplete(modpackId)
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGenerating = false,
                    error = e.message
                )
            }
        }
    }
    
    fun saveModpackOnly(onComplete: () -> Unit) {
        val currentState = _state.value
        if (currentState.modpackName.isEmpty() || currentState.selectedMinecraftVersion.isEmpty()) {
            return
        }
        
        // Проверка сети перед сохранением
        if (networkChecker?.isNetworkAvailable() == false) {
            _state.value = _state.value.copy(
                error = "Проблема сети. Проверьте подключение к интернету."
            )
            return
        }
        
        viewModelScope.launch {
            try {
                val modpackId = if (currentState.modpackId.isNotEmpty()) {
                    currentState.modpackId
                } else {
                    UUID.randomUUID().toString()
                }
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                val modpackData = hashMapOf(
                    "id" to modpackId,
                    "name" to currentState.modpackName,
                    "minecraftVersion" to currentState.selectedMinecraftVersion,
                    "modLoader" to currentState.selectedModLoader,
                    "mods" to currentState.selectedMods.map { hashMapOf(
                        "projectId" to it.projectId,
                        "title" to it.name,
                        "version" to it.version,
                        "downloadUrl" to it.downloadUrl,
                        "iconUrl" to it.iconUrl,
                        "fileName" to it.fileName,
                        "fileSize" to it.fileSize,
                        "sha1" to it.sha1,
                        "sha512" to it.sha512
                    )},
                    "authorId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "isCustom" to true,
                    "isFavorite" to true
                )
                
                firestoreRepository.saveCustomModpack(modpackId, modpackData)
                _state.value = _state.value.copy(modpackId = modpackId)
                onComplete()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
    
    fun generateModpackWithProgress(
        context: Context,
        onProgress: (currentStep: Int, modName: String, isComplete: Boolean, error: String?) -> Unit
    ) {
        val currentState = _state.value
        if (currentState.modpackName.isEmpty() || 
            currentState.selectedMinecraftVersion.isEmpty() || 
            currentState.selectedMods.isEmpty()) {
            onProgress(0, "", true, "Invalid state")
            return
        }
        
        // Проверка сети перед генерацией
        if (networkChecker?.isNetworkAvailable() == false) {
            _state.value = _state.value.copy(
                error = "Проблема сети. Проверьте подключение к интернету."
            )
            onProgress(0, "", true, "Проблема сети. Проверьте подключение к интернету.")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)
            
            try {
                onProgress(1, "Preparing...", false, null)
                
                val modpackId = if (currentState.modpackId.isNotEmpty()) {
                    currentState.modpackId
                } else {
                    UUID.randomUUID().toString()
                }
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                var step = 2
                val totalMods = currentState.selectedMods.size
                
                withContext(Dispatchers.IO) {
                    val zipFile = createZipFileWithProgress(currentState) { modName ->
                        onProgress(step, modName, false, null)
                        step++
                    }
                    
                    onProgress(step, "Creating archive...", false, null)
                    step++
                    
                    saveToDownloads(context, zipFile, currentState.modpackName)
                }
                
                onProgress(step, "", false, null)
                
                val modpackData = hashMapOf(
                    "id" to modpackId,
                    "name" to currentState.modpackName,
                    "minecraftVersion" to currentState.selectedMinecraftVersion,
                    "modLoader" to currentState.selectedModLoader,
                    "mods" to currentState.selectedMods.map { hashMapOf(
                        "projectId" to it.projectId,
                        "title" to it.name,
                        "version" to it.version,
                        "downloadUrl" to it.downloadUrl,
                        "iconUrl" to it.iconUrl,
                        "fileName" to it.fileName,
                        "fileSize" to it.fileSize,
                        "sha1" to it.sha1,
                        "sha512" to it.sha512
                    )},
                    "authorId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "isCustom" to true,
                    "isFavorite" to true
                )
                
                firestoreRepository.saveCustomModpack(modpackId, modpackData)
                
                _state.value = _state.value.copy(isGenerating = false)
                onProgress(step, "", true, null)
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGenerating = false,
                    error = e.message
                )
                onProgress(0, "", true, e.message)
            }
        }
    }
    
    fun generateMrpackWithProgress(
        context: Context,
        onProgress: (currentStep: Int, modName: String, isComplete: Boolean, error: String?) -> Unit
    ) {
        val currentState = _state.value
        if (currentState.modpackName.isEmpty() || 
            currentState.selectedMinecraftVersion.isEmpty() || 
            currentState.selectedMods.isEmpty()) {
            onProgress(0, "", true, "Invalid state")
            return
        }
        
        // Проверка сети перед генерацией
        if (networkChecker?.isNetworkAvailable() == false) {
            _state.value = _state.value.copy(
                error = "Проблема сети. Проверьте подключение к интернету."
            )
            onProgress(0, "", true, "Проблема сети. Проверьте подключение к интернету.")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isGenerating = true)
            
            try {
                onProgress(1, "Preparing mrpack...", false, null)
                
                val modpackId = if (currentState.modpackId.isNotEmpty()) {
                    currentState.modpackId
                } else {
                    UUID.randomUUID().toString()
                }
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                
                // Получаем последнюю версию модлоадера
                val modLoaderVersion = try {
                    withContext(Dispatchers.IO) {
                        getLatestLoaderVersion(currentState.selectedModLoader, currentState.selectedMinecraftVersion)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ModpackCreator", "Failed to get loader version: ${e.message}")
                    null
                }
                
                android.util.Log.d("ModpackCreator", "Using modloader version: $modLoaderVersion")
                
                // Конвертируем ModpackMod в ModReference с необходимыми данными
                val modReferences = currentState.selectedMods.map { mod ->
                    ModReference(
                        projectId = mod.projectId,
                        title = mod.name,
                        iconUrl = mod.iconUrl,
                        required = true,
                        downloadUrl = mod.downloadUrl,
                        fileName = mod.fileName ?: "${mod.name.replace(" ", "_")}.jar",
                        fileSize = mod.fileSize ?: 0L,
                        sha1 = mod.sha1,
                        sha512 = mod.sha512
                    )
                }
                
                val mrpackGenerator = MrpackGenerator(context)
                
                withContext(Dispatchers.IO) {
                    val mrpackFile = mrpackGenerator.generateMrpack(
                        modpackName = currentState.modpackName,
                        modpackDescription = currentState.modpackDescription,
                        minecraftVersion = currentState.selectedMinecraftVersion,
                        modLoader = currentState.selectedModLoader,
                        modLoaderVersion = modLoaderVersion,
                        mods = modReferences,
                        onProgress = { step, modName ->
                            onProgress(step, modName, false, null)
                        }
                    )
                    
                    if (mrpackFile != null) {
                        onProgress(currentState.selectedMods.size + 3, "Saving to downloads...", false, null)
                        saveToDownloads(context, mrpackFile, currentState.modpackName)
                    } else {
                        throw Exception("Failed to generate mrpack")
                    }
                }
                
                val modpackData = hashMapOf(
                    "id" to modpackId,
                    "name" to currentState.modpackName,
                    "minecraftVersion" to currentState.selectedMinecraftVersion,
                    "modLoader" to currentState.selectedModLoader,
                    "mods" to currentState.selectedMods.map { hashMapOf(
                        "projectId" to it.projectId,
                        "title" to it.name,
                        "version" to it.version,
                        "downloadUrl" to it.downloadUrl,
                        "iconUrl" to it.iconUrl,
                        "fileName" to it.fileName,
                        "fileSize" to it.fileSize,
                        "sha1" to it.sha1,
                        "sha512" to it.sha512
                    )},
                    "authorId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis(),
                    "isCustom" to true,
                    "isFavorite" to true
                )
                
                firestoreRepository.saveCustomModpack(modpackId, modpackData)
                
                _state.value = _state.value.copy(isGenerating = false)
                onProgress(currentState.selectedMods.size + 4, "", true, null)
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGenerating = false,
                    error = e.message
                )
                onProgress(0, "", true, e.message)
            }
        }
    }
    
    private suspend fun createZipFileWithProgress(
        state: ModpackCreatorState,
        onModProgress: (modName: String) -> Unit
    ): File {
        val cacheDir = File(System.getProperty("java.io.tmpdir"))
        val zipFile = File(cacheDir, "${state.modpackName}.zip")
        
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            state.selectedMods.forEach { mod ->
                onModProgress(mod.name)
                try {
                    if (mod.downloadUrl.isNotEmpty()) {
                        val url = java.net.URL(mod.downloadUrl)
                        val connection = url.openConnection()
                        connection.connectTimeout = 15000
                        connection.readTimeout = 15000
                        connection.connect()
                        
                        val inputStream = connection.getInputStream()
                        val fileName = "${mod.name.replace(" ", "_")}.jar"
                        
                        zos.putNextEntry(ZipEntry(fileName))
                        inputStream.copyTo(zos)
                        zos.closeEntry()
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    // Ошибка при скачивании мода
                }
            }
        }
        
        return zipFile
    }
    
    private fun saveToDownloads(context: Context, zipFile: File, modpackName: String) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$modpackName.zip")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Save Modpack"))
    }
    
    private suspend fun createZipFile(state: ModpackCreatorState): File {
        val cacheDir = File(System.getProperty("java.io.tmpdir"))
        val zipFile = File(cacheDir, "${state.modpackName}.zip")
        
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            state.selectedMods.forEach { mod ->
                try {
                    if (mod.downloadUrl.isNotEmpty()) {
                        val url = java.net.URL(mod.downloadUrl)
                        val connection = url.openConnection()
                        connection.connect()
                        
                        val inputStream = connection.getInputStream()
                        val fileName = "${mod.name}.jar"
                        
                        zos.putNextEntry(ZipEntry(fileName))
                        inputStream.copyTo(zos)
                        zos.closeEntry()
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    // Skip this mod if download fails
                }
            }
        }
        
        return zipFile
    }
    
    private fun shareZipFile(context: Context, zipFile: File, modpackName: String) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "$modpackName.zip")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Export Modpack"))
    }
    
    fun clearState() {
        _state.value = ModpackCreatorState()
        loadMinecraftVersions()
    }
    
    fun setModpackId(id: String) {
        _state.value = _state.value.copy(modpackId = id)
    }
    
    private suspend fun getLatestLoaderVersion(modLoader: String, minecraftVersion: String): String? {
        return try {
            when (modLoader.lowercase()) {
                "forge" -> {
                    // Получаем версии Forge для данной версии Minecraft
                    // Modrinth API: /tag/loader/{loader}/versions
                    val response = ModrinthApi.retrofitService.searchProjects(
                        query = "forge",
                        facets = "[[\"project_type:modpack\"],[\"versions:$minecraftVersion\"]]",
                        limit = 1
                    )
                    // Для простоты возвращаем null, так как Modrinth API не предоставляет прямой доступ к версиям лоадеров
                    // Можно использовать Forge API или другой источник
                    null
                }
                "fabric" -> {
                    // Аналогично для Fabric
                    null
                }
                "neoforge" -> {
                    null
                }
                "quilt" -> {
                    null
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Экспорт сборки в Google Drive
     */
    fun exportToGoogleDrive(
        context: Context,
        onProgress: (current: Int, modName: String, isComplete: Boolean, error: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isGenerating = true, error = null)
                
                val currentState = _state.value
                if (currentState.selectedMods.isEmpty()) {
                    _state.value = _state.value.copy(
                        isGenerating = false,
                        error = "No mods selected"
                    )
                    onProgress(0, "", true, "No mods selected")
                    return@launch
                }
                
                // Создаем ZIP файл
                val zipFile = withContext(Dispatchers.IO) {
                    createZipFileWithProgress(currentState) { modName ->
                        val progress = (currentState.selectedMods.indexOf(
                            currentState.selectedMods.find { it.name == modName }
                        ) + 1) * 100 / currentState.selectedMods.size
                        onProgress(progress, modName, false, null)
                    }
                }
                
                // Загружаем в Google Drive
                val driveRepo = googleDriveRepository
                if (driveRepo == null) {
                    _state.value = _state.value.copy(
                        isGenerating = false,
                        error = "Google Drive not initialized"
                    )
                    onProgress(0, "", true, "Google Drive not initialized")
                    return@launch
                }
                
                val fileName = "${currentState.modpackName}.zip"
                val result = driveRepo.uploadZipToDrive(zipFile, fileName)
                
                if (result.isSuccess) {
                    _state.value = _state.value.copy(isGenerating = false)
                    onProgress(100, "", true, null)
                } else {
                    _state.value = _state.value.copy(
                        isGenerating = false,
                        error = result.exceptionOrNull()?.message
                    )
                    onProgress(0, "", true, result.exceptionOrNull()?.message)
                }
                
                // Удаляем временный файл
                zipFile.delete()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGenerating = false,
                    error = e.message
                )
                onProgress(0, "", true, e.message)
            }
        }
    }
}

data class ModpackCreatorState(
    val modpackId: String = "",
    val modpackName: String = "",
    val modpackDescription: String = "",
    val selectedMinecraftVersion: String = "",
    val selectedModLoader: String = "",
    val availableMinecraftVersions: List<String> = emptyList(),
    val availableModLoaders: List<String> = listOf("fabric", "forge", "quilt", "neoforge"),
    val searchQuery: String = "",
    val searchResults: List<ModrinthProject> = emptyList(),
    val selectedMods: List<ModpackMod> = emptyList(),
    val isSearching: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val showVersionWarning: Boolean = false,
    val pendingVersion: String = "",
    val showModLoaderWarning: Boolean = false,
    val pendingModLoader: String = ""
)

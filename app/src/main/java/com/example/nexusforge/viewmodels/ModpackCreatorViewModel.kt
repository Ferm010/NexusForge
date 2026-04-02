package com.example.nexusforge.viewmodels

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusforge.data.ModrinthProject
import com.example.nexusforge.data.ModpackMod
import com.example.nexusforge.network.ModrinthApi
import com.example.nexusforge.repository.FirestoreRepository
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
    
    private val firestoreRepository = FirestoreRepository()
    
    init {
        loadMinecraftVersions()
    }
    
    fun loadMinecraftVersions() {
        viewModelScope.launch {
            try {
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
                    error = e.message
                )
            }
        }
    }
    
    fun updateModLoader(modLoader: String) {
        _state.value = _state.value.copy(selectedModLoader = modLoader)
    }
    
    fun addMod(mod: ModrinthProject) {
        android.util.Log.d("ModpackCreator", "=== addMod called for: ${mod.title} (${mod.actualProjectId}) ===")
        viewModelScope.launch {
            var downloadUrl = ""
            var modVersion = ""
            
            try {
                android.util.Log.d("ModpackCreator", "Fetching versions for: ${mod.actualProjectId}")
                
                val versions = withContext(Dispatchers.IO) {
                    ModrinthApi.retrofitService.getProjectVersions(mod.actualProjectId)
                }
                
                android.util.Log.d("ModpackCreator", "Found ${versions.size} versions for ${mod.projectId}")
                
                val mcVersion = _state.value.selectedMinecraftVersion
                val modLoader = _state.value.selectedModLoader
                android.util.Log.d("ModpackCreator", "Looking for MC version: $mcVersion, modLoader: $modLoader")
                
                val matchingVersion = versions.firstOrNull { version ->
                    val versionsList = version.gameVersion.ifEmpty { version.gameVersions ?: emptyList() }
                    val hasMatchingVersion = versionsList.any { it == mcVersion }
                    val hasMatchingLoader = version.loaders.any { it.equals(modLoader, ignoreCase = true) }
                    hasMatchingVersion && hasMatchingLoader
                }
                
                if (matchingVersion != null) {
                    android.util.Log.d("ModpackCreator", "Found matching version: ${matchingVersion.versionNumber}")
                    downloadUrl = matchingVersion.files.firstOrNull()?.url ?: ""
                    modVersion = matchingVersion.versionNumber
                    android.util.Log.d("ModpackCreator", "Download URL: $downloadUrl")
                } else {
                    // Try to get first available version
                    val firstVersion = versions.firstOrNull()
                    if (firstVersion != null) {
                        downloadUrl = firstVersion.files.firstOrNull()?.url ?: ""
                        modVersion = firstVersion.versionNumber
                        android.util.Log.d("ModpackCreator", "Using first version: $modVersion, URL: $downloadUrl")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ModpackCreator", "Error fetching versions: ${e.message}")
            }
            
            android.util.Log.d("ModpackCreator", "Final modVersion: '$modVersion'")
            
            val currentMods = _state.value.selectedMods.toMutableList()
            if (currentMods.none { it.projectId == mod.actualProjectId }) {
                currentMods.add(
                    ModpackMod(
                        projectId = mod.actualProjectId,
                        name = mod.title,
                        version = modVersion,
                        downloadUrl = downloadUrl,
                        iconUrl = mod.iconUrl
                    )
                )
                android.util.Log.d("ModpackCreator", "Added mod with version: '$modVersion'")
                _state.value = _state.value.copy(selectedMods = currentMods)
            } else {
                android.util.Log.d("ModpackCreator", "Mod already exists in list")
            }
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
        _state.value = _state.value.copy(modpackName = name)
    }
    
    fun updateMinecraftVersion(version: String) {
        _state.value = _state.value.copy(
            selectedMinecraftVersion = version,
            searchResults = emptyList(),
            searchQuery = ""
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
                    delay(2000) // 2 second delay before sharing
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
                        "iconUrl" to it.iconUrl
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
                        "iconUrl" to it.iconUrl
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
                        "iconUrl" to it.iconUrl
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
                        android.util.Log.d("ModpackCreator", "Downloading: ${mod.name} from ${mod.downloadUrl}")
                        
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
                        
                        android.util.Log.d("ModpackCreator", "Successfully downloaded: ${mod.name}")
                    } else {
                        android.util.Log.w("ModpackCreator", "Empty download URL for: ${mod.name}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ModpackCreator", "Failed to download ${mod.name}: ${e.message}")
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
}

data class ModpackCreatorState(
    val modpackId: String = "",
    val modpackName: String = "",
    val selectedMinecraftVersion: String = "",
    val selectedModLoader: String = "",
    val availableMinecraftVersions: List<String> = emptyList(),
    val availableModLoaders: List<String> = listOf("fabric", "forge", "quilt", "neoforge"),
    val searchQuery: String = "",
    val searchResults: List<ModrinthProject> = emptyList(),
    val selectedMods: List<ModpackMod> = emptyList(),
    val isSearching: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null
)

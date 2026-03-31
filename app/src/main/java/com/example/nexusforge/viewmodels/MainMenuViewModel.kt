package com.example.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusforge.data.GameVersion
import com.example.nexusforge.data.ModrinthProject
import com.example.nexusforge.network.ModrinthApi
import kotlinx.coroutines.launch

enum class SearchMode {
    MODPACK,
    MOD
}

enum class SortOption(val apiValue: String, val needsClientSort: Boolean = false) {
    RELEVANCE("relevance"),
    DOWNLOADS_DESC("downloads"),
    DOWNLOADS_ASC("downloads", needsClientSort = true),
    NEWEST("newest")
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val projects: List<ModrinthProject>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class MainMenuViewModel : ViewModel() {
    
    // Search state
    var searchQuery by mutableStateOf("")
    var searchMode by mutableStateOf(SearchMode.MODPACK)
    var searchUiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
    var sortOption by mutableStateOf(SortOption.RELEVANCE)
    var selectedVersion by mutableStateOf<String?>(null)
    
    // Featured projects state
    var featuredProjects by mutableStateOf<List<ModrinthProject>>(emptyList())
    var isLoadingFeatured by mutableStateOf(false)
    var isLoadingMoreFeatured by mutableStateOf(false)
    var featuredOffset by mutableStateOf(0)
    var hasMoreFeatured by mutableStateOf(true)
    
    // Search pagination state
    var isLoadingMore by mutableStateOf(false)
    var searchOffset by mutableStateOf(0)
    var hasMoreResults by mutableStateOf(true)
    
    // Game versions
    var gameVersions by mutableStateOf<List<GameVersion>>(emptyList())
    var isLoadingVersions by mutableStateOf(false)
    
    init {
        loadGameVersions()
    }
    
    private fun loadGameVersions() {
        viewModelScope.launch {
            isLoadingVersions = true
            try {
                val versions = ModrinthApi.retrofitService.getGameVersions()
                // Фильтруем только release версии и берем версии до 1.7.10 включительно
                gameVersions = versions.filter { version ->
                    version.versionType == "release" && isVersionValid(version.version)
                }.take(50)
            } catch (e: Exception) {
                gameVersions = emptyList()
            } finally {
                isLoadingVersions = false
            }
        }
    }
    
    private fun isVersionValid(version: String): Boolean {
        // Парсим версию и проверяем, что она >= 1.7.10
        val parts = version.split(".")
        if (parts.isEmpty()) return false
        
        return try {
            val major = parts.getOrNull(0)?.toIntOrNull() ?: return false
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0
            
            when {
                major > 1 -> true
                major == 1 && minor > 7 -> true
                major == 1 && minor == 7 && patch >= 10 -> true
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun clearSearch() {
        searchQuery = ""
        searchUiState = SearchUiState.Idle
        searchOffset = 0
        hasMoreResults = true
    }
    
    fun toggleSearchMode() {
        searchMode = if (searchMode == SearchMode.MODPACK) SearchMode.MOD else SearchMode.MODPACK
        if (searchQuery.isNotEmpty()) {
            searchProjects()
        }
    }
    
    fun changeSearchMode(mode: SearchMode) {
        searchMode = mode
        // Сбрасываем пагинацию
        featuredOffset = 0
        featuredProjects = emptyList()
        hasMoreFeatured = true
        searchOffset = 0
        hasMoreResults = true
        // Перезагружаем featured проекты при смене режима
        loadFeaturedProjects()
        // Если есть активный поиск, обновляем результаты
        if (searchQuery.isNotEmpty()) {
            searchProjects()
        }
    }
    
    fun loadFeaturedProjects() {
        if (isLoadingFeatured || !hasMoreFeatured) return
        
        viewModelScope.launch {
            isLoadingFeatured = true
            try {
                val facets = buildFacets()
                val apiSort = if (sortOption == SortOption.DOWNLOADS_ASC) "downloads" else sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = "",
                    facets = facets,
                    limit = 20,
                    offset = featuredOffset,
                    index = apiSort
                )
                
                val projects = if (sortOption.needsClientSort) {
                    response.hits.sortedBy { it.downloads }
                } else {
                    response.hits
                }
                
                featuredProjects = projects
                featuredOffset = 20
                hasMoreFeatured = response.hits.size >= 20
            } catch (e: Exception) {
                // Игнорируем ошибки для featured проектов
                featuredProjects = emptyList()
                hasMoreFeatured = false
            } finally {
                isLoadingFeatured = false
            }
        }
    }
    
    fun loadMoreFeaturedProjects() {
        if (isLoadingMoreFeatured || !hasMoreFeatured) return
        
        viewModelScope.launch {
            isLoadingMoreFeatured = true
            try {
                val facets = buildFacets()
                val apiSort = if (sortOption == SortOption.DOWNLOADS_ASC) "downloads" else sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = "",
                    facets = facets,
                    limit = 20,
                    offset = featuredOffset,
                    index = apiSort
                )
                
                val projects = if (sortOption.needsClientSort) {
                    response.hits.sortedBy { it.downloads }
                } else {
                    response.hits
                }
                
                featuredProjects = featuredProjects + projects
                featuredOffset += response.hits.size
                hasMoreFeatured = response.hits.size >= 20
            } catch (e: Exception) {
                hasMoreFeatured = false
            } finally {
                isLoadingMoreFeatured = false
            }
        }
    }
    
    fun changeSortOption(option: SortOption) {
        sortOption = option
        // Сбрасываем пагинацию
        featuredOffset = 0
        featuredProjects = emptyList()
        hasMoreFeatured = true
        searchOffset = 0
        hasMoreResults = true
        
        if (searchQuery.isNotEmpty()) {
            searchProjects()
        } else {
            loadFeaturedProjects()
        }
    }
    
    fun changeVersion(version: String?) {
        selectedVersion = version
        // Сбрасываем пагинацию
        featuredOffset = 0
        featuredProjects = emptyList()
        hasMoreFeatured = true
        searchOffset = 0
        hasMoreResults = true
        
        if (searchQuery.isNotEmpty()) {
            searchProjects()
        } else {
            loadFeaturedProjects()
        }
    }
    
    private fun buildFacets(): String {
        val projectType = if (searchMode == SearchMode.MODPACK) "modpack" else "mod"
        val facetsList = mutableListOf<String>()
        
        facetsList.add("[\"project_type:$projectType\"]")
        
        if (selectedVersion != null) {
            facetsList.add("[\"versions:$selectedVersion\"]")
        }
        
        return "[${facetsList.joinToString(",")}]"
    }
    
    fun searchProjects() {
        if (searchQuery.isEmpty()) {
            searchUiState = SearchUiState.Idle
            return
        }
        
        viewModelScope.launch {
            searchUiState = SearchUiState.Loading
            searchOffset = 0
            hasMoreResults = true
            
            try {
                val facets = buildFacets()
                val apiSort = if (sortOption == SortOption.DOWNLOADS_ASC) "downloads" else sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = searchQuery,
                    facets = facets,
                    limit = 20,
                    offset = 0,
                    index = apiSort
                )
                
                val projects = if (sortOption.needsClientSort) {
                    response.hits.sortedBy { it.downloads }
                } else {
                    response.hits
                }
                
                searchOffset = 20
                hasMoreResults = response.hits.size >= 20
                searchUiState = SearchUiState.Success(projects)
            } catch (e: Exception) {
                searchUiState = SearchUiState.Error(e.message ?: "Ошибка поиска")
            }
        }
    }
    
    fun loadMoreSearchResults() {
        if (isLoadingMore || !hasMoreResults || searchUiState !is SearchUiState.Success) return
        
        viewModelScope.launch {
            isLoadingMore = true
            try {
                val facets = buildFacets()
                val apiSort = if (sortOption == SortOption.DOWNLOADS_ASC) "downloads" else sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = searchQuery,
                    facets = facets,
                    limit = 20,
                    offset = searchOffset,
                    index = apiSort
                )
                
                val projects = if (sortOption.needsClientSort) {
                    response.hits.sortedBy { it.downloads }
                } else {
                    response.hits
                }
                
                val currentProjects = (searchUiState as SearchUiState.Success).projects
                searchUiState = SearchUiState.Success(currentProjects + projects)
                searchOffset += response.hits.size
                hasMoreResults = response.hits.size >= 20
            } catch (e: Exception) {
                hasMoreResults = false
            } finally {
                isLoadingMore = false
            }
        }
    }
}

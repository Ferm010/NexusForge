package com.ferm.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.data.GameVersion
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.network.ModrinthApi
import kotlinx.coroutines.launch

enum class SearchMode {
    MODPACK,
    MOD
}

enum class SortOption(val apiValue: String) {
    RELEVANCE("relevance"),
    DOWNLOADS_DESC("downloads"),
    NEWEST("newest")
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val projects: List<ModrinthProject>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

class MainMenuViewModel : ViewModel() {

    var searchQuery by mutableStateOf("")
    var searchMode by mutableStateOf(SearchMode.MODPACK)
    var searchUiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
    var sortOption by mutableStateOf(SortOption.RELEVANCE)
    var selectedVersion by mutableStateOf<String?>(null)

    var featuredProjects by mutableStateOf<List<ModrinthProject>>(emptyList())
    var isLoadingFeatured by mutableStateOf(false)
    var isLoadingMoreFeatured by mutableStateOf(false)
    var featuredOffset by mutableIntStateOf(0)
    var hasMoreFeatured by mutableStateOf(true)
    var isLoadingMore by mutableStateOf(false)
    var searchOffset by mutableIntStateOf(0)
    var hasMoreResults by mutableStateOf(true)
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
                // Фильтр
                gameVersions = versions.filter { version ->
                    version.versionType == "release" && isVersionValid(version.version)
                }.take(50)
            } catch (_: Exception) {
                gameVersions = emptyList()
            } finally {
                isLoadingVersions = false
            }
        }
    }
    
    private fun isVersionValid(version: String): Boolean {
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
        } catch (_: Exception) {
            false
        }
    }
    
    fun clearSearch() {
        searchQuery = ""
        searchUiState = SearchUiState.Idle
        searchOffset = 0
        hasMoreResults = true
    }
    
    fun changeSearchMode(mode: SearchMode) {
        searchMode = mode
        featuredOffset = 0
        featuredProjects = emptyList()
        hasMoreFeatured = true
        searchOffset = 0
        hasMoreResults = true
        loadFeaturedProjects()
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
                val apiSort = sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = "",
                    facets = facets,
                    limit = 20,
                    offset = featuredOffset,
                    index = apiSort
                )
                
                featuredProjects = response.hits
                featuredOffset = 20
                hasMoreFeatured = response.hits.size >= 20
            } catch (_: Exception) {
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
                val apiSort = sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = "",
                    facets = facets,
                    limit = 20,
                    offset = featuredOffset,
                    index = apiSort
                )

                val currentProjects = featuredProjects.toMutableList()
                currentProjects.addAll(response.hits)
                featuredProjects = currentProjects
                featuredOffset += response.hits.size
                hasMoreFeatured = response.hits.size >= 20
            } catch (_: Exception) {
                hasMoreFeatured = false
            } finally {
                isLoadingMoreFeatured = false
            }
        }
    }
    
    fun changeSortOption(option: SortOption) {
        sortOption = option
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
        
        // сантизиация
        val sanitizedQuery = com.ferm.nexusforge.utils.InputSanitizer.sanitizeSearchQuery(searchQuery)
        if (!com.ferm.nexusforge.utils.InputSanitizer.isValidSearchQuery(sanitizedQuery)) {
            searchUiState = SearchUiState.Error("Некорректный поисковый запрос")
            return
        }
        
        viewModelScope.launch {
            searchUiState = SearchUiState.Loading
            searchOffset = 0
            hasMoreResults = true
            
            try {
                val facets = buildFacets()
                val apiSort = sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = sanitizedQuery,
                    facets = facets,
                    limit = 20,
                    offset = 0,
                    index = apiSort
                )
                
                searchOffset = 20
                hasMoreResults = response.hits.size >= 20
                searchUiState = SearchUiState.Success(response.hits)
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
                val apiSort = sortOption.apiValue
                
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = searchQuery,
                    facets = facets,
                    limit = 20,
                    offset = searchOffset,
                    index = apiSort
                )
                
                val currentProjects = (searchUiState as SearchUiState.Success).projects
                val updatedProjects = currentProjects.toMutableList()
                updatedProjects.addAll(response.hits)
                searchUiState = SearchUiState.Success(updatedProjects)
                searchOffset += response.hits.size
                hasMoreResults = response.hits.size >= 20
            } catch (_: Exception) {
                hasMoreResults = false
            } finally {
                isLoadingMore = false
            }
        }
    }
    
    fun setError(message: String) {
        searchUiState = SearchUiState.Error(message)
    }
}

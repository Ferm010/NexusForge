package com.example.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusforge.data.ModrinthProject
import com.example.nexusforge.network.ModrinthApi
import kotlinx.coroutines.launch

class ProjectDetailsViewModel : ViewModel() {
    
    var project by mutableStateOf<ModrinthProject?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    fun loadProject(projectId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Используем поиск по project_id через facets
                val facets = "[[\"project_id:$projectId\"]]"
                val response = ModrinthApi.retrofitService.searchProjects(
                    query = "",
                    facets = facets,
                    limit = 1
                )
                
                project = response.hits.firstOrNull()
                if (project == null) {
                    errorMessage = "Проект не найден"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Ошибка загрузки проекта"
            } finally {
                isLoading = false
            }
        }
    }
}

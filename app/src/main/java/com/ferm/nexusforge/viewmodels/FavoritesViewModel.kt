package com.ferm.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.data.FavoriteProject
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.repository.FirestoreRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    
    // Список избранных проектов из Firestore (реалтайм синхронизация)
    val favoriteProjects: StateFlow<List<FavoriteProject>> = repository.getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    /**
     * Добавить проект в избранное
     */
    fun addFavorite(project: ModrinthProject) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val favoriteProject = FavoriteProject.fromModrinthProject(project)
            val result = repository.addToFavorites(favoriteProject)
            
            result.onFailure { error ->
                errorMessage = "Ошибка добавления в избранное: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Удалить проект из избранного
     */
    fun removeFavorite(projectId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = repository.removeFromFavorites(projectId)
            
            result.onFailure { error ->
                errorMessage = "Ошибка удаления из избранного: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Проверить, находится ли проект в избранном
     */
    fun isFavorite(projectId: String): Boolean {
        return favoriteProjects.value.any { it.projectId == projectId }
    }
    
    /**
     * Переключить статус избранного
     */
    fun toggleFavorite(project: ModrinthProject): Boolean {
        return if (isFavorite(project.projectId)) {
            removeFavorite(project.projectId)
            false
        } else {
            addFavorite(project)
            true
        }
    }
    
    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        errorMessage = null
    }
}

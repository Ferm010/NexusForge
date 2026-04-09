package com.ferm.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.data.CustomModpack
import com.ferm.nexusforge.data.ModReference
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.repository.FirestoreRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomModpacksViewModel(
    private val repository: FirestoreRepository = FirestoreRepository()
) : ViewModel() {
    
    // Список пользовательских сборок из Firestore (реалтайм синхронизация)
    val customModpacks: StateFlow<List<CustomModpack>> = repository.getCustomModpacks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var selectedModpack by mutableStateOf<CustomModpack?>(null)
        private set
    
    /**
     * Создать новую сборку
     */
    fun createModpack(
        name: String,
        description: String,
        minecraftVersion: String,
        modLoader: String,
        iconUrl: String? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val modpack = CustomModpack(
                name = name,
                description = description,
                minecraftVersion = minecraftVersion,
                modLoader = modLoader,
                iconUrl = iconUrl,
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
            
            val result = repository.createCustomModpack(modpack)
            
            result.onSuccess { modpackId ->
                // Успешно создано
            }.onFailure { error ->
                errorMessage = "Ошибка создания сборки: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Обновить существующую сборку
     */
    fun updateModpack(modpack: CustomModpack) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val updatedModpack = modpack.copy(updatedAt = Timestamp.now())
            val result = repository.updateCustomModpack(updatedModpack)
            
            result.onFailure { error ->
                errorMessage = "Ошибка обновления сборки: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Удалить сборку
     */
    fun deleteModpack(modpackId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = repository.deleteCustomModpack(modpackId)
            
            result.onFailure { error ->
                errorMessage = "Ошибка удаления сборки: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Добавить мод в сборку
     */
    fun addModToModpack(modpackId: String, project: ModrinthProject, required: Boolean = true) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = repository.getCustomModpack(modpackId)
            
            result.onSuccess { modpack ->
                if (modpack != null) {
                    val modRef = ModReference(
                        projectId = project.projectId,
                        title = project.title,
                        iconUrl = project.iconUrl,
                        required = required
                    )
                    
                    // Проверяем, что мод еще не добавлен
                    if (!modpack.mods.any { it.projectId == project.projectId }) {
                        val updatedModpack = modpack.copy(
                            mods = modpack.mods + modRef,
                            updatedAt = Timestamp.now()
                        )
                        updateModpack(updatedModpack)
                    }
                }
            }.onFailure { error ->
                errorMessage = "Ошибка добавления мода: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Удалить мод из сборки
     */
    fun removeModFromModpack(modpackId: String, projectId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = repository.getCustomModpack(modpackId)
            
            result.onSuccess { modpack ->
                if (modpack != null) {
                    val updatedModpack = modpack.copy(
                        mods = modpack.mods.filter { it.projectId != projectId },
                        updatedAt = Timestamp.now()
                    )
                    updateModpack(updatedModpack)
                }
            }.onFailure { error ->
                errorMessage = "Ошибка удаления мода: ${error.message}"
            }
            
            isLoading = false
        }
    }
    
    /**
     * Выбрать сборку для редактирования
     */
    fun selectModpack(modpackId: String) {
        viewModelScope.launch {
            val result = repository.getCustomModpack(modpackId)
            result.onSuccess { modpack ->
                selectedModpack = modpack
            }
        }
    }
    
    /**
     * Очистить выбранную сборку
     */
    fun clearSelectedModpack() {
        selectedModpack = null
    }
    
    /**
     * Очистить сообщение об ошибке
     */
    fun clearError() {
        errorMessage = null
    }
}

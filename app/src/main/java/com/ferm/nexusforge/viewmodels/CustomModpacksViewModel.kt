package com.ferm.nexusforge.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.data.CustomModpack
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

    
    override fun onCleared() {
        super.onCleared()
        repository.clearAllListeners()
    }
    
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
            
            result.onSuccess { _ ->
                // Успешно создано
            }.onFailure { error ->
                errorMessage = "Ошибка создания сборки: ${error.message}"
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
}

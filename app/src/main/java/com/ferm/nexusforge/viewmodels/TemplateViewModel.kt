package com.ferm.nexusforge.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ferm.nexusforge.data.ModpackTemplate
import com.ferm.nexusforge.data.TemplateMod
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.repository.FirestoreRepository
import com.ferm.nexusforge.utils.NetworkChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TemplateViewModel : ViewModel() {
    private val repository = FirestoreRepository()
    private var networkChecker: NetworkChecker? = null
    
    private val _state = MutableStateFlow(TemplateState())
    val state: StateFlow<TemplateState> = _state.asStateFlow()
    
    private val _templates = MutableStateFlow<List<ModpackTemplate>>(emptyList())
    val templates: StateFlow<List<ModpackTemplate>> = _templates.asStateFlow()
    
    fun initializeNetworkChecker(context: Context) {
        networkChecker = NetworkChecker(context)
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
    
    init {
        loadTemplates()
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            repository.getTemplates().collect { templates ->
                _templates.value = templates
            }
        }
    }
    
    fun updateTemplateName(name: String) {
        _state.value = _state.value.copy(templateName = name)
    }
    
    fun updateTemplateDescription(description: String) {
        _state.value = _state.value.copy(templateDescription = description)
    }
    
    fun addMod(mod: ModrinthProject) {
        val currentMods = _state.value.selectedMods.toMutableList()
        if (currentMods.none { it.projectId == mod.actualProjectId }) {
            currentMods.add(
                TemplateMod(
                    projectId = mod.actualProjectId,
                    name = mod.title,
                    iconUrl = mod.iconUrl
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
    
    fun saveTemplate(minecraftVersion: String, modLoader: String, onComplete: (Boolean) -> Unit) {
        // Проверка сети перед сохранением
        if (networkChecker?.isNetworkAvailable() == false) {
            _state.value = _state.value.copy(
                error = "Проблема сети. Проверьте подключение к интернету."
            )
            onComplete(false)
            return
        }
        
        viewModelScope.launch {
            val template = ModpackTemplate(
                id = _state.value.templateId,
                name = _state.value.templateName,
                description = _state.value.templateDescription,
                mods = _state.value.selectedMods,
                minecraftVersion = minecraftVersion,
                modLoader = modLoader
            )
            
            val result = repository.saveTemplate(template)
            onComplete(result.isSuccess)
        }
    }
    
    fun loadTemplate(templateId: String) {
        viewModelScope.launch {
            val result = repository.getTemplate(templateId)
            result.onSuccess { template ->
                template?.let {
                    _state.value = _state.value.copy(
                        templateId = it.id,
                        templateName = it.name,
                        templateDescription = it.description,
                        selectedMods = it.mods,
                        minecraftVersion = it.minecraftVersion,
                        modLoader = it.modLoader
                    )
                }
            }
        }
    }
    
    fun deleteTemplate(templateId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteTemplate(templateId)
            onComplete(result.isSuccess)
        }
    }
    
    fun resetState() {
        _state.value = TemplateState()
    }
}

data class TemplateState(
    val templateId: String = "",
    val templateName: String = "",
    val templateDescription: String = "",
    val selectedMods: List<TemplateMod> = emptyList(),
    val minecraftVersion: String = "",
    val modLoader: String = "",
    val error: String? = null
)

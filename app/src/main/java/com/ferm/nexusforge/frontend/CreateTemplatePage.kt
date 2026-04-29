package com.ferm.nexusforge.frontend

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ferm.nexusforge.R
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.data.TemplateMod
import com.ferm.nexusforge.viewmodels.ModpackCreatorViewModel
import com.ferm.nexusforge.viewmodels.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTemplatePage(
    templateId: String = "",
    onBackClick: () -> Unit = {},
    searchViewModel: ModpackCreatorViewModel = viewModel(),
    templateViewModel: TemplateViewModel = viewModel()
) {
    val searchState by searchViewModel.state.collectAsState()
    val templateState by templateViewModel.state.collectAsState()
    val context = LocalContext.current
    val templateSaveSuccessMessage = stringResource(R.string.template_save_success)
    val templateSaveErrorMessage = stringResource(R.string.template_save_error)
    
    var showVersionDropdown by remember { mutableStateOf(false) }
    var showLoaderDropdown by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showExitWarning by remember { mutableStateOf(false) }
    
    LaunchedEffect(templateState.templateName, templateState.templateDescription, templateState.selectedMods) {
        hasUnsavedChanges = true
    }
    
    // Синхронизация версии и загрузчика при загрузке шаблона
    LaunchedEffect(templateState.minecraftVersion, templateState.modLoader) {
        if (templateState.minecraftVersion.isNotEmpty() && searchState.selectedMinecraftVersion != templateState.minecraftVersion) {
            searchViewModel.updateMinecraftVersion(templateState.minecraftVersion)
        }
        if (templateState.modLoader.isNotEmpty() && searchState.selectedModLoader != templateState.modLoader) {
            searchViewModel.updateModLoader(templateState.modLoader)
        }
    }
    
    LaunchedEffect(Unit) {
        searchViewModel.resetState()
        searchViewModel.initializeNetworkChecker(context)
        templateViewModel.initializeNetworkChecker(context)
        if (templateId.isNotEmpty()) {
            templateViewModel.loadTemplate(templateId)
        } else {
            templateViewModel.resetState()
        }
        hasUnsavedChanges = false
    }
    
    val handleBackClick: () -> Unit = {
        if (hasUnsavedChanges) {
            showExitWarning = true
        } else {
            onBackClick()
        }
    }
    
    BackHandler(enabled = hasUnsavedChanges) {
        showExitWarning = true
    }

    if (showExitWarning) {
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        AlertDialog(
            onDismissRequest = { showExitWarning = false },
            title = { Text(stringResource(R.string.exit_without_saving_title)) },
            text = { Text(stringResource(R.string.exit_without_saving_message)) },
            confirmButton = {
                Button(onClick = {
                    hasUnsavedChanges = false
                    showExitWarning = false
                    onBackClick()
                }) {
                    Text(stringResource(R.string.exit_button))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitWarning = false }) {
                    Text(stringResource(R.string.stay_button))
                }
            }
        )
    }
    
    // Alert для изменения версии Minecraft
    if (templateState.showVersionWarning) {
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        AlertDialog(
            onDismissRequest = { templateViewModel.cancelVersionChange() },
            title = { Text(stringResource(R.string.change_version_title)) },
            text = { Text(stringResource(R.string.change_version_message)) },
            confirmButton = {
                Button(onClick = { 
                    templateViewModel.confirmVersionChange()
                    searchViewModel.updateMinecraftVersion(templateState.minecraftVersion)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { templateViewModel.cancelVersionChange() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    // Alert для изменения модлоадера
    if (templateState.showModLoaderWarning) {
        @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
        AlertDialog(
            onDismissRequest = { templateViewModel.cancelModLoaderChange() },
            title = { Text(stringResource(R.string.change_loader_title)) },
            text = { Text(stringResource(R.string.change_loader_message)) },
            confirmButton = {
                Button(onClick = { 
                    templateViewModel.confirmModLoaderChange()
                    searchViewModel.updateModLoader(templateState.modLoader)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { templateViewModel.cancelModLoaderChange() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (templateId.isEmpty()) stringResource(R.string.create_template_title) else stringResource(R.string.edit_template_title)) },
                navigationIcon = {
                    IconButton(onClick = handleBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Отображение ошибки сети
            if (templateState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = templateState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { templateViewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = templateState.templateName,
                onValueChange = { templateViewModel.updateTemplateName(it) },
                label = { Text(stringResource(R.string.template_name_label)) },
                placeholder = { Text(stringResource(R.string.template_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = templateState.templateDescription,
                onValueChange = { templateViewModel.updateTemplateDescription(it) },
                label = { Text(stringResource(R.string.template_description_label)) },
                placeholder = { Text(stringResource(R.string.template_description_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Выбор версии Minecraft
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showVersionDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = templateState.minecraftVersion.ifEmpty {
                                stringResource(R.string.select_mc_version)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    DropdownMenu(
                        expanded = showVersionDropdown,
                        onDismissRequest = { showVersionDropdown = false }
                    ) {
                        searchState.availableMinecraftVersions.forEach { version ->
                            DropdownMenuItem(
                                text = { Text(version) },
                                onClick = {
                                    templateViewModel.requestMinecraftVersionChange(version)
                                    showVersionDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Выбор загрузчика модов
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showLoaderDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = templateState.modLoader.ifEmpty { 
                                stringResource(R.string.select_loader)
                            }.uppercase()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    DropdownMenu(
                        expanded = showLoaderDropdown,
                        onDismissRequest = { showLoaderDropdown = false }
                    ) {
                        searchState.availableModLoaders.forEach { loader ->
                            DropdownMenuItem(
                                text = { Text(loader.uppercase()) },
                                onClick = {
                                    templateViewModel.requestModLoaderChange(loader)
                                    showLoaderDropdown = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Поиск модов
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.search_mods),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = searchState.searchQuery,
                        onValueChange = { query ->
                            searchViewModel.updateSearchQuery(query)
                            if (query.length >= 2) {
                                searchViewModel.searchMods()
                            }
                        },
                        placeholder = { Text(stringResource(R.string.search_mods)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (searchState.isSearching) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (searchState.searchResults.isEmpty() && searchState.searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_results),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(searchState.searchResults) { mod ->
                                TemplateSearchResultItem(
                                    mod = mod,
                                    isAdded = templateState.selectedMods.any { it.projectId == mod.actualProjectId },
                                    onAdd = { templateViewModel.addMod(mod) }
                                )
                            }
                        }
                    }
                }
                
                // Выбранные моды
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Text(
                        text = stringResource(R.string.selected_mods),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (templateState.selectedMods.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_mods_selected),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(templateState.selectedMods) { mod ->
                                TemplateModItem(
                                    mod = mod,
                                    onRemove = { templateViewModel.removeMod(mod.projectId) }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    templateViewModel.saveTemplate(
                        minecraftVersion = templateState.minecraftVersion,
                        modLoader = templateState.modLoader
                    ) { success ->
                        if (success) {
                            Toast.makeText(context, templateSaveSuccessMessage, Toast.LENGTH_SHORT).show()
                            onBackClick()
                        } else {
                            Toast.makeText(context, templateSaveErrorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                 enabled = templateState.templateName.isNotEmpty() && 
                         templateState.selectedMods.isNotEmpty() &&
                         templateState.minecraftVersion.isNotEmpty() &&
                         templateState.modLoader.isNotEmpty() &&
                         templateState.error == null
            ) {
                Text(stringResource(R.string.save_template_button))
            }
        }
    }
}

@Composable
private fun TemplateSearchResultItem(
    mod: ModrinthProject,
    isAdded: Boolean,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAdded) { onAdd() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAdded) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mod.iconUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(mod.iconUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mod.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = mod.categories.firstOrNull() ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TemplateModItem(
    mod: TemplateMod,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!mod.iconUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(mod.iconUrl),
                    contentDescription = mod.name,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mod.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mod.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

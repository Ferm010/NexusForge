package com.ferm.nexusforge.frontend

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
import com.ferm.nexusforge.data.ModpackTemplate
import com.ferm.nexusforge.viewmodels.ModpackCreatorViewModel
import com.ferm.nexusforge.viewmodels.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModpackPage(
    vm: ModpackCreatorViewModel = viewModel(),
    templateViewModel: TemplateViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onModpackCreated: (String) -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val templates by templateViewModel.templates.collectAsState()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val templateSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedModId by remember { mutableStateOf<String?>(null) }
    var selectedModDetails by remember { mutableStateOf<ModrinthProject?>(null) }
    var isLoadingModDetails by remember { mutableStateOf(false) }
    var showTemplateSelector by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        vm.resetState()
    }
    
    LaunchedEffect(selectedModDetails) {
        if (selectedModDetails != null) {
            sheetState.show()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.create_modpack)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            OutlinedTextField(
                value = state.modpackName,
                onValueChange = { vm.updateModpackName(it) },
                label = { Text(stringResource(R.string.modpack_name)) },
                placeholder = { Text(stringResource(R.string.enter_modpack_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Выбор версии Minecraft
                var versionExpanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { versionExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.selectedMinecraftVersion.isEmpty()) 
                                stringResource(R.string.select_minecraft_version)
                            else 
                                state.selectedMinecraftVersion
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    DropdownMenu(
                        expanded = versionExpanded,
                        onDismissRequest = { versionExpanded = false }
                    ) {
                        state.availableMinecraftVersions.forEach { version ->
                            DropdownMenuItem(
                                text = { Text(version) },
                                onClick = {
                                    vm.updateMinecraftVersion(version)
                                    versionExpanded = false
                                    vm.clearSearchResults()
                                }
                            )
                        }
                    }
                }
                
                // Выбор загрузчика модов
                var loaderExpanded by remember { mutableStateOf(false) }
                
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { loaderExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.selectedModLoader.isEmpty()) 
                                stringResource(R.string.select_mod_loader)
                            else 
                                state.selectedModLoader.uppercase()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    DropdownMenu(
                        expanded = loaderExpanded,
                        onDismissRequest = { loaderExpanded = false }
                    ) {
                        state.availableModLoaders.forEach { loader ->
                            DropdownMenuItem(
                                text = { Text(loader.uppercase()) },
                                onClick = {
                                    vm.updateModLoader(loader)
                                    loaderExpanded = false
                                    vm.clearSearchResults()
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка выбора шаблона
            OutlinedButton(
                onClick = { showTemplateSelector = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedMinecraftVersion.isNotEmpty() && state.selectedModLoader.isNotEmpty()
            ) {
                Icon(Icons.Default.FolderOpen, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.use_template))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query ->
                    vm.updateSearchQuery(query)
                    if (query.length >= 2 && state.selectedMinecraftVersion.isNotEmpty()) {
                        vm.searchMods()
                    }
                },
                label = { Text(stringResource(R.string.search_mods)) },
                placeholder = { Text(stringResource(R.string.search_mods)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.searchMods() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
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
                    
                    if (state.isSearching) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (state.searchResults.isEmpty() && state.searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No mods found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.searchResults) { mod ->
                                SearchResultModItem(
                                    mod = mod,
                                    onAdd = { vm.addMod(mod) }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
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
                    
                    if (state.selectedMods.isEmpty()) {
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
                            items(state.selectedMods) { mod ->
                                SelectedModItem(
                                    name = mod.name,
                                    version = mod.version,
                                    iconUrl = mod.iconUrl,
                                    onRemove = { vm.removeMod(mod.projectId) },
                                    onClick = {
                                        selectedModId = mod.projectId
                                        isLoadingModDetails = true
                                        scope.launch {
                                            try {
                                                val project = withContext(Dispatchers.IO) {
                                                    com.ferm.nexusforge.network.ModrinthApi.retrofitService.getProject(mod.projectId)
                                                }
                                                selectedModDetails = project
                                            } catch (e: Exception) {
                                                android.util.Log.e("CreateModpack", "Error loading mod details: ${e.message}")
                                            } finally {
                                                isLoadingModDetails = false
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    onModpackCreated("generating")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.modpackName.isNotEmpty() && 
                          state.selectedMinecraftVersion.isNotEmpty() &&
                          state.selectedMods.isNotEmpty() &&
                          !state.isGenerating
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.generate_pack))
                }
            }
        }
    }
    
    if (selectedModDetails != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                selectedModDetails = null
                selectedModId = null
            },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (selectedModDetails?.iconUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(selectedModDetails?.iconUrl),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedModDetails?.title?.take(1)?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedModDetails?.title ?: "",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "by ${selectedModDetails?.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = selectedModDetails?.description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedModDetails?.categories?.isNotEmpty() == true) {
                    Text(
                        text = stringResource(R.string.tags),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedModDetails?.categories?.take(5)?.forEach { category ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    
    // ModalBottomSheet для выбора шаблона
    if (showTemplateSelector) {
        ModalBottomSheet(
            onDismissRequest = { showTemplateSelector = false },
            sheetState = templateSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.templates),
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (templates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_templates),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(templates) { template ->
                            TemplateSelectItem(
                                template = template,
                                currentMinecraftVersion = state.selectedMinecraftVersion,
                                currentModLoader = state.selectedModLoader,
                                onSelect = {
                                    // Применить шаблон - загрузить моды
                                    scope.launch {
                                        template.mods.forEach { templateMod ->
                                            try {
                                                val project = withContext(Dispatchers.IO) {
                                                    com.ferm.nexusforge.network.ModrinthApi.retrofitService.getProject(templateMod.projectId)
                                                }
                                                vm.addMod(project)
                                            } catch (e: Exception) {
                                                android.util.Log.e("CreateModpack", "Error loading mod from template: ${e.message}")
                                            }
                                        }
                                    }
                                    showTemplateSelector = false
                                    Toast.makeText(context, "Шаблон применен", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TemplateSelectItem(
    template: ModpackTemplate,
    currentMinecraftVersion: String,
    currentModLoader: String,
    onSelect: () -> Unit
) {
    val isCompatible = template.minecraftVersion == currentMinecraftVersion && 
                       template.modLoader.equals(currentModLoader, ignoreCase = true)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isCompatible) { onSelect() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!isCompatible) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                color = if (!isCompatible) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            if (template.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Модов: ${template.mods.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!isCompatible) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${template.minecraftVersion} • ${template.modLoader.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!isCompatible) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠ Несовместимо с текущей версией ($currentMinecraftVersion • ${currentModLoader.uppercase()})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun SearchResultModItem(
    mod: ModrinthProject,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdd() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = mod.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Composable
private fun SelectedModItem(
    name: String,
    version: String,
    iconUrl: String?,
    onRemove: () -> Unit,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconUrl != null && iconUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(iconUrl),
                    contentDescription = name,
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
                        text = name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (version.isNotEmpty()) "v$version" else stringResource(R.string.loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

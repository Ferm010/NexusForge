package com.example.nexusforge.frontend

import android.content.Context
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.nexusforge.R
import com.example.nexusforge.data.ModrinthProject
import com.example.nexusforge.repository.FirestoreRepository
import com.example.nexusforge.data.ModpackMod
import com.example.nexusforge.viewmodels.ModpackCreatorViewModel
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModpackEditorPage(
    modpackId: String,
    onBackClick: () -> Unit = {},
    onGenerateClick: () -> Unit = {}
) {
    val vm = viewModel<ModpackCreatorViewModel>()
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val firestoreRepository = remember { FirestoreRepository() }
    
    var isLoading by remember { mutableStateOf(true) }
    var isSaved by remember { mutableStateOf(false) }
    var showVersionWarning by remember { mutableStateOf(false) }
    var showLoaderWarning by remember { mutableStateOf(false) }
    var showUnsavedWarning by remember { mutableStateOf(false) }
    var showSearchResults by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var previousVersion by remember { mutableStateOf("") }
    var previousLoader by remember { mutableStateOf("") }
    val savedMessage = stringResource(R.string.modpack_saved)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var selectedModId by remember { mutableStateOf<String?>(null) }
    var selectedModDetails by remember { mutableStateOf<ModrinthProject?>(null) }
    var isLoadingModDetails by remember { mutableStateOf(false) }
    
    LaunchedEffect(modpackId) {
        try {
            vm.setModpackId(modpackId)
            val result = firestoreRepository.getCustomModpack(modpackId)
            result.onSuccess { modpack ->
                modpack?.let { pack ->
                    vm.updateModpackName(pack.name)
                    vm.updateMinecraftVersion(pack.minecraftVersion)
                    vm.updateModLoader(pack.modLoader)
                    
                    // Load mods with versions sequentially
                    for (modRef in pack.mods) {
                        try {
                            val versions = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                com.example.nexusforge.network.ModrinthApi.retrofitService.getProjectVersions(modRef.projectId)
                            }
                            
                            val matchingVersion = versions.firstOrNull { version ->
                                val versionsList = version.gameVersion.ifEmpty { version.gameVersions ?: emptyList() }
                                val hasMatchingVersion = versionsList.any { v -> v == pack.minecraftVersion }
                                val hasMatchingLoader = version.loaders.any { loader -> loader.equals(pack.modLoader, ignoreCase = true) }
                                hasMatchingVersion && hasMatchingLoader
                            }
                            
                            val modVersion = matchingVersion?.versionNumber ?: versions.firstOrNull()?.versionNumber ?: ""
                            val file = matchingVersion?.files?.firstOrNull() ?: versions.firstOrNull()?.files?.firstOrNull()
                            
                            vm.addModDirectly(
                                ModpackMod(
                                    projectId = modRef.projectId,
                                    name = modRef.title,
                                    version = modVersion,
                                    downloadUrl = modRef.downloadUrl ?: file?.url ?: "",
                                    iconUrl = modRef.iconUrl,
                                    fileName = modRef.fileName ?: file?.filename,
                                    fileSize = modRef.fileSize ?: file?.size,
                                    sha1 = modRef.sha1 ?: file?.hashes?.get("sha1"),
                                    sha512 = modRef.sha512 ?: file?.hashes?.get("sha512")
                                )
                            )
                        } catch (e: Exception) {
                            android.util.Log.e("ModpackEditor", "Error loading version for ${modRef.title}: ${e.message}")
                            vm.addModDirectly(
                                ModpackMod(
                                    projectId = modRef.projectId,
                                    name = modRef.title,
                                    version = "",
                                    downloadUrl = modRef.downloadUrl ?: "",
                                    iconUrl = modRef.iconUrl,
                                    fileName = modRef.fileName,
                                    fileSize = modRef.fileSize,
                                    sha1 = modRef.sha1,
                                    sha512 = modRef.sha512
                                )
                            )
                        }
                    }
                    
                    previousVersion = pack.minecraftVersion
                    previousLoader = pack.modLoader
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isLoading = false
    }
    
    LaunchedEffect(state.modpackName, state.selectedMinecraftVersion, state.selectedModLoader, state.selectedMods) {
        if (!isLoading) {
            hasUnsavedChanges = true
        }
    }
    
    LaunchedEffect(selectedModDetails) {
        if (selectedModDetails != null) {
            sheetState.show()
        }
    }
    
    LaunchedEffect(isSaved) {
        if (isSaved) {
            Toast.makeText(context, savedMessage, Toast.LENGTH_SHORT).show()
            isSaved = false
        }
    }
    
    val handleBackClick: () -> Unit = {
        if (showSearchResults) {
            showSearchResults = false
            vm.clearSearchResults()
        } else if (hasUnsavedChanges) {
            showUnsavedWarning = true
        } else {
            onBackClick()
        }
    }
    
    BackHandler(enabled = showSearchResults || hasUnsavedChanges) {
        if (showSearchResults) {
            showSearchResults = false
            vm.clearSearchResults()
        } else if (hasUnsavedChanges) {
            showUnsavedWarning = true
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_modpack)) },
                navigationIcon = {
                    IconButton(onClick = handleBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                                        if (version != previousVersion && state.selectedMods.isNotEmpty()) {
                                            showVersionWarning = true
                                        } else {
                                            vm.updateMinecraftVersion(version)
                                            previousVersion = version
                                            vm.clearSearchResults()
                                        }
                                        versionExpanded = false
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
                                        if (loader != previousLoader && state.selectedMods.isNotEmpty()) {
                                            showLoaderWarning = true
                                        } else {
                                            vm.updateModLoader(loader)
                                            previousLoader = loader
                                            vm.clearSearchResults()
                                        }
                                        loaderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { query ->
                        vm.updateSearchQuery(query)
                        if (query.length >= 2 && state.selectedMinecraftVersion.isNotEmpty()) {
                            showSearchResults = true
                            vm.searchMods()
                        } else if (query.isEmpty()) {
                            showSearchResults = false
                            vm.clearSearchResults()
                        }
                    },
                    label = { Text(stringResource(R.string.search_mods)) },
                    placeholder = { Text(stringResource(R.string.search_mods)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            if (state.searchResults.isNotEmpty() && showSearchResults) {
                                IconButton(onClick = { showSearchResults = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    showSearchResults = true
                                    vm.searchMods() 
                                }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (state.isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (state.searchResults.isNotEmpty() && showSearchResults) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        items(state.searchResults) { project ->
                            ModSearchResultItem(
                                project = project,
                                isSelected = state.selectedMods.any { it.projectId == project.projectId },
                                onClick = {
                                    if (state.selectedMods.none { it.projectId == project.projectId }) {
                                        vm.addMod(project)
                                    }
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${stringResource(R.string.selected_mods)} (${state.selectedMods.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.selectedMods) { mod ->
                        SelectedModItem(
                            mod = mod,
                            onRemove = { vm.removeMod(mod.projectId) },
                            onClick = {
                                android.util.Log.d("ModpackEditor", "Clicked on mod: ${mod.name}, ID: ${mod.projectId}")
                                selectedModId = mod.projectId
                                isLoadingModDetails = true
                                scope.launch {
                                    try {
                                        android.util.Log.d("ModpackEditor", "Loading details for: ${mod.projectId}")
                                        val project = withContext(Dispatchers.IO) {
                                            com.example.nexusforge.network.ModrinthApi.retrofitService.getProject(mod.projectId)
                                        }
                                        android.util.Log.d("ModpackEditor", "Loaded project: ${project.title}")
                                        selectedModDetails = project
                                        isLoadingModDetails = false
                                    } catch (e: Exception) {
                                        android.util.Log.e("ModpackEditor", "Error loading mod details: ${e.message}", e)
                                        isLoadingModDetails = false
                                    }
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            vm.saveModpackOnly {
                                isSaved = true
                                hasUnsavedChanges = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = state.modpackName.isNotEmpty() && state.selectedMinecraftVersion.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.save_only))
                    }

                    Button(
                        onClick = {
                            vm.saveModpackOnly {
                                hasUnsavedChanges = false
                                onGenerateClick()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = state.modpackName.isNotEmpty() && state.selectedMinecraftVersion.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.generate))
                    }
                }
            }
        }
    }
    
    if (showVersionWarning) {
        AlertDialog(
            onDismissRequest = { showVersionWarning = false },
            title = { Text(stringResource(R.string.warning)) },
            text = { Text(stringResource(R.string.version_changed_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAllMods()
                    vm.updateMinecraftVersion(state.selectedMinecraftVersion)
                    previousVersion = state.selectedMinecraftVersion
                    vm.clearSearchResults()
                    showVersionWarning = false
                }) {
                    Text(stringResource(R.string.clear_mods))
                }
            },
            dismissButton = {
                TextButton(onClick = { showVersionWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    if (showLoaderWarning) {
        AlertDialog(
            onDismissRequest = { showLoaderWarning = false },
            title = { Text(stringResource(R.string.warning)) },
            text = { Text(stringResource(R.string.loader_changed_warning)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearAllMods()
                    vm.updateModLoader(state.selectedModLoader)
                    previousLoader = state.selectedModLoader
                    vm.clearSearchResults()
                    showLoaderWarning = false
                }) {
                    Text(stringResource(R.string.clear_mods))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoaderWarning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
    
    if (showUnsavedWarning) {
        AlertDialog(
            onDismissRequest = { showUnsavedWarning = false },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    hasUnsavedChanges = false
                    showUnsavedWarning = false
                    onBackClick()
                }) {
                    Text(stringResource(R.string.exit))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedWarning = false }) {
                    Text(stringResource(R.string.stay))
                }
            }
        )
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
                        AsyncImage(
                            model = selectedModDetails?.iconUrl,
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
}

@Composable
fun ModSearchResultItem(
    project: ModrinthProject,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSelected) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (project.iconUrl != null) {
                AsyncImage(
                    model = project.iconUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = project.title.take(1),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = project.categories.firstOrNull() ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!isSelected) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Added",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SelectedModItem(
    mod: ModpackMod,
    onRemove: () -> Unit,
    onClick: () -> Unit = {}
) {
    val displayName = mod.name.ifEmpty { "Unknown Mod" }
    val displayVersion = when {
        mod.version.isNotEmpty() -> "v${mod.version}"
        else -> stringResource(R.string.loading)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mod.iconUrl != null && mod.iconUrl.isNotEmpty()) {
                AsyncImage(
                    model = mod.iconUrl,
                    contentDescription = displayName,
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
                        text = displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = displayVersion,
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

private fun formatDownloads(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
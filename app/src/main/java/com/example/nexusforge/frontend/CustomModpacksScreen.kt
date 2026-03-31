package com.example.nexusforge.frontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.nexusforge.data.CustomModpack
import com.example.nexusforge.viewmodels.CustomModpacksViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomModpacksScreen(
    viewModel: CustomModpacksViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val modpacks by viewModel.customModpacks.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои сборки") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "Создать сборку")
                    }
                }
            )
        }
    ) { padding ->
        if (modpacks.isEmpty()) {
            EmptyModpacksState(
                onCreateClick = { showCreateDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(modpacks, key = { it.id }) { modpack ->
                    ModpackCard(
                        modpack = modpack,
                        onDelete = { viewModel.deleteModpack(modpack.id) }
                    )
                }
            }
        }
        
        if (showCreateDialog) {
            CreateModpackDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, description, version, loader ->
                    viewModel.createModpack(name, description, version, loader)
                    showCreateDialog = false
                }
            )
        }
        
        // Показываем ошибки
        viewModel.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Можно показать Snackbar
            }
        }
    }
}

@Composable
fun EmptyModpacksState(
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "У вас пока нет сборок",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Создайте свою первую сборку модов",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Создать сборку")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModpackCard(
    modpack: CustomModpack,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Иконка сборки
                if (modpack.iconUrl != null) {
                    AsyncImage(
                        model = modpack.iconUrl,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // Информация о сборке
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = modpack.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = modpack.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = modpack.minecraftVersion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = modpack.modLoader,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                // Кнопка удаления
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Раскрывающийся список модов
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${modpack.mods.size} модов",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Скрыть" else "Показать",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Список модов
            if (expanded && modpack.mods.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modpack.mods.forEach { mod ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (mod.iconUrl != null) {
                                AsyncImage(
                                    model = mod.iconUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Extension,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = mod.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (mod.required) "Обязательный" else "Опциональный",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        if (mod != modpack.mods.last()) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить сборку?") },
            text = { Text("Вы уверены, что хотите удалить сборку \"${modpack.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateModpackDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, version: String, loader: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var minecraftVersion by remember { mutableStateOf("1.21.1") }
    var modLoader by remember { mutableStateOf("fabric") }
    var expandedLoader by remember { mutableStateOf(false) }
    
    val loaders = listOf("fabric", "forge", "quilt", "neoforge")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать сборку") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = minecraftVersion,
                    onValueChange = { minecraftVersion = it },
                    label = { Text("Версия Minecraft") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedLoader,
                    onExpandedChange = { expandedLoader = it }
                ) {
                    OutlinedTextField(
                        value = modLoader,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Загрузчик модов") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLoader)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedLoader,
                        onDismissRequest = { expandedLoader = false }
                    ) {
                        loaders.forEach { loader ->
                            DropdownMenuItem(
                                text = { Text(loader) },
                                onClick = {
                                    modLoader = loader
                                    expandedLoader = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, description, minecraftVersion, modLoader) },
                enabled = name.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

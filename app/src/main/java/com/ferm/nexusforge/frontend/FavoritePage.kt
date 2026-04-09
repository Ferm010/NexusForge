package com.ferm.nexusforge.frontend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ferm.nexusforge.R
import com.ferm.nexusforge.data.CustomModpack
import com.ferm.nexusforge.data.ModpackTemplate
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.frontend.components.NameAppBar
import com.ferm.nexusforge.frontend.mainmenu.FabMenuItem
import com.ferm.nexusforge.frontend.mainmenu.ProjectCard
import com.ferm.nexusforge.frontend.ModpackCard
import com.ferm.nexusforge.viewmodels.CustomModpacksViewModel
import com.ferm.nexusforge.viewmodels.FavoritesViewModel
import com.ferm.nexusforge.viewmodels.RegViewModel
import com.ferm.nexusforge.viewmodels.TemplateViewModel
import android.widget.Toast

enum class FavoritePageMode {
    FAVORITES,
    CUSTOM_MODPACKS,
    TEMPLATES
}

@Composable
fun favoritePage(
    vm: RegViewModel = viewModel(),
    favoritesVm: FavoritesViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onProjectClick: (String) -> Unit = {},
    onModpackClick: (String) -> Unit = {},
    onTemplatesClick: () -> Unit = {},
    onEditTemplate: (String) -> Unit = {},
    onCreateTemplate: () -> Unit = {}
) {
    val favoriteProjects by favoritesVm.favoriteProjects.collectAsState()
    var currentMode by remember { mutableStateOf(FavoritePageMode.FAVORITES) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                namePage = when (currentMode) {
                    FavoritePageMode.FAVORITES -> stringResource(R.string.favorites)
                    FavoritePageMode.CUSTOM_MODPACKS -> stringResource(R.string.custom_modpack)
                    FavoritePageMode.TEMPLATES -> stringResource(R.string.templates)
                },
                userPhotoUrl = vm.userPhotoUrl
            )

            when (currentMode) {
                FavoritePageMode.CUSTOM_MODPACKS -> {
                    CustomModpacksList(
                        onModpackClick = { modpackId -> onModpackClick(modpackId) }
                    )
                }
                FavoritePageMode.TEMPLATES -> {
                    TemplatesContent(
                        onEditTemplate = onEditTemplate,
                        onCreateTemplate = onCreateTemplate
                    )
                }
                FavoritePageMode.FAVORITES -> {
                    if (favoritesVm.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (favoriteProjects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.not_favorite_modpacks),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (favoriteProjects.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.favorite_projects),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                                    )
                                }

                                items(favoriteProjects, key = { it.projectId }) { favorite ->
                                    val project = ModrinthProject(
                                        projectId = favorite.projectId,
                                        slug = favorite.projectId,
                                        title = favorite.title,
                                        description = favorite.description,
                                        categories = favorite.categories,
                                        clientSide = "required",
                                        serverSide = "optional",
                                        projectType = favorite.projectType,
                                        downloads = favorite.downloads,
                                        iconUrl = favorite.iconUrl,
                                        author = favorite.author,
                                        versions = favorite.versions,
                                        follows = 0,
                                        dateCreated = "",
                                        dateModified = ""
                                    )

                                    ProjectCard(
                                        project = project,
                                        onClick = {
                                            onProjectClick(favorite.projectId)
                                        }
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }

        FavoritesFabMenu(
            currentMode = currentMode,
            onModeChange = { mode ->
                currentMode = mode
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FavoritesFabMenu(
    currentMode: FavoritePageMode,
    onModeChange: (FavoritePageMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "rotation"
    )

    Box(modifier = modifier.fillMaxSize()) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { expanded = false }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.widthIn(max = 280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.modpacks),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        FabMenuItem(
                            text = stringResource(R.string.default_modpack),
                            isSelected = currentMode == FavoritePageMode.FAVORITES,
                            onClick = {
                                onModeChange(FavoritePageMode.FAVORITES)
                                expanded = false
                            }
                        )

                        FabMenuItem(
                            text = stringResource(R.string.custom_modpack),
                            isSelected = currentMode == FavoritePageMode.CUSTOM_MODPACKS,
                            onClick = {
                                onModeChange(FavoritePageMode.CUSTOM_MODPACKS)
                                expanded = false
                            }
                        )
                        
                        FabMenuItem(
                            text = stringResource(R.string.templates),
                            isSelected = currentMode == FavoritePageMode.TEMPLATES,
                            onClick = {
                                onModeChange(FavoritePageMode.TEMPLATES)
                                expanded = false
                            }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    painter = painterResource(R.drawable.list),
                    contentDescription = if (expanded) "Close menu" else "Open menu",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun CustomModpacksList(
    onModpackClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modpacksViewModel: CustomModpacksViewModel = viewModel()
    val customModpacks by modpacksViewModel.customModpacks.collectAsState()

    if (modpacksViewModel.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (customModpacks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.zero_modpacks),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.zero_custom_modpacks),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(customModpacks, key = { it.id }) { modpack ->
                ModpackCard(
                    modpack = modpack,
                    onDelete = { modpacksViewModel.deleteModpack(modpack.id) },
                    onClick = { onModpackClick(modpack.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FavoriteModpackItem(
    modpack: CustomModpack,
    onClick: () -> Unit
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = modpack.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${modpack.minecraftVersion} • ${modpack.modLoader}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TemplatesContent(
    onEditTemplate: (String) -> Unit,
    onCreateTemplate: () -> Unit,
    templateViewModel: TemplateViewModel = viewModel()
) {
    val templates by templateViewModel.templates.collectAsState()
    val context = LocalContext.current
    var templateToDelete by remember { mutableStateOf<ModpackTemplate?>(null) }
    
    if (templates.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_templates),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(templates, key = { it.id }) { template ->
                TemplateCard(
                    template = template,
                    onEdit = { onEditTemplate(template.id) },
                    onDelete = { templateToDelete = template }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    if (templateToDelete != null) {
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text(stringResource(R.string.delete_template)) },
            text = { Text("Удалить шаблон \"${templateToDelete?.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        templateToDelete?.let { template ->
                            templateViewModel.deleteTemplate(template.id) { success ->
                                if (success) {
                                    Toast.makeText(context, R.string.template_deleted, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        templateToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun TemplateCard(
    template: ModpackTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (template.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = template.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = template.minecraftVersion,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = template.modLoader.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Модов: ${template.mods.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (template.mods.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        template.mods.take(3).forEach { mod ->
                            if (mod.iconUrl != null && mod.iconUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(mod.iconUrl),
                                    contentDescription = mod.name,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        
                        if (template.mods.size > 3) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${template.mods.size - 3}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
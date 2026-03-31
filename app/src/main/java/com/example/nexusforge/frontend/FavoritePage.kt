package com.example.nexusforge.frontend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.R
import com.example.nexusforge.data.ModrinthProject
import com.example.nexusforge.frontend.components.NameAppBar
import com.example.nexusforge.frontend.mainmenu.FabMenuItem
import com.example.nexusforge.frontend.mainmenu.ProjectCard
import com.example.nexusforge.viewmodels.FavoritesViewModel
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun favoritePage(
    vm: RegViewModel = viewModel(),
    favoritesVm: FavoritesViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onProjectClick: (String) -> Unit = {}
) {
    val favoriteProjects by favoritesVm.favoriteProjects.collectAsState()
    var showCustomModpacks by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                namePage = if (showCustomModpacks) stringResource(R.string.custom_modpack) else stringResource(R.string.favorites),
                userPhotoUrl = vm.userPhotoUrl
            )
            
            if (showCustomModpacks) {
                // Показываем пользовательские сборки
                CustomModpacksList(
                    onModpackClick = { /* TODO */ }
                )
            } else {
                // Показываем избранное
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
                                dateModified = "",
                                license = "",
                                gallery = null
                            )
                            
                            ProjectCard(
                                project = project,
                                onClick = {
                                    onProjectClick(favorite.projectId)
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
        
        // FAB Menu
        FavoritesFabMenu(
            showCustomModpacks = showCustomModpacks,
            onModeChange = { isCustom ->
                showCustomModpacks = isCustom
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun FavoritesFabMenu(
    showCustomModpacks: Boolean,
    onModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "rotation"
    )
    
    Box(modifier = modifier.fillMaxSize()) {
        // Затемнение фона при открытии
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { expanded = false }
            )
        }
        
        // FAB меню в правом нижнем углу
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Bottom)
        ) {
            // Опции меню
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
                            isSelected = !showCustomModpacks,
                            onClick = {
                                onModeChange(false)
                                expanded = false
                            }
                        )
                        
                        FabMenuItem(
                            text = stringResource(R.string.custom_modpack),
                            isSelected = showCustomModpacks,
                            onClick = {
                                onModeChange(true)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Главная FAB кнопка
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
    val modpacksViewModel: com.example.nexusforge.viewmodels.CustomModpacksViewModel = viewModel()
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
                    onDelete = { modpacksViewModel.deleteModpack(modpack.id) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

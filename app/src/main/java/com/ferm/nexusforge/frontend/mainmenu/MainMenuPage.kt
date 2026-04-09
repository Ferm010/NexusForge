package com.ferm.nexusforge.frontend.mainmenu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.viewmodels.MainMenuViewModel
import com.ferm.nexusforge.viewmodels.RegViewModel
import com.ferm.nexusforge.viewmodels.SearchUiState

@Composable
fun MainMenuPage(
    vm: RegViewModel = viewModel(),
    menuVm: MainMenuViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCreateModpack: () -> Unit = {},
    onCreateTemplate: () -> Unit = {},
    onProjectClick: (String) -> Unit = {}
) {
    BackHandler(enabled = true) { }
    
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // Загружаем сборки дня при первом открытии
    LaunchedEffect(Unit) {
        if (menuVm.featuredProjects.isEmpty()) {
            menuVm.loadFeaturedProjects()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search App Bar
            SearchAppBar(
                searchQuery = menuVm.searchQuery,
                onSearchQueryChange = { 
                    menuVm.searchQuery = it
                    if (it.isNotEmpty()) {
                        menuVm.searchProjects()
                    } else {
                        menuVm.clearSearch()
                    }
                },
                onSearch = { query ->
                    menuVm.searchProjects()
                },
                onBackClick = {
                    menuVm.clearSearch()
                },
                onProfileClick = onProfileClick,
                userName = vm.userName,
                userPhotoUrl = vm.userPhotoUrl
            )
            
            // Контент страницы
            when (val state = menuVm.searchUiState) {
                is SearchUiState.Idle -> {
                    // Сборки дня / Моды дня
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (menuVm.searchMode == com.ferm.nexusforge.viewmodels.SearchMode.MODPACK)
                                        stringResource(R.string.modpacks_today)
                                    else
                                        stringResource(R.string.mods_today),
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center
                                )
                                
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.list),
                                        contentDescription = "filter"
                                    )
                                }
                            }
                        }
                        
                        if (menuVm.isLoadingFeatured) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        items(menuVm.featuredProjects) { project ->
                            ProjectCard(
                                project = project,
                                onClick = {
                                    onProjectClick(project.projectId)
                                }
                            )
                        }
                        
                        // Индикатор загрузки следующей страницы
                        if (menuVm.isLoadingMoreFeatured) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        // Триггер для загрузки следующей страницы
                        if (menuVm.hasMoreFeatured && !menuVm.isLoadingMoreFeatured && menuVm.featuredProjects.isNotEmpty()) {
                            item {
                                LaunchedEffect(Unit) {
                                    menuVm.loadMoreFeaturedProjects()
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is SearchUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.found) + ": ${state.projects.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                                
                                IconButton(onClick = { showFilterSheet = true }) {
                                    Icon(
                                        painter = painterResource(R.drawable.list),
                                        contentDescription = "filter"
                                    )
                                }
                            }
                        }
                        
                        items(state.projects) { project ->
                            ProjectCard(
                                project = project,
                                onClick = {
                                    onProjectClick(project.projectId)
                                }
                            )
                        }
                        
                        // Индикатор загрузки следующей страницы
                        if (menuVm.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        
                        // Триггер для загрузки следующей страницы
                        if (menuVm.hasMoreResults && !menuVm.isLoadingMore && state.projects.isNotEmpty()) {
                            item {
                                LaunchedEffect(Unit) {
                                    menuVm.loadMoreSearchResults()
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                
                is SearchUiState.Error -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Ошибка: ${state.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { menuVm.searchProjects() }) {
                            Text(text = stringResource(R.string.retry))
                        }
                    }
                }
            }
        }
        
        // FAB Menu справа снизу
        ExpandableFabMenu(
            currentMode = menuVm.searchMode,
            selectedVersion = menuVm.selectedVersion,
            gameVersions = menuVm.gameVersions,
            onModeChange = { mode ->
                menuVm.changeSearchMode(mode)
            },
            onVersionChange = { version ->
                menuVm.changeVersion(version)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // Filter Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentSort = menuVm.sortOption,
            onSortChange = { sortOption ->
                menuVm.changeSortOption(sortOption)
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}


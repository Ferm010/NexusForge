package com.example.nexusforge.frontend.mainmenu

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.nexusforge.R
import com.example.nexusforge.data.GameVersion
import com.example.nexusforge.viewmodels.SearchMode

@Composable
fun ExpandableFabMenu(
    currentMode: SearchMode = SearchMode.MODPACK,
    selectedVersion: String?,
    gameVersions: List<GameVersion>,
    onModeChange: (SearchMode) -> Unit = {},
    onVersionChange: (String?) -> Unit = {},
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
            // Опции меню (показываются при expanded = true)
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
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.type_project),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        item {
                            FabMenuItem(
                                text = stringResource(R.string.modpacks),
                                isSelected = currentMode == SearchMode.MODPACK,
                                onClick = {
                                    onModeChange(SearchMode.MODPACK)
                                }
                            )
                        }
                        
                        item {
                            FabMenuItem(
                                text = stringResource(R.string.mods),
                                isSelected = currentMode == SearchMode.MOD,
                                onClick = {
                                    onModeChange(SearchMode.MOD)
                                }
                            )
                        }
                        
                        item {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        item {
                            Text(
                                text = stringResource(R.string.ver_minecraft),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        item {
                            FabMenuItem(
                                text = stringResource(R.string.all_versions),
                                isSelected = selectedVersion == null,
                                onClick = {
                                    onVersionChange(null)
                                }
                            )
                        }
                        
                        items(gameVersions) { version ->
                            FabMenuItem(
                                text = version.version,
                                isSelected = selectedVersion == version.version,
                                onClick = {
                                    onVersionChange(version.version)
                                }
                            )
                        }
                    }
                }
            }
            
            // Главная FAB кнопка
            FloatingActionButton(
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    painter = painterResource(R.drawable.list),
                    contentDescription = if (expanded) "close menu" else "open menu",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
fun FabMenuItem(
    text: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


package com.ferm.nexusforge.frontend.projectdetails

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ferm.nexusforge.R
import com.ferm.nexusforge.data.ModrinthProject
import com.ferm.nexusforge.frontend.components.SkeletonProjectDetails
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailsPage(
    project: ModrinthProject,
    onBackClick: () -> Unit,
    onOpenWebPage: () -> Unit,
    onDownload: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(project.title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.undo),
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onToggleFavorite()
                        val message = if (isFavorite) "Сборка убрана из избранного" else "Сборка сохранена в избранное"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            painter = painterResource(
                                if (isFavorite) R.drawable.add_bookmark_fiiled 
                                else R.drawable.add_bookmark
                            ),
                            contentDescription = if (isFavorite) "delete favorite" else "add favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Аватарка проекта
            if (project.iconUrl != null) {
                AsyncImage(
                    model = project.iconUrl,
                    contentDescription = project.title,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = project.title.first().uppercase(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Название проекта
            Text(
                text = project.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            // Статистика
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.author) + ":",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = project.author,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.downloads) + ":",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatDownloads(project.downloads),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.subscribers) + ":",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = project.follows.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.create_at) + ":",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatFullDate(project.dateCreated),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.last_updates) + ":",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatFullDate(project.dateModified),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // Описание
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.info),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Теги/Категории
            if (project.categories.isNotEmpty()) {
                var categoriesExpanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Заголовок с кликабельной областью
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoriesExpanded = !categoriesExpanded },
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.tags),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${project.categories.size} " + stringResource(R.string.tags),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = if (categoriesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (categoriesExpanded) "unshow" else "show",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Список категорий
                        if (categoriesExpanded) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                project.categories.forEach { category ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(category) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Поддерживаемые версии
            if (project.versions.isNotEmpty()) {
                var versionsExpanded by remember { mutableStateOf(false) }
                
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Заголовок с кликабельной областью
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { versionsExpanded = !versionsExpanded },
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.ver_minecraft),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${project.versions.size} " + stringResource(R.string.versions),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = if (versionsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (versionsExpanded) "unshow" else "show",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Список версий
                        if (versionsExpanded) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(project.versions) { version ->
                                    Text(
                                        text = version,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Кнопки действий
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenWebPage,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.open_modpack))
                }
                
                OutlinedButton(
                    onClick = onDownload,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.button_download))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatDownloads(downloads: Int): String {
    return when {
        downloads >= 1_000_000 -> "${downloads / 1_000_000}M"
        downloads >= 1_000 -> "${downloads / 1_000}K"
        else -> downloads.toString()
    }
}

private fun formatFullDate(dateString: String): String {
    return try {
        val date = ZonedDateTime.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        date.format(formatter)
    } catch (e: Exception) {
        dateString.take(10)
    }
}

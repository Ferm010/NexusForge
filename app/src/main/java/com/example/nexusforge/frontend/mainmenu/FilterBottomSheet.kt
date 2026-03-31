package com.example.nexusforge.frontend.mainmenu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.nexusforge.R
import com.example.nexusforge.viewmodels.SortOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.filter),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Первый столбец
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentSort == SortOption.RELEVANCE,
                        onClick = {
                            onSortChange(SortOption.RELEVANCE)
                            onDismiss()
                        },
                        label = { Text(text = stringResource(R.string.popular)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    FilterChip(
                        selected = currentSort == SortOption.NEWEST,
                        onClick = {
                            onSortChange(SortOption.NEWEST)
                            onDismiss()
                        },
                        label = { Text(text = stringResource(R.string.newest)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Второй столбец
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = currentSort == SortOption.DOWNLOADS_DESC,
                        onClick = {
                            onSortChange(SortOption.DOWNLOADS_DESC)
                            onDismiss()
                        },
                        label = { Text(text = stringResource(R.string.downloads) + "↓") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    FilterChip(
                        selected = currentSort == SortOption.DOWNLOADS_ASC,
                        onClick = {
                            onSortChange(SortOption.DOWNLOADS_ASC)
                            onDismiss()
                        },
                        label = { Text(text = stringResource(R.string.downloads) + "↑") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

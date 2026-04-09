package com.ferm.nexusforge.frontend.mainmenu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ferm.nexusforge.R
import com.ferm.nexusforge.ui.theme.AppIcon
import com.ferm.nexusforge.ui.theme.AppIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    userName: String = "",
    userPhotoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    // Search App Bar - поле поиска по центру
    CenterAlignedTopAppBar(
        title = {
            // Поле поиска по центру
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(text = stringResource(R.string.searchbar)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodySmall,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            )
        },
        navigationIcon = {
            // Кнопка назад/закрыть слева
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.close),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            // Аватарка справа
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (!userPhotoUrl.isNullOrEmpty()) {
                    // Показываем аватарку из Firebase
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = "url avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (userName.isNotEmpty()) {
                    // Показываем первую букву имени
                    Text(
                        text = userName.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    // Показываем знак вопроса
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        windowInsets = WindowInsets(top = 8.dp),
        modifier = modifier
    )
}

@Preview
@Composable
fun SearchAppBarPreview() {
    var query by remember { mutableStateOf("") }

    SearchAppBar(
        searchQuery = query,
        onSearchQueryChange = { query = it },
        onSearch = {},
        onBackClick = {},
        onProfileClick = {},
        userName = "John"
    )
}

package com.example.nexusforge.frontend.mainmenu

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.viewmodels.MainMenuViewModel
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun MainMenuPage(
    vm: RegViewModel = viewModel(),
    menuVm: MainMenuViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onSignOut: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCreateModpack: () -> Unit = {},
    onCreateTemplate: () -> Unit = {}
) {
    BackHandler(enabled = true) { }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Search App Bar
            SearchAppBar(
                searchQuery = menuVm.searchQuery,
                onSearchQueryChange = { menuVm.searchQuery = it },
                onSearch = { query ->
                    // Обработка поиска
                    println("Поиск: $query")
                },
                onBackClick = {
                    // Очистка поиска
                    menuVm.clearSearch()
                },
                onProfileClick = onProfileClick,
                userName = vm.userName,
                userPhotoUrl = vm.userPhotoUrl
            )
            
            // Контент страницы
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (menuVm.searchQuery.isNotEmpty()) {
                    Text(
                        text = "Результаты поиска: ${menuVm.searchQuery}",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                } else {
                    Text(
                        text = "Главное меню",
                        style = MaterialTheme.typography.displayMedium,
                    )
                    if (vm.userName.isNotEmpty()) {
                        Text(
                            text = "Добро пожаловать, ${vm.userName}!",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onSignOut) {
                    Text("Выйти из аккаунта")
                }
            }
        }
        
        // FAB Menu справа снизу
        ExpandableFabMenu(
            onCreateModpack = onCreateModpack,
            onCreateTemplate = onCreateTemplate,
            modifier = Modifier.fillMaxSize()
        )
    }
}


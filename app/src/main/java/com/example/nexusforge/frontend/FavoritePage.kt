package com.example.nexusforge.frontend

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.frontend.components.NameAppBar
import com.example.nexusforge.frontend.mainmenu.SearchAppBar
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun favoritePage(
    vm: RegViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NameAppBar(
            onBackClick = onBackClick,
            onProfileClick = onProfileClick,
            namePage = "Избранное",
            userPhotoUrl = vm.userPhotoUrl
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Избранное",
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}
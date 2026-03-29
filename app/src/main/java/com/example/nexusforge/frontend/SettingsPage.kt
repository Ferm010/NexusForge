package com.example.nexusforge.frontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.R
import com.example.nexusforge.frontend.components.NameAppBar
import com.example.nexusforge.viewmodels.RegViewModel
import com.example.nexusforge.viewmodels.ThemeViewModel

@Composable
fun SettingPage(
    vm: RegViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        NameAppBar(
            onBackClick = onBackClick,
            onProfileClick = onProfileClick,
            namePage = "Настройки",
            userPhotoUrl = vm.userPhotoUrl
        )

        Column(
            modifier = Modifier
                .padding(15.dp)
        ) {
            Text(
                text = "Темы",
                fontSize = 24.sp,
                modifier = Modifier.padding(start = 20.dp)
            )

            Icon(
                painter = painterResource(
                    if (themeViewModel.isDarkTheme == true) R.drawable.balck_theme
                    else R.drawable.white_theme
                ),
                contentDescription = if (themeViewModel.isDarkTheme == true) "Dark Theme" else "Light Theme",
                modifier = Modifier
                    .padding(top = 14.dp)
                    .size(100.dp)
                    .clickable { 
                        themeViewModel.toggleTheme()
                    }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SettingPagePreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = {},
                onProfileClick = {},
                namePage = "Настройки",
                userPhotoUrl = null
            )

            Column(
                modifier = Modifier
                    .padding(15.dp)
            ) {
                Text(
                    text = "Темы",
                    fontSize = 24.sp
                )

                Icon(
                    painter = painterResource(R.drawable.white_theme),
                    contentDescription = "White Theme",
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .size(100.dp)
                        .clickable { }
                )
            }
        }
    }
}

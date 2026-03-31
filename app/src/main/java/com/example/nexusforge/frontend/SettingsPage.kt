package com.example.nexusforge.frontend

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.R
import com.example.nexusforge.frontend.components.NameAppBar
import com.example.nexusforge.viewmodels.LanguageViewModel
import com.example.nexusforge.viewmodels.RegViewModel
import com.example.nexusforge.viewmodels.ThemeViewModel

@Composable
fun SettingPage(
    vm: RegViewModel = viewModel(),
    themeViewModel: ThemeViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onTechnicalSupportClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {}
) {
    val currentLang = languageViewModel.currentLanguage
    
    val technicalSupportText = stringResource(R.string.technical_support)
    val changeLanguageText = stringResource(R.string.change_language)
    
    key(currentLang) {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                namePage = stringResource(R.string.settings),
                userPhotoUrl = vm.userPhotoUrl
            )

            Column(
                modifier = Modifier
                    .padding(15.dp)
            ) {
                Text(
                    text = stringResource(R.string.themes),
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

                Text(
                    text = stringResource(R.string.support),
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 30.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, start = 20.dp, end = 20.dp)
                        .clickable { onTechnicalSupportClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = technicalSupportText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = technicalSupportText,
                        fontSize = 18.sp
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, start = 20.dp, end = 20.dp)
                        .clickable { onLanguageClick() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.translate),
                        contentDescription = changeLanguageText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = changeLanguageText,
                        fontSize = 18.sp
                    )
                }
            }
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
                namePage = "Settings",
                userPhotoUrl = null
            )

            Column(
                modifier = Modifier
                    .padding(15.dp)
            ) {
                Text(
                    text = "Themes",
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

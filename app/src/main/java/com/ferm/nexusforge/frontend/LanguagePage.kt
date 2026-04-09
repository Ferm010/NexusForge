package com.ferm.nexusforge.frontend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.frontend.components.NameAppBar
import com.ferm.nexusforge.viewmodels.LanguageViewModel
import com.ferm.nexusforge.viewmodels.RegViewModel

@Composable
fun LanguagePage(
    vm: RegViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLang = languageViewModel.currentLanguage
    
    key(currentLang) {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                namePage = stringResource(R.string.language_title),
                userPhotoUrl = vm.userPhotoUrl
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.choose_language),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageItem(
                        language = stringResource(R.string.lang_russian),
                        flag = "🇷🇺",
                        isSelected = currentLang == "ru",
                        onClick = { languageViewModel.setLanguage(context, "ru") },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageItem(
                        language = stringResource(R.string.lang_english),
                        flag = "🇬🇧",
                        isSelected = currentLang == "en",
                        onClick = { languageViewModel.setLanguage(context, "en") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageItem(
                        language = stringResource(R.string.lang_german),
                        flag = "🇩🇪",
                        isSelected = currentLang == "de",
                        onClick = { languageViewModel.setLanguage(context, "de") },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageItem(
                        language = stringResource(R.string.lang_spanish),
                        flag = "🇪🇸",
                        isSelected = currentLang == "es",
                        onClick = { languageViewModel.setLanguage(context, "es") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageItem(
                        language = stringResource(R.string.lang_french),
                        flag = "🇫🇷",
                        isSelected = currentLang == "fr",
                        onClick = { languageViewModel.setLanguage(context, "fr") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    language: String,
    flag: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = flag,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = language,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LanguagePagePreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = {},
                onProfileClick = {},
                namePage = "Смена языка",
                userPhotoUrl = null
            )
        }
    }
}

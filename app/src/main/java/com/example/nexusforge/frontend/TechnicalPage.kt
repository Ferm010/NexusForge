package com.example.nexusforge.frontend

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.BuildConfig
import com.example.nexusforge.R
import com.example.nexusforge.frontend.components.NameAppBar
import com.example.nexusforge.ui.theme.logo
import com.example.nexusforge.viewmodels.LanguageViewModel
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun TechnicalPage(
    vm: RegViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val currentLang = languageViewModel.currentLanguage
    
    key(currentLang) {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = onBackClick,
                onProfileClick = onProfileClick,
                namePage = stringResource(R.string.technical_support),
                userPhotoUrl = vm.userPhotoUrl
            )

            TechnicalPageContent()
        }
    }
}

@Composable
private fun TechnicalPageContent() {
    val context = LocalContext.current
    
    val telegramText = stringResource(R.string.telegram)
    val vkText = stringResource(R.string.vk)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(id = R.string.upname),
            style = MaterialTheme.typography.displayMedium,
        )
        Text(
            text = stringResource(id = R.string.downname),
            style = MaterialTheme.typography.displayMedium,
        )
        logo()
        Text(
            text = stringResource(R.string.app_version),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
        )
        Text(
            text = BuildConfig.VERSION_NAME,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(R.string.social_networks),
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.primary,
        )
        Row() {
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/nexusforge_support"))
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(R.drawable.telegram),
                    contentDescription = telegramText
                )
            }
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/club237235048"))
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(R.drawable.vk),
                    contentDescription = vkText
                )
            }
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/Vmrt9BQg42"))
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(R.drawable.discord),
                    contentDescription = "Discord"
                )
            }
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Ferm010"))
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(R.drawable.github),
                    contentDescription = "GitHub"
                )
            }
        }
        Text(
            text = stringResource(R.string.working_hours),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = stringResource(R.string.working_hours_text),
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = stringResource(R.string.by_ferm),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 32.dp),
            fontWeight = FontWeight.Light
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TechnicalPagePreview() {
    MaterialTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            NameAppBar(
                onBackClick = {},
                onProfileClick = {},
                namePage = "Technical Support",
                userPhotoUrl = null
            )
            TechnicalPageContent()
        }
    }
}

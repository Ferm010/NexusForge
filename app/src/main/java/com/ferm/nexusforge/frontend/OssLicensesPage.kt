package com.ferm.nexusforge.frontend

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.frontend.components.NameAppBar
import com.ferm.nexusforge.viewmodels.RegViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LicenseInfo(
    val libraryName: String,
    val licenseText: String
)

@Composable
fun OssLicensesPage(
    vm: RegViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var licenses by remember { mutableStateOf<List<LicenseInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val ossLicensesTitle = stringResource(com.ferm.nexusforge.R.string.oss_licenses_title)
    val loadingLicensesText = stringResource(com.ferm.nexusforge.R.string.loading_licenses)
    val collapseText = stringResource(com.ferm.nexusforge.R.string.collapse)
    val expandText = stringResource(com.ferm.nexusforge.R.string.expand)

    LaunchedEffect(Unit) {
        val result = loadLicenses(context)
        isLoading = false
        if (result.isNotEmpty() && result[0].licenseText.startsWith("Ошибка") || result[0].licenseText.startsWith("Файлы")) {
            errorMessage = result[0].licenseText
        } else {
            licenses = result
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NameAppBar(
            onBackClick = onBackClick,
            onProfileClick = onProfileClick,
            namePage = ossLicensesTitle,
            userPhotoUrl = vm.userPhotoUrl
        )

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = loadingLicensesText,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            else -> {
                LicensesList(
                    licenses = licenses, 
                    context = context,
                    collapseText = collapseText,
                    expandText = expandText
                )
            }
        }
    }
}

@Composable
fun LicensesList(
    licenses: List<LicenseInfo>, 
    context: Context,
    collapseText: String,
    expandText: String
) {
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(licenses.size) { index ->
            val license = licenses[index]
            val isExpanded = expandedStates[index] ?: false

            LicenseCard(
                license = license,
                isExpanded = isExpanded,
                onToggle = { expandedStates[index] = !isExpanded },
                context = context,
                collapseText = collapseText,
                expandText = expandText
            )
        }
    }
}

@Composable
fun LicenseCard(
    license: LicenseInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    context: Context,
    collapseText: String,
    expandText: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = license.libraryName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) collapseText else expandText
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    ClickableLicenseText(
                        text = license.licenseText,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun ClickableLicenseText(text: String, context: Context) {
    val urlPattern = remember { Regex("https?://[^\\s]+") }
    val urls = remember(text) { urlPattern.findAll(text).map { it.value }.toList() }
    val textColor = MaterialTheme.colorScheme.onSurface

    if (urls.isEmpty()) {
        Text(
            text = text,
            fontSize = 13.sp,
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    } else {
        val annotatedString = buildAnnotatedString {
            var lastIndex = 0
            urls.forEach { url ->
                val startIndex = text.indexOf(url, lastIndex)
                if (startIndex >= 0) {
                    // Добавляем текст до ссылки
                    append(text.substring(lastIndex, startIndex))
                    
                    // Добавляем ссылку
                    pushStringAnnotation(tag = "URL", annotation = url)
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    append(url)
                    pop()
                    pop()
                    
                    lastIndex = startIndex + url.length
                }
            }
            // Добавляем оставшийся текст
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }

        androidx.compose.foundation.text.ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                color = textColor
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    }
            }
        )
    }
}

private suspend fun loadLicenses(context: Context): List<LicenseInfo> = withContext(Dispatchers.IO) {
    try {
        val licensesResId = context.resources.getIdentifier(
            "third_party_licenses",
            "raw",
            context.packageName
        )
        val metadataResId = context.resources.getIdentifier(
            "third_party_license_metadata",
            "raw",
            context.packageName
        )
        
        if (licensesResId == 0 || metadataResId == 0) {
            return@withContext listOf(
                LicenseInfo(
                    context.getString(com.ferm.nexusforge.R.string.license_info),
                    context.getString(com.ferm.nexusforge.R.string.license_files_not_found)
                )
            )
        }
        
        val licenses = context.resources.openRawResource(licensesResId)
            .bufferedReader().use { it.readText() }
        val metadata = context.resources.openRawResource(metadataResId)
            .bufferedReader().use { it.readLines() }
        
        val result = mutableListOf<LicenseInfo>()
        
        metadata.forEach { line ->
            // Формат: "offset:length название_библиотеки"
            val firstColonIndex = line.indexOf(':')
            if (firstColonIndex > 0) {
                val offset = line.substring(0, firstColonIndex).toIntOrNull() ?: 0
                val remaining = line.substring(firstColonIndex + 1)
                
                val spaceIndex = remaining.indexOf(' ')
                if (spaceIndex > 0) {
                    val length = remaining.substring(0, spaceIndex).toIntOrNull() ?: 0
                    val libraryName = remaining.substring(spaceIndex + 1).trim()
                    
                    val licenseText = if (offset >= 0 && length > 0 && offset + length <= licenses.length) {
                        licenses.substring(offset, offset + length).trim()
                    } else {
                        context.getString(com.ferm.nexusforge.R.string.license_unavailable)
                    }
                    
                    result.add(LicenseInfo(libraryName, licenseText))
                }
            }
        }
        
        if (result.isEmpty()) {
            listOf(LicenseInfo(
                context.getString(com.ferm.nexusforge.R.string.license_info), 
                context.getString(com.ferm.nexusforge.R.string.licenses_not_found)
            ))
        } else {
            result
        }
    } catch (e: Exception) {
        listOf(
            LicenseInfo(
                context.getString(com.ferm.nexusforge.R.string.error_title),
                context.getString(com.ferm.nexusforge.R.string.error_loading_licenses, e.message ?: "")
            )
        )
    }
}

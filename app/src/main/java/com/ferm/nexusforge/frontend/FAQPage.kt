package com.ferm.nexusforge.frontend

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.frontend.components.NameAppBar
import com.ferm.nexusforge.viewmodels.RegViewModel

data class FAQItemData(
    val question: String,
    val answer: String,
    val icon: ImageVector,
    val iconColor: androidx.compose.ui.graphics.Color
)

@Composable
fun FAQPage(
    vm: RegViewModel = viewModel(),
    onBackClick: () -> Unit = {}
) {
    val faqItems = listOf(
        FAQItemData(
            question = stringResource(R.string.faq_install_zip),
            answer = stringResource(R.string.faq_install_zip_answer),
            icon = Icons.Default.Download,
            iconColor = MaterialTheme.colorScheme.primary
        ),
        FAQItemData(
            question = stringResource(R.string.faq_install_mrpack),
            answer = stringResource(R.string.faq_install_mrpack_answer),
            icon = Icons.Default.Info,
            iconColor = MaterialTheme.colorScheme.secondary
        ),
        FAQItemData(
            question = stringResource(R.string.faq_minecraft_folder),
            answer = stringResource(R.string.faq_minecraft_folder_answer),
            icon = Icons.Default.Folder,
            iconColor = MaterialTheme.colorScheme.tertiary
        ),
        FAQItemData(
            question = stringResource(R.string.faq_minecraft_version),
            answer = stringResource(R.string.faq_minecraft_version_answer),
            icon = Icons.Default.VerifiedUser,
            iconColor = MaterialTheme.colorScheme.primary
        ),
        FAQItemData(
            question = stringResource(R.string.faq_mods_not_loading),
            answer = stringResource(R.string.faq_mods_not_loading_answer),
            icon = Icons.Default.Error,
            iconColor = MaterialTheme.colorScheme.error
        ),
        FAQItemData(
            question = stringResource(R.string.faq_update_modpack),
            answer = stringResource(R.string.faq_update_modpack_answer),
            icon = Icons.Default.Update,
            iconColor = MaterialTheme.colorScheme.tertiary
        ),
        FAQItemData(
            question = stringResource(R.string.faq_ram_requirements),
            answer = stringResource(R.string.faq_ram_requirements_answer),
            icon = Icons.Default.Memory,
            iconColor = MaterialTheme.colorScheme.primary
        )
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        NameAppBar(
            onBackClick = onBackClick,
            onProfileClick = {},
            namePage = stringResource(R.string.faq_answers),
            userPhotoUrl = vm.userPhotoUrl
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(faqItems.size) { index ->
                FAQCardItem(faqItems[index])
            }
            
            item {
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                }
            }
        }
    }
}

@Composable
private fun FAQCardItem(item: FAQItemData) {
    val isExpanded = remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded.value = !isExpanded.value }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = item.iconColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = item.iconColor
                    )
                }
                
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    imageVector = if (isExpanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded.value) "Свернуть" else "Развернуть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 76.dp, end = 16.dp, bottom = 16.dp)
                )
            }
        }
    }
}

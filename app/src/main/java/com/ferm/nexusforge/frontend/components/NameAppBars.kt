package com.ferm.nexusforge.frontend.components

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameAppBar(
    namePage: String,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    userName: String = "",
    userPhotoUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val backContentDesc = stringResource(R.string.back)
    val profileContentDesc = stringResource(R.string.profile)
    
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = namePage,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.undo),
                    contentDescription = backContentDesc
                )
            }
        },
        actions = {
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
                    AsyncImage(
                        model = userPhotoUrl,
                        contentDescription = profileContentDesc,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (userName.isNotEmpty()) {
                    Text(
                        text = userName.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
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

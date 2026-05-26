package com.ferm.nexusforge.frontend

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.repository.UploadProgress
import com.ferm.nexusforge.viewmodels.ModpackCreatorViewModel

data class GenerateProgress(
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val currentModName: String = "",
    val isComplete: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateModpackPage(
    vm: ModpackCreatorViewModel = viewModel(),
    method: String = "local",
    onBackClick: () -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    
    var progress by remember { mutableStateOf(GenerateProgress()) }
    var driveUploadProgress by remember { mutableStateOf<UploadProgress>(UploadProgress.Idle) }
    
    // Google Drive
    @Suppress("DEPRECATION")
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.result
                vm.getGoogleDriveRepository()?.initializeDriveService(account)
                
                // Начинаем загрузку после успешной авторизации
                vm.exportToGoogleDrive(context) { currentStep, modName, isComplete, error ->
                    progress = GenerateProgress(
                        currentStep = currentStep,
                        totalSteps = state.selectedMods.size + 2,
                        currentModName = modName,
                        isComplete = isComplete,
                        error = error
                    )
                }
            } catch (e: Exception) {
                progress = progress.copy(error = "Google Drive authorization failed: ${e.message}")
            }
        } else {
            progress = progress.copy(error = "Google Drive authorization cancelled")
        }
    }
    
    LaunchedEffect(Unit) {
        try {
            vm.initializeGoogleDrive(context)
        } catch (_: Exception) {
            // Ошибка инициализации Google Drive
        }
        
        val totalMods = state.selectedMods.size
        progress = progress.copy(currentStep = 0, totalSteps = totalMods)
        
        // Генерация в зависимости от метода
        when (method) {
            "local" -> {
                vm.generateModpackWithProgress(context) { currentStep, modName, isComplete, error ->
                    progress = GenerateProgress(
                        currentStep = currentStep,
                        totalSteps = totalMods,
                        currentModName = modName,
                        isComplete = isComplete,
                        error = error
                    )
                }
            }
            "mrpack" -> {
                vm.generateMrpackWithProgress(context) { currentStep, modName, isComplete, error ->
                    progress = GenerateProgress(
                        currentStep = currentStep,
                        totalSteps = totalMods,
                        currentModName = modName,
                        isComplete = isComplete,
                        error = error
                    )
                }
            }
             "google_drive" -> {
                  try {
                      val driveRepo = vm.getGoogleDriveRepository()
                      if (driveRepo != null) {
                          // Всегда показываем диалог выбора аккаунта
                          googleSignInLauncher.launch(driveRepo.getAuthorizationIntent())
                      } else {
                          progress = progress.copy(error = "Google Drive not initialized")
                      }
                  } catch (e: Exception) {
                      progress = progress.copy(error = "Google Drive error: ${e.message}")
                  }
              }
        }
    }
    
    // прогресс загрузки в Drive
    LaunchedEffect(method) {
        if (method == "google_drive") {
            vm.getGoogleDriveRepository()?.uploadProgress?.collect { uploadProgress ->
                driveUploadProgress = uploadProgress
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.generating)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LaunchedEffect(progress.isComplete, progress.error) {
            if (progress.isComplete && progress.error == null) {
                onComplete()
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (progress.isComplete && progress.error == null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    item {
                        Text(
                            text = stringResource(R.string.modpack_created),
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    item {
                        Text(
                            text = when (method) {
                                "mrpack" -> "${state.modpackName}.mrpack"
                                "google_drive" -> {
                                    when (driveUploadProgress) {
                                        is UploadProgress.Success -> "Uploaded to Google Drive successfully!"
                                        else -> "${state.modpackName}.zip"
                                    }
                                }
                                else -> "${state.modpackName}.zip"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Показываем ссылку на файл в Google Drive
                    if (method == "google_drive" && driveUploadProgress is UploadProgress.Success) {
                        val successProgress = driveUploadProgress as UploadProgress.Success
                        if (successProgress.webViewLink != null) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                     Text(
                                         text = stringResource(R.string.file_id, successProgress.fileId),
                                         style = MaterialTheme.typography.bodySmall,
                                         color = MaterialTheme.colorScheme.onSurfaceVariant
                                     )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                         Button(
                                             onClick = {
                                                 val intent = Intent(Intent.ACTION_VIEW).apply {
                                                     data = android.net.Uri.parse(successProgress.webViewLink)
                                                 }
                                                 context.startActivity(intent)
                                             },
                                             modifier = Modifier.weight(1f)
                                         ) {
                                             Text(stringResource(R.string.open_in_drive))
                                         }
                                         
                                         Button(
                                             onClick = {
                                                 val shareIntent = Intent().apply {
                                                     action = Intent.ACTION_SEND
                                                     putExtra(Intent.EXTRA_TEXT, successProgress.webViewLink)
                                                     type = "text/plain"
                                                 }
                                                 context.startActivity(Intent.createChooser(shareIntent, "Share link"))
                                             },
                                             modifier = Modifier.weight(1f)
                                         ) {
                                             Text(stringResource(R.string.share))
                                         }
                                    }
                                }
                            }
                        }
                    }
                    
                    // FAQ
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        val faqQuestion = when (method) {
                            "mrpack" -> stringResource(R.string.faq_install_mrpack)
                            else -> stringResource(R.string.faq_install_zip)
                        }

                        val faqAnswer = when (method) {
                            "mrpack" -> stringResource(R.string.faq_install_mrpack_answer)
                            else -> stringResource(R.string.faq_install_zip_answer)
                        }
                        
                        val icon = when (method) {
                            "mrpack" -> Icons.Default.Info
                            else -> Icons.Default.Download
                        }
                        
                        val iconColor = when (method) {
                            "mrpack" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(
                                                color = iconColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = iconColor
                                        )
                                    }
                                    
                                    Text(
                                        text = faqQuestion,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                Text(
                                    text = faqAnswer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 12.dp, start = 60.dp)
                                )
                            }
                        }
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else if (progress.error != null) {
                
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.error),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = progress.error ?: "Unknown error",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onBackClick) {
                    Text(stringResource(R.string.go_back))
                }
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = stringResource(R.string.generating),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                LinearProgressIndicator(
                    progress = { 
                        if (progress.totalSteps > 0) {
                            progress.currentStep.toFloat() / progress.totalSteps.toFloat()
                        } else 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.generation_progress, progress.currentStep, progress.totalSteps),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                if (progress.currentModName.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = progress.currentModName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                if (method == "google_drive" && driveUploadProgress is UploadProgress.Uploading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.uploading_to_drive),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}


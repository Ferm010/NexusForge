package com.example.nexusforge.frontend

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.R
import com.example.nexusforge.viewmodels.ModpackCreatorViewModel
import kotlinx.coroutines.delay

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
    
    var hasStoragePermission by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(GenerateProgress()) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasStoragePermission = isGranted
    }
    
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasStoragePermission = true
        } else {
            val permission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            hasStoragePermission = permission == PackageManager.PERMISSION_GRANTED
        }
        
        progress = progress.copy(currentStep = 0, totalSteps = state.selectedMods.size + 2)
        
        // Генерация в зависимости от метода
        when (method) {
            "local" -> {
                vm.generateModpackWithProgress(context) { currentStep, modName, isComplete, error ->
                    progress = GenerateProgress(
                        currentStep = currentStep,
                        totalSteps = state.selectedMods.size + 2,
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
                        totalSteps = state.selectedMods.size + 4,
                        currentModName = modName,
                        isComplete = isComplete,
                        error = error
                    )
                }
            }
            "google_drive" -> {
                // TODO: Реализовать загрузку в Google Drive
                vm.generateModpackWithProgress(context) { currentStep, modName, isComplete, error ->
                    progress = GenerateProgress(
                        currentStep = currentStep,
                        totalSteps = state.selectedMods.size + 2,
                        currentModName = modName,
                        isComplete = isComplete,
                        error = error
                    )
                }
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
                kotlinx.coroutines.delay(1500)
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.modpack_created),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (method) {
                        "mrpack" -> "${state.modpackName}.mrpack"
                        else -> "${state.modpackName}.zip"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (progress.error != null) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Error",
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
                    Text("Go Back")
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
                    text = "${progress.currentStep} / ${progress.totalSteps}",
                    style = MaterialTheme.typography.bodyMedium
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
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressStep(
                        step = 1,
                        currentStep = progress.currentStep,
                        text = "Preparing files..."
                    )
                    ProgressStep(
                        step = progress.currentStep,
                        currentStep = progress.currentStep,
                        text = "Downloading mods (${progress.currentStep}/${progress.totalSteps})"
                    )
                    ProgressStep(
                        step = progress.totalSteps,
                        currentStep = progress.currentStep,
                        text = "Creating ZIP archive..."
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStep(
    step: Int,
    currentStep: Int,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (step < currentStep) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (step == currentStep) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

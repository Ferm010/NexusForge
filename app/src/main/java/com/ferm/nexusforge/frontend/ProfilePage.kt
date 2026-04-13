package com.ferm.nexusforge.frontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.ferm.nexusforge.R
import com.ferm.nexusforge.frontend.components.NameAppBar
import com.ferm.nexusforge.viewmodels.LanguageViewModel
import com.ferm.nexusforge.viewmodels.RegViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(
    vm: RegViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        vm.setContext(context)
    }
    
    val currentLang = languageViewModel.currentLanguage
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    
    var showChangeDataSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }
    
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    
    val changeDataText = stringResource(R.string.change_data)
    val deleteAccountText = stringResource(R.string.delete_account)
    val signOutText = stringResource(R.string.sign_out_account)
    val noUsernameText = stringResource(R.string.no_username)
    val googleErrorText = stringResource(R.string.google_account_error)
    val profileTitleText = stringResource(R.string.profile)
    val settingsProfileText = stringResource(R.string.settings_profile)
    val changeDataTitleText = stringResource(R.string.change_data_title)
    val newNameText = stringResource(R.string.new_name)
    val newEmailText = stringResource(R.string.new_email)
    val currentPasswordText = stringResource(R.string.current_password)
    val saveText = stringResource(R.string.save)
    val cancelText = stringResource(R.string.cancel)
    val confirmDeleteText = stringResource(R.string.confirm_delete_account)
    val confirmSignOutText = stringResource(R.string.confirm_sign_out)
    val signOutMessageText = stringResource(R.string.sign_out_message)
    val displayName = vm.userName.ifEmpty { noUsernameText }
    
    fun clearGoogleError() {
        googleError = null
    }
    
    fun showGoogleError() {
        googleError = googleErrorText
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NameAppBar(
            onBackClick = onBackClick,
            onProfileClick = onProfileClick,
            namePage = profileTitleText,
            userPhotoUrl = vm.userPhotoUrl
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.size(32.dp))

            Box(
                contentAlignment = Alignment.Center
            ) {
                if (vm.userPhotoUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(vm.userPhotoUrl),
                        contentDescription = displayName,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF6650a4), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, Color(0xFF6650a4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = noUsernameText,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )

            Spacer(modifier = Modifier.size(48.dp))

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = settingsProfileText,
                    fontSize = 24.sp
                )
                
                googleError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 14.dp)
                        .clickable {
                            clearGoogleError()
                            if (vm.isGoogleSignIn()) {
                                showGoogleError()
                            } else {
                                newName = vm.userName
                                newEmail = ""
                                currentPassword = ""
                                showChangeDataSheet = true
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.person),
                        contentDescription = changeDataText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = changeDataText,
                        fontSize = 18.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 14.dp)
                        .clickable {
                            clearGoogleError()
                            if (vm.isGoogleSignIn()) {
                                showGoogleError()
                            } else {
                                showDeleteDialog = true
                            }
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = deleteAccountText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = deleteAccountText,
                        fontSize = 18.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 14.dp)
                        .clickable {
                            showSignOutDialog = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.logout),
                        contentDescription = signOutText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = signOutText,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
    
    if (showChangeDataSheet) {
        ModalBottomSheet(
            onDismissRequest = { showChangeDataSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = changeDataTitleText,
                    fontSize = 22.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(newNameText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text(newEmailText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(currentPasswordText) },
                    placeholder = { Text("*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showChangeDataSheet = false }) {
                        Text(cancelText)
                    }
                    Button(
                        onClick = {
                            if (currentPassword.isEmpty()) return@Button
                            scope.launch {
                                if (newName.isNotEmpty() && newName != vm.userName) {
                                    vm.updateDisplayName(
                                        newName = newName,
                                        onSuccess = { },
                                        onError = { }
                                    )
                                }
                                if (newEmail.isNotEmpty() && currentPassword.isNotEmpty()) {
                                    vm.updateEmail(
                                        newEmail = newEmail,
                                        password = currentPassword,
                                        onSuccess = { showChangeDataSheet = false },
                                        onError = { }
                                    )
                                }
                                if (newName.isEmpty() && newEmail.isEmpty()) {
                                    showChangeDataSheet = false
                                }
                            }
                        },
                        enabled = currentPassword.isNotEmpty()
                    ) {
                        Text(saveText)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(confirmDeleteText) },
            text = {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text(currentPasswordText) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteAccount(
                            password = currentPassword,
                            onSuccess = { 
                                showDeleteDialog = false
                                onSignOut()
                            },
                            onError = { }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(deleteAccountText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(cancelText)
                }
            }
        )
    }
    
    if (showSignOutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(confirmSignOutText) },
            text = { Text(signOutMessageText) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.signOut()
                        showSignOutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(signOutText)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(cancelText)
                }
            }
        )
    }
}

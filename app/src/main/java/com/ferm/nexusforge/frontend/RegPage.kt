package com.ferm.nexusforge.frontend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.backend.WEB_CLIENT_ID
import com.ferm.nexusforge.ui.theme.logo
import com.ferm.nexusforge.viewmodels.RegViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch


@Composable
fun RegPageScreen(
    modifier: Modifier = Modifier,
    vm: RegViewModel = viewModel(),
    onNavigateToEula: () -> Unit,
    onNavigateToAuthPassword: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
    onNavigateToLanguage: () -> Unit = {}
){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var authMethodConflictError by remember { mutableStateOf<String?>(null) }
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    
    val signInGoogleText = stringResource(R.string.sign_in_google)
    val noGoogleAccountsMessage = stringResource(R.string.no_google_accounts)
    val signInErrorMessage = stringResource(R.string.sign_in_error, "")

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Кнопка смены языка в верхнем левом углу
        androidx.compose.material3.IconButton(
            onClick = onNavigateToLanguage,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.translate),
                contentDescription = stringResource(R.string.change_language),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
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

            OutlinedTextField(
                value = vm.email,
                onValueChange = {
                    vm.onEmailChanged(it, context)
                    emailError = null
                    authMethodConflictError = null
                },
                label = { Text(stringResource(R.string.email)) },
                singleLine = true,
                isError = emailError != null || vm.emailError != null || authMethodConflictError != null,
                supportingText = {
                    when {
                        authMethodConflictError != null -> Text(
                            text = authMethodConflictError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        emailError != null -> Text(
                            text = emailError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        vm.emailError != null -> Text(
                            text = vm.emailError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        vm.isValidatingEmail -> Text(
                            text = stringResource(R.string.checking_email),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
            OutlinedButton(
                onClick = {
                    if (isGoogleSigningIn) return@OutlinedButton
                    isGoogleSigningIn = true
                    googleSignInError = null
                    authMethodConflictError = null
                    coroutineScope.launch {
                        try {
                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(WEB_CLIENT_ID)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val result = credentialManager.getCredential(context, request)
                            val googleIdTokenCredential =
                                GoogleIdTokenCredential.createFrom(result.credential.data)
                            
                            // Email находится в credential.id
                            val googleEmail = googleIdTokenCredential.id
                            
                            vm.signInWithGoogle(
                                context = context,
                                idToken = googleIdTokenCredential.idToken,
                                email = googleEmail,
                                onSuccess = { isNewUser ->
                                    isGoogleSigningIn = false
                                    googleSignInError = null
                                    authMethodConflictError = null
                                    if (isNewUser) {
                                        onNavigateToEula()
                                    } else {
                                        onNavigateToMainMenu()
                                    }
                                },
                                onError = { error ->
                                    isGoogleSigningIn = false
                                    if (error.contains("существует") || error.contains("другим способом") || 
                                        error.contains("создан другим способом")) {
                                        authMethodConflictError = error
                                        googleSignInError = null
                                    } else {
                                        googleSignInError = error
                                        authMethodConflictError = null
                                    }
                                }
                            )
                        } catch (e: GetCredentialException) {
                            isGoogleSigningIn = false
                            googleSignInError = when {
                                e.message?.contains("No credentials available") == true -> 
                                    noGoogleAccountsMessage
                                else -> signInErrorMessage.replace("{0}", e.message ?: "")
                            }
                        } catch (e: Exception) {
                            isGoogleSigningIn = false
                            googleSignInError = signInErrorMessage.replace("{0}", e.message ?: "")
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp),
                enabled = !isGoogleSigningIn
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    signInGoogleText,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            if (googleSignInError != null) {
                Text(
                    text = googleSignInError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 12.dp, bottom = 12.dp)
                .align(Alignment.BottomEnd)
        ) {
            Button(
                onClick = {
                    vm.checkEmailAndNavigate(
                        context = context,
                        onExists = onNavigateToAuthPassword,
                        onNotExists = onNavigateToEula,
                        onError = { emailError = it }
                    )
                },
                enabled = vm.isContinueEnabled && !vm.isValidatingEmail && emailError == null && authMethodConflictError == null
            ) {
                if (vm.isValidatingEmail) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text(stringResource(R.string.continue_btn))
            }
        }

        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = stringResource(R.string.by_ferm),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}
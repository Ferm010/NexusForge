package com.example.nexusforge.frontend

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.R
import com.example.nexusforge.backend.WEB_CLIENT_ID
import com.example.nexusforge.ui.theme.NexusForgeTheme
import com.example.nexusforge.ui.theme.logo
import com.example.nexusforge.viewmodels.RegViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch


@Composable
fun RegPageScreen(
    vm: RegViewModel = viewModel(),
    modifier: Modifier = Modifier,
    onNavigateToEula: () -> Unit,
    onNavigateToAuthPassword: () -> Unit,
    onNavigateToMainMenu: () -> Unit
){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var googleSignInError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Центральный контент (имя приложения, лого, поле ввода, кнопка Google)
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
                    vm.onEmailChanged(it)
                    emailError = null
                },
                label = { Text("Email") },
                singleLine = true,
                isError = vm.isError || emailError != null,
                supportingText = {
                    when {
                        emailError != null -> Text(text = emailError!!)
                        vm.isError -> Text(text = "Введите корректный email")
                    }
                }
            )
            OutlinedButton(
                onClick = {
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
                            vm.signInWithGoogle(
                                idToken = googleIdTokenCredential.idToken,
                                onSuccess = { isNewUser ->
                                    googleSignInError = null
                                    if (isNewUser) onNavigateToEula() else onNavigateToMainMenu()
                                },
                                onError = { googleSignInError = it }
                            )
                        } catch (e: GetCredentialException) {
                            // Пользователь отменил выбор аккаунта или нет доступных аккаунтов
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Продолжить через Google",
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

        // 2. Кнопка "Продолжить" в правом нижнем углу
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
                        onExists = onNavigateToAuthPassword,
                        onGoogleOnly = {
                            emailError = "Этот email зарегистрирован через Google. " +
                                    "Войдите через кнопку «Продолжить через Google»."
                        },
                        onNotExists = onNavigateToEula
                    )
                },
                enabled = vm.isContinueEnabled
            ) {
                Text("Продолжить")
            }
        }

        // 3. Текст "By Ferm" в нижнем центре
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = "By Ferm",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ContentPreview() {
    RegPageScreen(onNavigateToEula = {}, onNavigateToAuthPassword = {}, onNavigateToMainMenu = {})
}
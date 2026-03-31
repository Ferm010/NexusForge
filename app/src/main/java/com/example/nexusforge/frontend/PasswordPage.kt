package com.example.nexusforge.frontend

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.R
import com.example.nexusforge.ui.theme.logo
import com.example.nexusforge.viewmodels.LanguageViewModel
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun PasswordPage(
    vm: RegViewModel = viewModel(),
    languageViewModel: LanguageViewModel = viewModel(),
    modifier: Modifier = Modifier, 
    onNavigateToRegName: () -> Unit = {}
) {
    val isPasswordValid = vm.password.length >= 6
    val passwordsMatch = vm.password == vm.confirmPassword
    val canContinue = isPasswordValid && passwordsMatch && vm.confirmPassword.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            logo()
            Text(
                text = stringResource(R.string.create_password),
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedTextField(
                value = vm.password,
                onValueChange = { vm.password = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = vm.password.isNotEmpty() && !isPasswordValid,
                supportingText = {
                    if (vm.password.isNotEmpty() && !isPasswordValid) {
                        Text(stringResource(R.string.password_hint))
                    }
                }
            )
            OutlinedTextField(
                value = vm.confirmPassword,
                onValueChange = { vm.confirmPassword = it },
                label = { Text(stringResource(R.string.confirm_password)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                isError = vm.confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = {
                    if (vm.confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Text(stringResource(R.string.passwords_dont_match))
                    }
                }
            )
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
                onClick = onNavigateToRegName,
                enabled = canContinue
            ) {
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PasswordPagePreview() {
    PasswordPage()
}

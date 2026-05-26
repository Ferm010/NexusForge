package com.ferm.nexusforge.frontend

import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.ui.theme.logo
import com.ferm.nexusforge.viewmodels.RegViewModel

@Composable
fun RegNamePage(
    modifier: Modifier = Modifier,
    vm: RegViewModel = viewModel(),
    onNavigateToMainMenu: () -> Unit = {}
){
    val context = LocalContext.current
    val registrationSuccessMessage = stringResource(R.string.registration_success)
    var registerError by remember { mutableStateOf<String?>(null) }
    val isNameValid = vm.userName.trim().length >= 3

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            logo()
            Text(
                text = stringResource(R.string.create_name),
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedTextField(
                value = vm.userName,
                onValueChange = { newValue: String ->
                    vm.userName = newValue
                    registerError = null
                },
                label = { Text(stringResource(R.string.name)) },
                singleLine = true,
                isError = vm.userName.isNotEmpty() && !isNameValid,
                supportingText = {
                    if (vm.userName.isNotEmpty() && !isNameValid) {
                        Text(text = stringResource(R.string.name_hint))
                    }
                }
            )
            if (registerError != null) {
                Text(
                    text = registerError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
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
                    vm.userName = vm.userName.trim()
                    vm.registerUser(
                        context = context,
                        onSuccess = {
                            Toast.makeText(context, registrationSuccessMessage, Toast.LENGTH_SHORT).show()
                            onNavigateToMainMenu()
                        },
                        onError = { registerError = it }
                    )
                },
                enabled = isNameValid
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
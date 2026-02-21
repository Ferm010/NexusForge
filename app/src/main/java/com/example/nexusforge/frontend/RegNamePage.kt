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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexusforge.ui.theme.logo
import com.example.nexusforge.viewmodels.RegViewModel

@Composable
fun RegNamePage(vm: RegViewModel = viewModel(), modifier: Modifier = Modifier, onNavigateToMainMenu: () -> Unit = {}){
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var registerError by remember { mutableStateOf<String?>(null) }
    val isNameValid = name.trim().length >= 3

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
                text = "Придумайте имя",
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.size(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { newValue: String ->
                    name = newValue
                    isError = name.isNotEmpty() && !isNameValid
                    registerError = null
                },
                label = { Text("Имя") },
                singleLine = true,
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(text = "Введите имя от 3 символов")
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
                    vm.userName = name.trim()
                    vm.registerUser(
                        onSuccess = onNavigateToMainMenu,
                        onError = { registerError = it }
                    )
                },
                enabled = isNameValid
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
fun RegNamePreview(){
    RegNamePage()
}
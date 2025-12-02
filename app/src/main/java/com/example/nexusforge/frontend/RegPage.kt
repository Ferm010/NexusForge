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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nexusforge.R
import com.example.nexusforge.ui.theme.NexusForgeTheme
import com.example.nexusforge.ui.theme.logo


@Composable
fun RegPageScreen(modifier: Modifier = Modifier, onNavigateToEula: () -> Unit){
    // Состояния для TextField
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val isEmailValid = email.isNotBlank() && email.contains("@")

    // Используем Box для наложения элементов (центральный контент, кнопка внизу справа, автор внизу по центру)
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
                value = email,
                onValueChange = { newValue: String ->
                    email = newValue
                    isError = !newValue.contains("@") && newValue.isNotEmpty()
                },
                label = { Text("Email") },
                singleLine = true,
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text = "Введите корректный email"
                        )
                    }
                }
            )
            OutlinedButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.padding(top = 8.dp) // Небольшой отступ
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
        }

        // 2. Кнопка "Продолжить" в правом нижнем углу
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(end = 12.dp, bottom = 12.dp) // Добавлен отступ снизу для наглядности
                .align(Alignment.BottomEnd) // Явное выравнивание внутри Box
        ) {
            Button(
                onClick = onNavigateToEula,
                enabled = isEmailValid
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
                .align(Alignment.BottomCenter) // Явное выравнивание внутри Box
        ) {
            Text(
                text = "By Ferm",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 12.dp) // Отступ снизу, чтобы не перекрывать кнопку
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ContentPreview() {
    NexusForgeTheme {

    }
}
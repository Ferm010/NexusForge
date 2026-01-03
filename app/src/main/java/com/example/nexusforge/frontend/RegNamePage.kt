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
import com.example.nexusforge.ui.theme.logo

@Composable
fun RegNamePage(modifier: Modifier = Modifier){
    var name by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
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
                },
                label = { Text("Имя") },
                singleLine = true,
                isError = isError,
                supportingText = {
                    if (isError) {
                        Text(
                            text = "Введите имя от 3 символов"
                        )
                    }
                }
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
                 // Явное выравнивание внутри Box
        ) {
            Button(
                onClick = { /*TODO*/ },
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
                // Явное выравнивание внутри Box
        ) {
            Text(
                text = "By Ferm",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 12.dp) // Отступ снизу, чтобы не перекрывать кнопку
            )
        }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegNamePreview(){
    RegNamePage()
}
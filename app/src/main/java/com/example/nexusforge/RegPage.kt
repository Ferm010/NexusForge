package com.example.nexusforge

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import com.example.nexusforge.ui.theme.NexusForgeTheme


@Composable
fun Content(modifier: Modifier = Modifier){

    val blackAnvil = painterResource(id = R.drawable.anvil_black_logo_png)
    val whiteAnvil = painterResource(id = R.drawable.anvil_white_logo_png)
    var email by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.upname),
            style = MaterialTheme.typography.displayMedium,
            )
        Text(
            text = stringResource(id = R.string.downname),
            style = MaterialTheme.typography.displayMedium,
        )
        if (isSystemInDarkTheme()) {
            Image(
                painter = whiteAnvil,
                contentDescription = null,
            )
        } else {
            Image(
                painter = blackAnvil,
                contentDescription = null,
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { newValue: String -> // ← тип указан явно
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
        OutlinedButton(onClick = { /*TODO*/}) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp)
            )
            Text(
                "Продолжить через Google"
            )
        }
    }
    ButtonNext(email = email)
}

@Composable
fun ButtonNext(email: String, modifier: Modifier = Modifier) {
    val isEmailValid = email.isNotBlank() && email.contains("@")
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom,
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(end = 12.dp)
        ) {
        Button(
            onClick = { /*TODO*/ },
            enabled = isEmailValid
        ) {
            Text("Продолжить")
        }
    }
}
@Composable
fun Author(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Text(
            text = "By Ferm",
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ContentPreview() {
    NexusForgeTheme {
        Content()
        Author()
    }
}
package com.example.nexusforge.frontend

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.nexusforge.R

@Composable
fun CreateAlertDialog(
    onDismiss: () -> Unit,
    onCreateModpack: () -> Unit,
    onCreateTemplate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.select_dialog))
        },
        text = {
            Text(text = stringResource(R.string.select_type))
        },
        confirmButton = {
            TextButton(onClick = {
                onCreateModpack()
                onDismiss()
            }) {
                Text(text = stringResource(R.string.modpacks))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onCreateTemplate()
                onDismiss()
            }) {
                Text(text = stringResource(R.string.sample))
            }
        }
    )
}

@Preview
@Composable
fun alertPreview() {
    CreateAlertDialog(
        onDismiss = {},
        onCreateModpack = {},
        onCreateTemplate = {}
    )
}
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ferm.nexusforge.R
import com.ferm.nexusforge.ui.theme.logo
import com.ferm.nexusforge.viewmodels.RegViewModel

@Composable
fun ForgotPasswordPage(
    modifier: Modifier = Modifier,
    vm: RegViewModel = viewModel(),
    prefilledEmail: String = "",
    onSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val passwordResetSentMessage = stringResource(R.string.password_reset_sent)
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isEmailSent by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            logo()
            Text(
                text = stringResource(R.string.forgot_password),
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.size(16.dp))
            
            if (!isEmailSent) {
                Text(
                    text = stringResource(R.string.forgot_password_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.size(24.dp))
                
                Text(
                    text = prefilledEmail,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = stringResource(R.string.reset_email_sent),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = prefilledEmail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
            
            if (error != null) {
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
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
            if (!isEmailSent) {
                Button(
                    onClick = {
                        isLoading = true
                        vm.sendPasswordResetEmail(
                            context = context,
                            email = prefilledEmail,
                            onSuccess = {
                                isLoading = false
                                isEmailSent = true
                                Toast.makeText(context, passwordResetSentMessage, Toast.LENGTH_SHORT).show()
                            },
                            onError = { errorMsg ->
                                isLoading = false
                                error = errorMsg
                            }
                        )
                    },
                    enabled = prefilledEmail.isNotEmpty() && !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(stringResource(R.string.send_reset_link))
                    }
                }
            } else {
                Button(
                    onClick = onSuccess,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        }
    }
}


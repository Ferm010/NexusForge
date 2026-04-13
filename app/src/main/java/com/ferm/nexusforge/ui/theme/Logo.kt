package com.ferm.nexusforge.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.ferm.nexusforge.R

@Composable
fun logo() {
    val blackAnvil = painterResource(id = R.drawable.anvil_black_logo_png)
    val whiteAnvil = painterResource(id = R.drawable.anvil_white_logo_png)

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
}

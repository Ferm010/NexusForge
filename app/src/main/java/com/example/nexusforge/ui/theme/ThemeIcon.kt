package com.example.nexusforge.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.nexusforge.R

data class ThemeIcon(
    @DrawableRes val lightTheme: Int,
    @DrawableRes val darkTheme: Int,
)

object AppIcons{
    val UNDO = ThemeIcon(
        lightTheme = R.drawable.undo_black,
        darkTheme = R.drawable.undo_white,
    )
    val LIST = ThemeIcon(
        lightTheme = R.drawable.list,
        darkTheme = R.drawable.list,
    )
    val CLOSE = ThemeIcon(
        lightTheme = R.drawable.close,
        darkTheme = R.drawable.close,
    )
}

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

@Composable
fun AppIcon(
    themedIcon: ThemeIcon,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
){
    val resourceId = if (isSystemInDarkTheme()){
            themedIcon.darkTheme
    } else {
            themedIcon.lightTheme
    }
    Image(
        painter = painterResource(id = resourceId),
        contentDescription = contentDescription,
        modifier = modifier
    )
}


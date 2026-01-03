package com.example.nexusforge.backend

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nexusforge.frontend.EulaScreen
import com.example.nexusforge.frontend.RegPageScreen

@OptIn(ExperimentalAnimationApi::class) // если нужны кастомные анимации
@Composable
fun MyAppNav3() {
    // Правильный вызов: без <Destination> и без initialKey=
    val backStack = rememberNavBackStack(Destination.RegPage) // или rememberNavBackStack(RegPage, OtherScreen) для нескольких

    // BackHandler: используем entries.size
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull() // безопаснее, чем removeLast()
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {
            entry<Destination.RegPage> {
                RegPageScreen(
                    onNavigateToEula = { backStack += Destination.EulaPage } // оператор += работает
                )
            }

            entry<Destination.EulaPage> {
                EulaScreen(
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            // Добавьте остальные entry<...> здесь
        }
    )
}
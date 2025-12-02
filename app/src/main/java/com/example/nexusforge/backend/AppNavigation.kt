package com.example.nexusforge.backend

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
import com.example.nexusforge.frontend.EulaScreen
import com.example.nexusforge.frontend.RegPageScreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyApp() {
    // 1. Создание типобезопасного стека
    val backStack = remember { mutableStateListOf<Destination>(Destination.RegPage) }

    // 2. Функции навигации
    val navigateTo: (Destination) -> Unit = { destination ->
        backStack.add(destination)
    }
    val navigateBack: () -> Unit = {
        if (backStack.size > 1) {
            backStack.removeLast()
        }
    }

    // 3. Анимированное отображение
    AnimatedContent(
        targetState = backStack.lastOrNull() to backStack.size, // Используем пару (экран, размер стека)
        label = "navigation",
        transitionSpec = {
            // Анимация "вперед", если размер стека увеличился
            val forward = targetState.second > initialState.second

            val enterTransition = if (forward) {
                slideInHorizontally { width -> width } + fadeIn()
            } else {
                slideInHorizontally { width -> -width } + fadeIn()
            }

            val exitTransition = if (forward) {
                slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideOutHorizontally { width -> width } + fadeOut()
            }

            enterTransition togetherWith exitTransition using SizeTransform(clip = false)
        }
    ) { (screen, _) ->
        // 4. Отображение (Логика выбора экрана)
        // Если экран null, ничего не делаем
        if (screen != null) {
            when (screen) {
                is Destination.RegPage -> {
                    RegPageScreen(
                        onNavigateToEula = {
                            navigateTo(Destination.EulaPage)
                        }
                    )
                }

                is Destination.EulaPage -> {
                    EulaScreen(
                        onNavigateBack = navigateBack
                    )
                }
            }
        }
    }
}

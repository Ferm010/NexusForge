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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nexusforge.R
import com.example.nexusforge.frontend.AuthPasswordPage
import com.example.nexusforge.frontend.EulaScreen
import com.example.nexusforge.frontend.MainMenuPage
import com.example.nexusforge.frontend.PasswordPage
import com.example.nexusforge.frontend.ProfilePage
import com.example.nexusforge.frontend.RegNamePage
import com.example.nexusforge.frontend.RegPageScreen
import com.example.nexusforge.frontend.SearchPage
import com.example.nexusforge.viewmodels.RegViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalAnimationApi::class) // если нужны кастомные анимации
@Composable
fun MyAppNav3() {
    val vm: RegViewModel = viewModel()
    val startDestination: Destination =
        if (FirebaseAuth.getInstance().currentUser != null) Destination.MainMenu
        else Destination.RegPage
    val backStack = rememberNavBackStack(startDestination)

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider {

            entry<Destination.RegPage> {
                RegPageScreen(
                    vm = vm,
                    onNavigateToEula = { backStack += Destination.EulaPage },
                    onNavigateToAuthPassword = { backStack += Destination.AuthPassPage },
                    onNavigateToMainMenu = { backStack += Destination.MainMenu }
                )
            }

            entry<Destination.EulaPage> {
                EulaScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onAcceptEula = {
                        if (vm.isGoogleFlow) {
                            backStack.clear()
                            backStack += Destination.MainMenu
                        } else {
                            backStack += Destination.RegPassPage
                        }
                    }
                )
            }

            entry<Destination.RegPassPage> {
                PasswordPage(
                    vm = vm,
                    onNavigateToRegName = { backStack += Destination.NameRegPage }
                )
            }

            entry<Destination.NameRegPage> {
                RegNamePage(
                    vm = vm,
                    onNavigateToMainMenu = {
                        backStack.clear()
                        backStack += Destination.MainMenu
                    }
                )
            }

            entry<Destination.AuthPassPage> {
                AuthPasswordPage(
                    vm = vm,
                    onNavigateToMainMenu = {
                        backStack.clear()
                        backStack += Destination.MainMenu
                    }
                )
            }
            entry<Destination.MainMenu> {
                // 1. Создаем локальный backStack специально для вкладок
                val tabBackStack = rememberNavBackStack(Destination.MainMenu)

                // 2. Scaffold — это каркас, который держит нижнюю панель
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val currentTab = tabBackStack.lastOrNull()

                            // Кнопка: Главная
                            NavigationBarItem(
                                selected = currentTab is Destination.MainMenu,
                                onClick = {
                                    if (currentTab !is Destination.MainMenu) {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    }
                                },
                                icon = { Icon(painter = painterResource(R.drawable.search_white,), "Search") },
                                label = { Text("Главная") }
                            )

                            // Кнопка: Поиск
                            NavigationBarItem(
                                selected = currentTab is Destination.SearchPage,
                                onClick = {
                                    if (currentTab !is Destination.SearchPage) {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.SearchPage
                                    }
                                },
                                icon = { Icon(painter = painterResource(R.drawable.search_white,), "Search") },
                                label = { Text("Поиск") }
                            )

                            // Кнопка: Профиль
                            NavigationBarItem(
                                selected = currentTab is Destination.ProfilePage,
                                onClick = {
                                    if (currentTab !is Destination.ProfilePage) {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.ProfilePage
                                    }
                                },
                                icon = { Icon(painter = painterResource(R.drawable.search_white,), "Search") },
                                label = { Text("Профиль") }
                            )
                        }
                    }
                ) { innerPadding ->
                    // 3. Вложенный NavDisplay для отображения контента вкладок
                    NavDisplay(
                        modifier = Modifier.padding(innerPadding),
                        backStack = tabBackStack,
                        entryProvider = entryProvider {
                            entry<Destination.MainMenu> {
                                // Вызываем ваш существующий MainMenuPage
                                MainMenuPage(
                                    vm = vm,
                                    onSignOut = {
                                        vm.signOut()
                                        backStack.clear() // Обращаемся к внешнему backStack
                                        backStack += Destination.RegPage
                                    }
                                )
                            }
                            entry<Destination.SearchPage> {
                                SearchPage() // Создайте этот компонент
                            }
                            entry<Destination.ProfilePage> {
                                ProfilePage() // Создайте этот компонент
                            }
                        }
                    )
                }
            }
        }
    )
}
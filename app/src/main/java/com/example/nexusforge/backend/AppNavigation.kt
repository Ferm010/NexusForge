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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nexusforge.R
import com.example.nexusforge.frontend.AuthPasswordPage
import com.example.nexusforge.frontend.CreateAlertDialog
import com.example.nexusforge.frontend.EulaScreen
import com.example.nexusforge.frontend.mainmenu.MainMenuPage
import com.example.nexusforge.frontend.PasswordPage
import com.example.nexusforge.frontend.ProfilePage
import com.example.nexusforge.frontend.RegNamePage
import com.example.nexusforge.frontend.RegPageScreen
import com.example.nexusforge.frontend.SearchPage
import com.example.nexusforge.frontend.SettingPage
import com.example.nexusforge.frontend.TechnicalPage
import com.example.nexusforge.frontend.favoritePage
import com.example.nexusforge.viewmodels.RegViewModel
import com.example.nexusforge.viewmodels.ThemeViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalAnimationApi::class) // Анимации
@Composable
fun MyAppNav3(themeViewModel: ThemeViewModel) {
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
                var showCreateAlert by remember { mutableStateOf(false) }

                if (showCreateAlert) {
                    CreateAlertDialog(
                        onDismiss = { showCreateAlert = false },
                        onCreateModpack = {
                            // Навигация на создание модпака
                            // tabBackStack += Destination.CreateModpackPage
                        },
                        onCreateTemplate = {
                            // Навигация на создание шаблона
                            // tabBackStack += Destination.CreateTemplatePage
                        }
                    )
                }

                // 2. Scaffold — это каркас, который держит нижнюю панель
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val currentTab = tabBackStack.lastOrNull()

                            // Кнопка: Поиск
                            NavigationBarItem(
                                selected = currentTab is Destination.MainMenu,
                                onClick = {
                                    if (currentTab !is Destination.MainMenu) {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    }
                                },
                                icon = { Icon(painter = painterResource(R.drawable.search_white,), "Search") },
                                label = { Text("Поиск") }
                            )

                            // Кнопка: Избранное
                            NavigationBarItem(
                                selected = currentTab is Destination.FavoritePage,
                                onClick = {
                                    if (currentTab !is Destination.FavoritePage) {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.FavoritePage
                                    }
                                },
                                icon = { Icon(painter = painterResource(R.drawable.bookmark,), "Search") },
                                label = { Text("Избранное") }
                            )

                            // Кнопка: создать
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    showCreateAlert = true
                                },
                                icon = { Icon(painter = painterResource(R.drawable.add,), "Search") },
                                label = { Text("Создать") }
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
                                icon = { Icon(painter = painterResource(R.drawable.person,), "Search") },
                                label = { Text("Профиль") }
                            )
                            // Кнопка: Настройки
                            NavigationBarItem(
                                selected = currentTab is Destination.SettingsPage,
                                onClick = {
                                    if (currentTab !is Destination.SettingsPage) {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.SettingsPage
                                    }
                                },
                                icon = { Icon(painter = painterResource(R.drawable.settings,), "Search") },
                                label = { Text("Настройки") }
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
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    },
                                    onCreateModpack = {
                                        // TODO: Навигация на создание модпака
                                        // tabBackStack += Destination.CreateModpackPage
                                    },
                                    onCreateTemplate = {
                                        // TODO: Навигация на создание шаблона
                                        // tabBackStack += Destination.CreateTemplatePage
                                    },
                                    onProjectClick = { projectId ->
                                        tabBackStack += Destination.ProjectDetailsPage(projectId)
                                    }
                                )
                            }
                            entry<Destination.FavoritePage> {
                                favoritePage(
                                    vm = vm,
                                    onBackClick = {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    },
                                    onProjectClick = { projectId ->
                                        tabBackStack += Destination.ProjectDetailsPage(projectId)
                                    }
                                )
                            }
                            entry<Destination.ProfilePage> {
                                ProfilePage()
                            }
                            entry<Destination.SettingsPage> {
                                SettingPage(
                                    themeViewModel = themeViewModel,
                                    onBackClick = {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    },
                                    onTechnicalSupportClick = {
                                        tabBackStack += Destination.TechnicalPage
                                    }
                                )
                            }
                            entry<Destination.TechnicalPage> {
                                TechnicalPage(
                                    vm = vm,
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    }
                                )
                            }
                            entry<Destination.ProjectDetailsPage> {
                                val projectId = it.projectId
                                val projectDetailsViewModel: com.example.nexusforge.viewmodels.ProjectDetailsViewModel = viewModel()
                                
                                androidx.compose.runtime.LaunchedEffect(projectId) {
                                    projectDetailsViewModel.loadProject(projectId)
                                }
                                
                                when {
                                    projectDetailsViewModel.isLoading -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }
                                    projectDetailsViewModel.errorMessage != null -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = projectDetailsViewModel.errorMessage ?: "Ошибка",
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Button(onClick = { 
                                                    projectDetailsViewModel.loadProject(projectId)
                                                }) {
                                                    Text("Повторить")
                                                }
                                            }
                                        }
                                    }
                                     projectDetailsViewModel.project != null -> {
                                        val favoritesViewModel: com.example.nexusforge.viewmodels.FavoritesViewModel = viewModel()
                                        val favoriteProjects by favoritesViewModel.favoriteProjects.collectAsState()
                                        
                                        com.example.nexusforge.frontend.projectdetails.ProjectDetailsPage(
                                            project = projectDetailsViewModel.project!!,
                                            isFavorite = favoriteProjects.any { it.projectId == projectDetailsViewModel.project!!.projectId },
                                            onToggleFavorite = {
                                                favoritesViewModel.toggleFavorite(projectDetailsViewModel.project!!)
                                            },
                                            onBackClick = {
                                                tabBackStack.removeLastOrNull()
                                            },
                                            onOpenWebPage = {
                                                // TODO: Открыть веб-страницу проекта
                                            },
                                            onDownload = {
                                                // TODO: Скачать проект
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    )
}
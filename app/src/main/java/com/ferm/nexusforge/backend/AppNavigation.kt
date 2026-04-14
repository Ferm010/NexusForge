package com.ferm.nexusforge.backend

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
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.ferm.nexusforge.R
import com.ferm.nexusforge.frontend.AuthPasswordPage
import com.ferm.nexusforge.frontend.CreateAlertDialog
import com.ferm.nexusforge.frontend.CreateModpackPage
import com.ferm.nexusforge.frontend.CreateTemplatePage
import com.ferm.nexusforge.frontend.TemplatesListPage
import com.ferm.nexusforge.frontend.GenerateModpackPage
import com.ferm.nexusforge.frontend.SelectGenerationMethodPage
import com.ferm.nexusforge.frontend.EulaScreen
import com.ferm.nexusforge.frontend.mainmenu.MainMenuPage
import com.ferm.nexusforge.frontend.PasswordPage
import com.ferm.nexusforge.frontend.ProfilePage
import com.ferm.nexusforge.frontend.RegNamePage
import com.ferm.nexusforge.frontend.RegPageScreen
import com.ferm.nexusforge.frontend.SettingPage
import com.ferm.nexusforge.frontend.TechnicalPage
import com.ferm.nexusforge.frontend.LanguagePage
import com.ferm.nexusforge.frontend.ModpackEditorPage
import com.ferm.nexusforge.frontend.favoritePage
import com.ferm.nexusforge.viewmodels.RegViewModel
import com.ferm.nexusforge.viewmodels.ThemeViewModel
import com.ferm.nexusforge.viewmodels.LanguageViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Map.entry

@OptIn(ExperimentalAnimationApi::class) // Анимации
@Composable
fun MyAppNav3(themeViewModel: ThemeViewModel) {
    val vm: RegViewModel = viewModel()
    val languageViewModel: LanguageViewModel = viewModel()
    val context = LocalContext.current
    languageViewModel.initLanguage(context)
    
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
                    languageViewModel = languageViewModel,
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
                    languageViewModel = languageViewModel,
                    onNavigateToRegName = { backStack += Destination.NameRegPage }
                )
            }

            entry<Destination.NameRegPage> {
                RegNamePage(
                    vm = vm,
                    languageViewModel = languageViewModel,
                    onNavigateToMainMenu = {
                        backStack.clear()
                        backStack += Destination.MainMenu
                    }
                )
            }

            entry<Destination.AuthPassPage> {
                AuthPasswordPage(
                    vm = vm,
                    languageViewModel = languageViewModel,
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
                            showCreateAlert = false
                            tabBackStack += Destination.CreateModpackPage
                        },
                        onCreateTemplate = {
                            showCreateAlert = false
                            tabBackStack += Destination.CreateTemplatePage
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
                                label = { Text(text = stringResource(R.string.search)) }
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
                                label = { Text(text = stringResource(R.string.favorites))}
                            )

                            // Кнопка: создать
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    showCreateAlert = true
                                },
                                icon = { Icon(painter = painterResource(R.drawable.add,), "Search") },
                                label = { Text(text = stringResource(R.string.create)) }
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
                                label = { Text(text = stringResource(R.string.profile)) }
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
                                label = { Text(text = stringResource(R.string.settings)) }
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
                                        tabBackStack += Destination.CreateModpackPage
                                    },
                                    onCreateTemplate = {
                                        tabBackStack += Destination.CreateTemplatePage
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
                                    },
                                    onModpackClick = { modpackId ->
                                        tabBackStack += Destination.ModpackEditorPage(modpackId)
                                    },
                                    onTemplatesClick = {
                                        tabBackStack += Destination.TemplatesListPage
                                    },
                                    onEditTemplate = { templateId ->
                                        tabBackStack += Destination.EditTemplatePage(templateId)
                                    },
                                    onCreateTemplate = {
                                        tabBackStack += Destination.CreateTemplatePage
                                    }
                                )
                            }
                            entry<Destination.ProfilePage> {
                                ProfilePage(
                                    onBackClick = {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    },
                                    onSignOut = {
                                        vm.signOut()
                                        backStack.clear()
                                        backStack += Destination.RegPage
                                    },
                                    vm = vm
                                )
                            }
                            entry<Destination.SettingsPage> {
                                SettingPage(
                                    themeViewModel = themeViewModel,
                                    languageViewModel = languageViewModel,
                                    onBackClick = {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    },
                                    onTechnicalSupportClick = {
                                        tabBackStack += Destination.TechnicalPage
                                    },
                                    onLanguageClick = {
                                        tabBackStack += Destination.LanguagePage
                                    }
                                )
                            }
                            entry<Destination.TechnicalPage> {
                                TechnicalPage(
                                    vm = vm,
                                    languageViewModel = languageViewModel,
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    }
                                )
                            }
                            entry<Destination.LanguagePage> {
                                LanguagePage(
                                    vm = vm,
                                    languageViewModel = languageViewModel,
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onProfileClick = {
                                        tabBackStack += Destination.ProfilePage
                                    }
                                )
                            }
                            entry<Destination.CreateModpackPage> {
                                CreateModpackPage(
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onModpackCreated = { modpackId ->
                                        tabBackStack += Destination.SelectGenerationMethodPage
                                    }
                                )
                            }
                            entry<Destination.SelectGenerationMethodPage> {
                                SelectGenerationMethodPage(
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onMethodSelected = { method ->
                                        val methodString = when(method) {
                                            com.ferm.nexusforge.frontend.GenerationMethod.LOCAL -> "local"
                                            com.ferm.nexusforge.frontend.GenerationMethod.MRPACK -> "mrpack"
                                            com.ferm.nexusforge.frontend.GenerationMethod.GOOGLE_DRIVE -> "google_drive"
                                        }
                                        tabBackStack += Destination.GenerateModpackPage(methodString)
                                    }
                                )
                            }
                            entry<Destination.GenerateModpackPage> {
                                val method = it.method
                                GenerateModpackPage(
                                    method = method,
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onComplete = {
                                        tabBackStack.clear()
                                        tabBackStack += Destination.MainMenu
                                    }
                                )
                            }
                            entry<Destination.TemplatesListPage> {
                                TemplatesListPage(
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onCreateTemplate = {
                                        tabBackStack += Destination.CreateTemplatePage
                                    },
                                    onEditTemplate = { templateId ->
                                        tabBackStack += Destination.EditTemplatePage(templateId)
                                    }
                                )
                            }
                            entry<Destination.CreateTemplatePage> {
                                CreateTemplatePage(
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    }
                                )
                            }
                            entry<Destination.EditTemplatePage> {
                                val templateId = it.templateId
                                CreateTemplatePage(
                                    templateId = templateId,
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    }
                                )
                            }
                            entry<Destination.ModpackEditorPage> {
                                val modpackId = it.modpackId
                                ModpackEditorPage(
                                    modpackId = modpackId,
                                    onBackClick = {
                                        tabBackStack.removeLastOrNull()
                                    },
                                    onGenerateClick = {
                                        tabBackStack.removeLastOrNull()
                                        tabBackStack += Destination.SelectGenerationMethodPage
                                    }
                                )
                            }
                            entry<Destination.ProjectDetailsPage> {
                                val projectId = it.projectId
                                val projectDetailsViewModel: com.ferm.nexusforge.viewmodels.ProjectDetailsViewModel = viewModel()
                                
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
                                                    text = projectDetailsViewModel.errorMessage ?: stringResource(R.string.error_generic),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Button(onClick = { 
                                                    projectDetailsViewModel.loadProject(projectId)
                                                }) {
                                                    Text(text = stringResource(R.string.retry))
                                                }
                                            }
                                        }
                                    }
                                     projectDetailsViewModel.project != null -> {
                                        val favoritesViewModel: com.ferm.nexusforge.viewmodels.FavoritesViewModel = viewModel()
                                        val favoriteProjects by favoritesViewModel.favoriteProjects.collectAsState()
                                        
                                        com.ferm.nexusforge.frontend.projectdetails.ProjectDetailsPage(
                                            project = projectDetailsViewModel.project!!,
                                            isFavorite = favoriteProjects.any { it.projectId == projectDetailsViewModel.project!!.projectId },
                                            onToggleFavorite = {
                                                favoritesViewModel.toggleFavorite(projectDetailsViewModel.project!!)
                                            },
                                            onBackClick = {
                                                tabBackStack.removeLastOrNull()
                                            },
                                            onOpenWebPage = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    data = android.net.Uri.parse("https://modrinth.com/mod/${projectDetailsViewModel.project!!.projectId}")
                                                }
                                                context.startActivity(intent)
                                            },
                                            onDownload = {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    data = android.net.Uri.parse("https://modrinth.com/mod/${projectDetailsViewModel.project!!.projectId}/versions")
                                                }
                                                context.startActivity(intent)
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
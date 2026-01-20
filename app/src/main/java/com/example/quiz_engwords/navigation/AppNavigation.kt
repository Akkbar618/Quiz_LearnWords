package com.example.quiz_engwords.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quiz_engwords.data.repository.WordRepository
import com.example.quiz_engwords.presentation.dictionary.DictionaryScreen
import com.example.quiz_engwords.presentation.home.HomeScreen
import com.example.quiz_engwords.presentation.quiz.QuizScreen
import com.example.quiz_engwords.presentation.settings.SettingsScreen

/**
 * Главная навигация приложения с Bottom Navigation Bar.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    repository: WordRepository,
    onFinish: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartQuiz = {
                        navController.navigate(Screen.Quiz.route)
                    },
                    repository = repository
                )
            }
            
            composable(Screen.Quiz.route) {
                QuizScreen(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
                        com.example.quiz_engwords.presentation.quiz.QuizViewModel(repository)
                    },
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Dictionary.route) {
                DictionaryScreen(
                    repository = repository
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    repository = repository,
                    onExit = onFinish
                )
            }
        }
    }
}

/**
 * Bottom Navigation Bar с иконками.
 */
@Composable
private fun BottomNavigationBar(
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Не показываем Bottom Bar на экране Quiz
    if (currentDestination?.route == Screen.Quiz.route) {
        return
    }
    
    NavigationBar {
        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == screen.route
            } == true
            
            val (icon, label) = when (screen) {
                Screen.Home -> {
                    val icon = if (selected) Icons.Filled.Home else Icons.Outlined.Home
                    icon to "Home"
                }
                Screen.Dictionary -> {
                    val icon = if (selected) Icons.Filled.Book else Icons.Outlined.Book
                    icon to "Dictionary"
                }
                Screen.Settings -> {
                    val icon = if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
                    icon to "Settings"
                }
                else -> Icons.Outlined.Home to "Unknown"
            }
            
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies
                        launchSingleTop = true
                        // Restore state
                        restoreState = true
                    }
                }
            )
        }
    }
}

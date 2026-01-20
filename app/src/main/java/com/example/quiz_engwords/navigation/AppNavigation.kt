package com.example.quiz_engwords.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
 * Главная навигация приложения с Premium Bottom Navigation Bar.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    repository: WordRepository,
    onFinish: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + 
                slideInHorizontally(
                    initialOffsetX = { 30 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) +
                slideOutHorizontally(
                    targetOffsetX = { 30 },
                    animationSpec = tween(300)
                )
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartQuiz = {
                        navController.navigate(Screen.Quiz.route)
                    },
                    repository = repository
                )
            }
            
            composable(
                Screen.Quiz.route,
                enterTransition = {
                    fadeIn(animationSpec = tween(400)) +
                    slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(400)
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) +
                    slideOutVertically(
                        targetOffsetY = { it / 4 },
                        animationSpec = tween(300)
                    )
                }
            ) {
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
 * Premium Bottom Navigation Bar с анимациями.
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
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == screen.route
            } == true
            
            val (selectedIcon, unselectedIcon, label) = when (screen) {
                Screen.Home -> Triple(Icons.Filled.Home, Icons.Outlined.Home, "Главная")
                Screen.Dictionary -> Triple(Icons.Filled.Book, Icons.Outlined.Book, "Словарь")
                Screen.Settings -> Triple(Icons.Filled.Settings, Icons.Outlined.Settings, "Настройки")
                else -> Triple(Icons.Outlined.Home, Icons.Outlined.Home, "Unknown")
            }
            
            // Анимация масштаба иконки
            val scale by animateFloatAsState(
                targetValue = if (selected) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "icon_scale"
            )
            
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = if (selected) selectedIcon else unselectedIcon, 
                        contentDescription = label,
                        modifier = Modifier
                            .size(26.dp)
                            .scale(scale)
                    )
                },
                label = { 
                    Text(
                        text = label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

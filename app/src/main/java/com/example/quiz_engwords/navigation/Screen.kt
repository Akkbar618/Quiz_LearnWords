package com.example.quiz_engwords.navigation

/**
 * Sealed class для определения маршрутов навигации.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object Dictionary : Screen("dictionary")
    object Settings : Screen("settings")
}

/**
 * Список экранов для Bottom Navigation.
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Dictionary,
    Screen.Settings
)

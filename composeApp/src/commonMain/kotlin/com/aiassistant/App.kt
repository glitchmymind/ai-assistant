package com.aiassistant

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aiassistant.common.navigation.HomeDestination
import com.aiassistant.common.uikit.theme.AiAssistantTheme
import com.aiassistant.features.home.presentation.HomeScreen

@Composable
fun App() {
    AiAssistantTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = HomeDestination,
        ) {
            composable<HomeDestination> {
                HomeScreen()
            }
        }
    }
}

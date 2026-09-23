package com.example.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object NavRoutes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val APPS = "apps"
    const val PERMISSIONS = "permissions"
    const val ABOUT = "about"
}

@Composable
fun DynamicIslandNavigation(
    navController: NavHostController = rememberNavController(),
    initialRoute: String = NavRoutes.MAIN
) {
    NavHost(
        navController = navController,
        startDestination = initialRoute
    ) {
        composable(NavRoutes.MAIN) {
            MainScreen(
                onNavigateToSettings = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToApps = { navController.navigate(NavRoutes.APPS) },
                onNavigateToPermissions = { navController.navigate(NavRoutes.PERMISSIONS) },
                onNavigateToAbout = { navController.navigate(NavRoutes.ABOUT) }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.APPS) {
            AppsFilterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.PERMISSIONS) {
            PermissionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

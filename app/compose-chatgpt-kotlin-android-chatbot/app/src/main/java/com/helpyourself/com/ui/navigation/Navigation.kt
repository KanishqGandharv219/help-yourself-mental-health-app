package com.helpyourself.com.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.helpyourself.com.ui.screens.TherapyNearMeScreen

object Routes {
    const val THERAPY_NEAR_ME = "therapy_near_me"
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.THERAPY_NEAR_ME) {
            TherapyNearMeScreen(navController = navController)
        }
        // Add routes here as needed
    }
}

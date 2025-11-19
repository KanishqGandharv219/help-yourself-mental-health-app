package com.helpyourself.com.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import android.util.Log
import com.helpyourself.com.R
import com.helpyourself.com.ui.conversations.ConversationScreen
import com.helpyourself.com.ui.resources.ResourcesScreen
import com.helpyourself.com.ui.settings.SettingsScreen
import com.helpyourself.com.ui.screens.MentalHealthResourcesScreen
import com.helpyourself.com.ui.screens.MentalHealthAnalysisScreen
import com.helpyourself.com.ui.screens.BreathingExerciseScreen
import com.helpyourself.com.ui.screens.TherapyNearMeScreen
import com.helpyourself.com.ui.screens.InquiryScreen
import com.helpyourself.com.ui.screens.InquiryDetailsScreen
import com.helpyourself.com.ui.screens.DepressionTestScreen
import com.helpyourself.com.ui.screens.AnxietyTestScreen
import com.helpyourself.com.ui.screens.StressTestScreen
import com.helpyourself.com.ui.screens.DatabaseTestScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    openDrawer: () -> Unit,
    onToggleTheme: () -> Unit
) {
    var isDark by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (navController.currentDestination?.route) {
                            AppRoute.Conversation.route -> "Chat"
                            "${AppRoute.Conversation.route}/{chatId}" -> "Chat"
                            AppRoute.AcademicResources.route -> "Academic Resources"
                            AppRoute.MentalHealthResources.route -> "Mental Health Resources"
                            AppRoute.MentalHealthAnalysis.route -> "Mental Health Analysis"
                            AppRoute.BreathingExercise.route -> "Breathing Exercise"
                            AppRoute.Settings.route -> "Settings"
                            AppRoute.TherapyNearMe.route -> "Therapy Near Me"
                            AppRoute.Inquiry.route -> "Inquiry"
                            AppRoute.AnxietyTest.route -> "Anxiety Assessment"
                            AppRoute.StressTest.route -> "Stress Scale"
                            AppRoute.DatabaseTest.route -> "Database Connection Test"
                            AppRoute.About.route -> "About"
                            else -> "Help Yourself"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isDark = !isDark
                        onToggleTheme()
                    }) {
                        Icon(
                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDark) "Switch to Light Mode" else "Switch to Dark Mode"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Conversation.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Basic conversation route (no parameters)
            composable(AppRoute.Conversation.route) {
                Log.d("AppNavigation", "Navigating to basic conversation screen")
                ConversationScreen()
            }
            
            // Parameterized conversation route
            composable(
                route = "${AppRoute.Conversation.route}/{chatId}",
                arguments = listOf(
                    navArgument("chatId") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                Log.d("AppNavigation", "Destination composable received chatId=$chatId")
                ConversationScreen(chatId = chatId)
            }
            
            composable(AppRoute.AcademicResources.route) {
                ResourcesScreen()
            }
            
            composable(AppRoute.MentalHealthResources.route) {
                MentalHealthResourcesScreen(navController = navController)
            }
            
            composable(AppRoute.Settings.route) {
                SettingsScreen()
            }
            
            composable(AppRoute.MentalHealthAnalysis.route) {
                MentalHealthAnalysisScreen(navController)
            }
            
            composable(AppRoute.PersonalisedReview.route) {
                com.helpyourself.com.ui.screens.PersonalisedReviewScreen(navController)
            }
            
            composable(AppRoute.BreathingExercise.route) {
                BreathingExerciseScreen(navController)
            }
            
            composable(AppRoute.TherapyNearMe.route) {
                TherapyNearMeScreen(navController = navController)
            }

            composable(AppRoute.Inquiry.route) {
                InquiryScreen(navController = navController)
            }

            composable(AppRoute.InquiryDetails.route) {
                InquiryDetailsScreen(navController = navController)
            }

            composable(AppRoute.DepressionTest.route) {
                DepressionTestScreen(navController = navController)
            }

            composable(AppRoute.AnxietyTest.route) {
                AnxietyTestScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(AppRoute.StressTest.route) {
                StressTestScreen(onNavigateBack = { navController.popBackStack() })
            }

            composable(AppRoute.DatabaseTest.route) {
                DatabaseTestScreen()
            }

            composable(AppRoute.About.route) {
                com.helpyourself.com.ui.screens.AboutScreen()
            }
        }
    }
}
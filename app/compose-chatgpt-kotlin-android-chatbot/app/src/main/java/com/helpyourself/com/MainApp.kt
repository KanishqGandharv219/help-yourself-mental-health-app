package com.helpyourself.com

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.helpyourself.com.ui.common.AppBar
import com.helpyourself.com.ui.common.AppDrawer
import com.helpyourself.com.ui.common.AppRoute
import com.helpyourself.com.ui.conversations.ConversationScreen
import com.helpyourself.com.ui.screens.AcademicResourcesScreen
import com.helpyourself.com.ui.resources.ResourcesScreen
import com.helpyourself.com.ui.resources.GeneralResourcesScreen
import com.helpyourself.com.ui.settings.SettingsScreen
import com.helpyourself.com.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    var isDarkMode by remember { mutableStateOf(false) }
    AppTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: AppRoute.Conversation.route

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    currentRoute = currentRoute,
                    navigateToRoute = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    closeDrawer = { scope.launch { drawerState.close() } },
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    AppBar(
                        currentRoute = currentRoute,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.Conversation.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(AppRoute.Conversation.route) {
                        ConversationScreen()
                    }
                    composable(AppRoute.AcademicResources.route) {
                        AcademicResourcesScreen()
                    }
                    composable(AppRoute.Settings.route) {
                        SettingsScreen()
                    }
                }
            }
        }
    }
}
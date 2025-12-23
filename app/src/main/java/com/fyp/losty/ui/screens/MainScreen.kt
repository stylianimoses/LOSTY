package com.fyp.losty.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun MainScreen(appNavController: NavController) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Posts Feed") },
                    label = { Text("Feed") },
                    selected = currentDestination?.hierarchy?.any { it.route == "posts_feed" } == true,
                    onClick = { navController.navigate("posts_feed") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Email, contentDescription = "Messages") },
                    label = { Text("Messages") },
                    selected = currentDestination?.hierarchy?.any { it.route == "conversations" } == true,
                    onClick = { navController.navigate("conversations") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Manage Own Posts") },
                    label = { Text("My Posts") },
                    selected = currentDestination?.hierarchy?.any { it.route == "manage_own_posts" } == true,
                    onClick = { navController.navigate("manage_own_posts") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Manage Active Claims") },
                    label = { Text("Claims") },
                    selected = currentDestination?.hierarchy?.any { it.route == "manage_active_claims" } == true,
                    onClick = { navController.navigate("manage_active_claims") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                    onClick = { navController.navigate("profile") { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { appNavController.navigate("create_post") }) {
                Icon(Icons.Filled.Add, contentDescription = "Create Post")
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "posts_feed",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("posts_feed") { PostsFeedScreen(navController = navController) }
            composable("conversations") { ConversationsScreen(navController = navController) }
            composable("chat/{conversationId}") { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                ChatScreen(conversationId = conversationId, navController = navController)
            }
            composable("manage_own_posts") { ManageOwnPostsScreen(appNavController = appNavController) }
            composable("manage_active_claims") { ManageActiveClaimsScreen() }
            composable("manage_post_claims") { ManagePostClaimsScreen() }
            composable("profile") { ProfileScreen(appNavController = appNavController) }
        }
    }
}

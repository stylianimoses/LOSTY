package com.fyp.losty.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun MainScreen(appNavController: NavController) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                appNavController = appNavController
            )
        }
        composable("conversations") { ConversationsScreen(navController = navController) }

        // Chat route with optional query parameter 'otherUserName'
        composable(
            route = "chat/{conversationId}?otherUserName={otherUserName}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val otherNameArg = backStackEntry.arguments?.getString("otherUserName")?.takeIf { it.isNotBlank() }
            ChatScreen(conversationId = conversationId, navController = navController, otherUserNameArg = otherNameArg)
        }

        composable("my_activity") { MyActivityScreen(navController = navController, appNavController = appNavController) }
        composable("profile") { ProfileScreen(navController = navController, onSignOut = { appNavController.navigate("login") { popUpTo(0) } }) }
    }
}

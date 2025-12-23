package com.fyp.losty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fyp.losty.auth.LoginScreen
import com.fyp.losty.auth.RegisterScreen
import com.fyp.losty.ui.screens.CreatePostScreen
import com.fyp.losty.ui.screens.EditPostScreen
import com.fyp.losty.ui.screens.MainScreen
import com.fyp.losty.ui.theme.LOSTYTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LOSTYTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Check the current user's status when the app starts
    val startDestination = if (FirebaseAuth.getInstance().currentUser != null) "main" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") { LoginScreen(navController = navController) }
        composable("register") { RegisterScreen(navController = navController) }
        composable("main") { MainScreen(appNavController = navController) }
        composable("create_post") { CreatePostScreen(navController = navController) }
        composable("edit_post/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            EditPostScreen(postId = postId, navController = navController)
        }
    }
}
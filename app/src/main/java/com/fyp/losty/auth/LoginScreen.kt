package com.fyp.losty.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fyp.losty.AppViewModel
import com.fyp.losty.AuthState

@Composable
fun LoginScreen(navController: NavController, appViewModel: AppViewModel = viewModel()) {
    var credential by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by appViewModel.authState.collectAsState()
    val context = LocalContext.current

    // Listen for state changes
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Success -> {
                navController.navigate("main") { popUpTo(0) }
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    val isEmail = Patterns.EMAIL_ADDRESS.matcher(credential).matches()
    val isPhone = Patterns.PHONE.matcher(credential).matches() && credential.length >= 10
    val isCredentialValid = isEmail || isPhone
    val isPasswordValid = password.length >= 8

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = credential,
                onValueChange = { credential = it },
                label = { Text("Email or Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                isError = credential.isNotEmpty() && !isCredentialValid,
                supportingText = { if (credential.isNotEmpty() && !isCredentialValid) Text("Please enter a valid email or phone number") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = password.isNotEmpty() && !isPasswordValid,
                supportingText = { if (password.isNotEmpty() && !isPasswordValid) Text("Password must be at least 8 characters") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { appViewModel.loginUser(credential, password) },
                enabled = isCredentialValid && isPasswordValid && authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("register") }) {
                Text("Don't have an account? Register")
            }
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        }
    }
}
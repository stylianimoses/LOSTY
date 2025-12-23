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
fun RegisterScreen(navController: NavController, appViewModel: AppViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPhoneValid = Patterns.PHONE.matcher(phoneNumber).matches() && phoneNumber.length >= 10
    val isPasswordValid = password.length >= 8
    val doPasswordsMatch = password == confirmPassword

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = email.isNotEmpty() && !isEmailValid,
                supportingText = { if (email.isNotEmpty() && !isEmailValid) Text("Please enter a valid email address") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumber.isNotEmpty() && !isPhoneValid,
                supportingText = { if (phoneNumber.isNotEmpty() && !isPhoneValid) Text("Please enter a valid phone number") }
            )
            Spacer(modifier = Modifier.height(8.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                isError = confirmPassword.isNotEmpty() && !doPasswordsMatch,
                supportingText = { if (confirmPassword.isNotEmpty() && !doPasswordsMatch) Text("Passwords do not match") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { appViewModel.registerUser(email, password, phoneNumber) },
                enabled = isEmailValid && isPhoneValid && isPasswordValid && doPasswordsMatch && authState !is AuthState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an account? Login")
            }
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        }
    }
}
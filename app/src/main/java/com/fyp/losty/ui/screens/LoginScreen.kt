package com.fyp.losty.ui.screens

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(navController: NavController) {
    var credential by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isEmail = Patterns.EMAIL_ADDRESS.matcher(credential).matches()
    val isPhone = Patterns.PHONE.matcher(credential).matches()
    val isCredentialValid = isEmail || isPhone
    val isPasswordLongEnough = password.length >= 8

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = credential,
            onValueChange = { credential = it },
            label = { Text("Email or Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            isError = credential.isNotEmpty() && !isCredentialValid,
            supportingText = {
                if (credential.isNotEmpty() && !isCredentialValid) {
                    Text("Please enter a valid email or phone number")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = {passwordVisible = !passwordVisible}){
                    Icon(imageVector  = image, "toggle password visibility")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            isError = password.isNotEmpty() && !isPasswordLongEnough,
            supportingText = {
                if (password.isNotEmpty() && !isPasswordLongEnough) {
                    Text("Password must be at least 8 characters long")
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // TODO: Add authentication logic here
                navController.navigate("main")
            },
            enabled = isCredentialValid && isPasswordLongEnough,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { navController.navigate("register") }) {
            Text("Don't have an account? Register")
        }
    }
}
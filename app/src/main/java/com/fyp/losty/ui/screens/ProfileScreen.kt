package com.fyp.losty.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fyp.losty.AppViewModel

@Composable
fun ProfileScreen(
    appNavController: NavController,
    appViewModel: AppViewModel = viewModel()
) {
    val userProfile by appViewModel.userProfile.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Load the user profile as soon as the screen is displayed
    LaunchedEffect(Unit) {
        appViewModel.loadUserProfile()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri /* TODO: Upload this URI to Firebase Storage and update profile */ }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = selectedImageUri ?: userProfile.photoUrl.ifEmpty { "https://i.imgur.com/8A2nO7N.png" },
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            OutlinedTextField(
                value = userProfile.displayName,
                onValueChange = { /* TODO: Update ViewModel state */ },
                label = { Text("Display Name") },
                singleLine = true
            )
        } else {
            Text(text = userProfile.displayName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Text(text = userProfile.email, fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { isEditing = !isEditing }) {
            Text(if (isEditing) "Save Profile" else "Edit Profile")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { /* TODO: Navigate to Change Password screen */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Change Password")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO: Navigate to Blocked Users screen */ }, modifier = Modifier.fillMaxWidth()) {
            Text("Manage Blocked Users")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                appNavController.navigate("login") { popUpTo(0) }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Out")
        }
    }
}
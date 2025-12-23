package com.fyp.losty.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.fyp.losty.AppViewModel // Import the unified ViewModel
import com.fyp.losty.SinglePostState

@Composable
fun CreatePostScreen(navController: NavController, appViewModel: AppViewModel = viewModel()) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var postType by remember { mutableStateOf("LOST") } // "LOST" or "FOUND"

    val createPostState by appViewModel.createPostState.collectAsState()
    val context = LocalContext.current

    // Listen for state changes from the ViewModel
    LaunchedEffect(createPostState) {
        when (val state = createPostState) {
            is SinglePostState.Updated -> { // Listen for the successful update signal
                Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack() // Go back to the previous screen
            }
            is SinglePostState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit // Do nothing for Idle or Loading
        }
    }

    val isFormValid = title.isNotEmpty() && description.isNotEmpty()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
        onResult = { uris -> selectedImageUris = uris }
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Post Type Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = postType == "LOST",
                    onClick = { postType = "LOST" },
                    label = { Text("Lost") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = postType == "FOUND",
                    onClick = { postType = "FOUND" },
                    label = { Text("Found") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Photos")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(selectedImageUris) { uri ->
                    AsyncImage(model = uri, contentDescription = "Selected image", modifier = Modifier.size(100.dp), contentScale = ContentScale.Crop)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    appViewModel.createPost(title, description, category, location, selectedImageUris, postType)
                },
                enabled = isFormValid && createPostState !is SinglePostState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post")
            }
        }

        if (createPostState is SinglePostState.Loading) {
            CircularProgressIndicator()
        }
    }
}
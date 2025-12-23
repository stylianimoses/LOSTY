package com.fyp.losty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fyp.losty.AppViewModel // Import the unified ViewModel
import com.fyp.losty.Post
import com.fyp.losty.PostFeedState

@Composable
fun ManageOwnPostsScreen(appViewModel: AppViewModel = viewModel(), appNavController: NavController) {
    val myPostsState by appViewModel.myPostsState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Post?>(null) }

    // Load the user's posts as soon as the screen is displayed
    LaunchedEffect(Unit) {
        appViewModel.loadMyPosts()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = myPostsState) {
            is PostFeedState.Loading -> {
                CircularProgressIndicator()
            }
            is PostFeedState.Success -> {
                if (state.posts.isEmpty()) {
                    Text(text = "You haven't created any posts yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.posts) { post ->
                            MyPostItem(
                                post = post,
                                onDeleteClick = { showDeleteDialog = post },
                                onEditClick = { appNavController.navigate("edit_post/${post.id}") },
                                onViewClaimsClick = { appNavController.navigate("manage_post_claims") }
                            )
                        }
                    }
                }
            }
            is PostFeedState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }

    // --- Confirmation Dialog for Deleting a Post ---
    showDeleteDialog?.let { postToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Post") },
            text = { Text("Are you sure you want to permanently delete this post?") },
            confirmButton = {
                Button(
                    onClick = {
                        appViewModel.deletePost(postToDelete)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MyPostItem(post: Post, onDeleteClick: () -> Unit, onEditClick: () -> Unit, onViewClaimsClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.description, maxLines = 3)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onViewClaimsClick) {
                    Icon(Icons.Filled.Notifications, contentDescription = "View Claims")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Post")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Post")
                }
            }
        }
    }
}
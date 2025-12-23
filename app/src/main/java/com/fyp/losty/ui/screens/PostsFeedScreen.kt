package com.fyp.losty.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fyp.losty.AppViewModel
import com.fyp.losty.ClaimEvent
import com.fyp.losty.Post
import com.fyp.losty.PostFeedState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PostsFeedScreen(appViewModel: AppViewModel = viewModel(), navController: androidx.navigation.NavController? = null) {
    val postFeedState by appViewModel.postFeedState.collectAsState()
    val context = LocalContext.current

    // Listen for one-time claim events
    LaunchedEffect(key1 = Unit) {
        appViewModel.claimEvents.collectLatest {
            event ->
            when (event) {
                is ClaimEvent.Success -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is ClaimEvent.Error -> Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = postFeedState) {
            is PostFeedState.Loading -> {
                CircularProgressIndicator()
            }
            is PostFeedState.Success -> {
                if (state.posts.isEmpty()) {
                    Text(text = "No posts found. Be the first to create one!")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.posts) {
                            post ->
                            PostItem(post = post, appViewModel = appViewModel, navController = navController)
                        }
                    }
                }
            }
            is PostFeedState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}

@Composable
fun PostItem(post: Post, appViewModel: AppViewModel, navController: androidx.navigation.NavController? = null) {
    val userProfile by appViewModel.userProfile.collectAsState()
    val currentUserId = userProfile.uid

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            if (post.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = post.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        if (post.authorName.isNotEmpty()) {
                            Text(
                                text = "Posted by ${post.authorName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = post.description, maxLines = 3)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Only show Message button if it's not the current user's post
                    if (currentUserId != post.authorId && navController != null) {
                        OutlinedButton(
                            onClick = {
                                appViewModel.getOrCreateConversation(
                                    postId = post.id,
                                    postTitle = post.title,
                                    postImageUrl = post.imageUrls.firstOrNull() ?: "",
                                    postOwnerId = post.authorId,
                                    postOwnerName = post.authorName
                                ) { conversationId ->
                                    navController.navigate("chat/$conversationId")
                                }
                            }
                        ) {
                            Text("Message")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = { /* TODO: Handle report post */ }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Report Post")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { appViewModel.claimItem(post) }) {
                        Text("Claim")
                    }
                }
            }
        }
    }
}
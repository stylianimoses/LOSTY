package com.fyp.losty.ui.screens

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class CreatePostState {
    object Idle : CreatePostState()
    object Loading : CreatePostState()
    object Success : CreatePostState()
    data class Error(val message: String) : CreatePostState()
}

class CreatePostViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _createPostState = MutableStateFlow<CreatePostState>(CreatePostState.Idle)
    val createPostState = _createPostState.asStateFlow()

    fun createPost(
        title: String,
        description: String,
        category: String,
        location: String,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _createPostState.value = CreatePostState.Loading

            val userId = auth.currentUser?.uid
            if (userId == null) {
                _createPostState.value = CreatePostState.Error("You must be logged in to create a post.")
                return@launch
            }

            try {
                // 1. Upload images to Firebase Storage and get their download URLs
                val imageUrls = imageUris.map { uri ->
                    val imageRef = storage.reference.child("post_images/${UUID.randomUUID()}")
                    imageRef.putFile(uri).await()
                    imageRef.downloadUrl.await().toString()
                }

                // 2. Create the post object to save in Firestore
                val post = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "location" to location,
                    "imageUrls" to imageUrls, // Save the list of image URLs
                    "authorId" to userId,
                    "createdAt" to System.currentTimeMillis(),
                    "status" to "active" // Or "pending_approval" based on your logic
                )

                // 3. Save the post to Firestore
                firestore.collection("posts").add(post).await()

                _createPostState.value = CreatePostState.Success

            } catch (e: Exception) {
                _createPostState.value = CreatePostState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }
}
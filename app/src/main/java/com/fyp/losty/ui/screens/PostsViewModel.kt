package com.fyp.losty.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class Post(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val imageUrls: List<String> = emptyList(),
    val authorId: String = "",
    val createdAt: Long = 0L,
    val status: String = ""
)

sealed class PostFeedState {
    object Loading : PostFeedState()
    data class Success(val posts: List<Post>) : PostFeedState()
    data class Error(val message: String) : PostFeedState()
}

sealed class SinglePostState {
    object Loading : SinglePostState()
    data class Success(val post: Post) : SinglePostState()
    object Updated : SinglePostState()
    data class Error(val message: String) : SinglePostState()
}

// --- NEW: Data class to represent a Claim ---
data class Claim(
    val id: String = "",
    val postId: String = "",
    val postTitle: String = "",
    val postOwnerId: String = "",
    val claimerId: String = "",
    val status: String = "",
    val claimedAt: Long = 0L
)

// --- NEW: State for the list of user's claims ---
sealed class MyClaimsState {
    object Loading : MyClaimsState()
    data class Success(val claims: List<Claim>) : MyClaimsState()
    data class Error(val message: String) : MyClaimsState()
}

sealed class ClaimEvent {
    data class Success(val message: String) : ClaimEvent()
    data class Error(val message: String) : ClaimEvent()
}

class PostsViewModel : ViewModel() {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _postFeedState = MutableStateFlow<PostFeedState>(PostFeedState.Loading)
    val postFeedState = _postFeedState.asStateFlow()

    private val _myPostsState = MutableStateFlow<PostFeedState>(PostFeedState.Loading)
    val myPostsState = _myPostsState.asStateFlow()

    private val _postToEditState = MutableStateFlow<SinglePostState>(SinglePostState.Loading)
    val postToEditState = _postToEditState.asStateFlow()

    // --- NEW: State for the user's own claims ---
    private val _myClaimsState = MutableStateFlow<MyClaimsState>(MyClaimsState.Loading)
    val myClaimsState = _myClaimsState.asStateFlow()

    // Channel for one-time claim events
    private val _claimEventChannel = Channel<ClaimEvent>()
    val claimEvents = _claimEventChannel.receiveAsFlow()

    init {
        loadPosts()
        loadMyPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _postFeedState.value = PostFeedState.Loading
            try {
                val snapshot = firestore.collection("posts")
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val posts = snapshot.documents.map { doc ->
                    Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        location = doc.getString("location") ?: "",
                        imageUrls = (doc.get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        authorId = doc.getString("authorId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        status = doc.getString("status") ?: "active"
                    )
                }
                _postFeedState.value = PostFeedState.Success(posts)
            } catch (e: Exception) {
                _postFeedState.value = PostFeedState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun loadMyPosts() {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _myPostsState.value = PostFeedState.Error("You must be logged in")
                return@launch
            }

            _myPostsState.value = PostFeedState.Loading
            try {
                val snapshot = firestore.collection("posts")
                    .whereEqualTo("authorId", userId)
                    .get()
                    .await()

                val posts = snapshot.documents.map { doc ->
                    Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        location = doc.getString("location") ?: "",
                        imageUrls = (doc.get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        authorId = doc.getString("authorId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        status = doc.getString("status") ?: "active"
                    )
                }.sortedByDescending { it.createdAt } // Sort in memory instead
                _myPostsState.value = PostFeedState.Success(posts)
            } catch (e: Exception) {
                _myPostsState.value = PostFeedState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun getPost(postId: String) {
        viewModelScope.launch {
            _postToEditState.value = SinglePostState.Loading
            try {
                val doc = firestore.collection("posts").document(postId).get().await()
                if (doc.exists()) {
                    val post = Post(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = doc.getString("category") ?: "",
                        location = doc.getString("location") ?: "",
                        imageUrls = (doc.get("imageUrls") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        authorId = doc.getString("authorId") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        status = doc.getString("status") ?: "active"
                    )
                    _postToEditState.value = SinglePostState.Success(post)
                } else {
                    _postToEditState.value = SinglePostState.Error("Post not found")
                }
            } catch (e: Exception) {
                _postToEditState.value = SinglePostState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun updatePost(postId: String, title: String, description: String, category: String, location: String) {
        viewModelScope.launch {
            _postToEditState.value = SinglePostState.Loading
            try {
                val updates = hashMapOf<String, Any>(
                    "title" to title,
                    "description" to description,
                    "category" to category,
                    "location" to location
                )
                firestore.collection("posts").document(postId).update(updates).await()
                _postToEditState.value = SinglePostState.Updated
            } catch (e: Exception) {
                _postToEditState.value = SinglePostState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("posts").document(postId).delete().await()
                loadMyPosts() // Refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun claimItem(post: Post) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                _claimEventChannel.send(ClaimEvent.Error("You must be logged in to claim an item."))
                return@launch
            }

            if (userId == post.authorId) {
                _claimEventChannel.send(ClaimEvent.Error("You cannot claim your own item."))
                return@launch
            }

            try {
                val claim = hashMapOf(
                    "postId" to post.id,
                    "postTitle" to post.title,
                    "postOwnerId" to post.authorId,
                    "claimerId" to userId,
                    "status" to "pending",
                    "claimedAt" to System.currentTimeMillis()
                )

                firestore.collection("claims").add(claim).await()
                _claimEventChannel.send(ClaimEvent.Success("Item claimed successfully! Your claim is pending approval."))
            } catch (e: Exception) {
                _claimEventChannel.send(ClaimEvent.Error(e.message ?: "An unknown error occurred."))
            }
        }
    }

    // --- NEW: Function to load only the current user's claims ---
    fun loadMyClaims() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _myClaimsState.value = MyClaimsState.Error("You must be logged in to see your claims.")
            return
        }

        viewModelScope.launch {
            _myClaimsState.value = MyClaimsState.Loading
            try {
                val snapshot = firestore.collection("claims")
                    .whereEqualTo("claimerId", userId)
                    .orderBy("claimedAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val claims = snapshot.documents.map { doc ->
                    Claim(
                        id = doc.id,
                        postId = doc.getString("postId") ?: "",
                        postTitle = doc.getString("postTitle") ?: "",
                        postOwnerId = doc.getString("postOwnerId") ?: "",
                        claimerId = doc.getString("claimerId") ?: "",
                        status = doc.getString("status") ?: "",
                        claimedAt = doc.getLong("claimedAt") ?: 0L
                    )
                }
                _myClaimsState.value = MyClaimsState.Success(claims)
            } catch (e: Exception) {
                _myClaimsState.value = MyClaimsState.Error(e.message ?: "Failed to load your claims.")
            }
        }
    }
}
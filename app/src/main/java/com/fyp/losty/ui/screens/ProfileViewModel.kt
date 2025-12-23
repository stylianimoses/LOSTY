package com.fyp.losty.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = ""
)

class ProfileViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile = _userProfile.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser ?: return@launch

            try {
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()

                _userProfile.value = UserProfile(
                    // For display name, try the auth profile first, then fallback to a default
                    displayName = firebaseUser.displayName ?: "User", 
                    email = firebaseUser.email ?: "No email found",
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    // Get phone number from our Firestore document
                    phoneNumber = userDoc.getString("phoneNumber") ?: ""
                )
            } catch (e: Exception) {
                // Handle potential errors fetching from Firestore
                _userProfile.value = UserProfile(
                    displayName = "User",
                    email = firebaseUser.email ?: "Error loading profile",
                )
            }
        }
    }
}
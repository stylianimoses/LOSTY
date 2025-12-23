package com.fyp.losty.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fyp.losty.utils.FCMTokenManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Defines the possible states of our UI
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState() // Represents successful authentication
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow() // The UI will observe this

    fun registerUser(email: String, password: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Step 1: Create the user in Firebase Authentication
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw Exception("User creation failed")

                // Step 2: Set the user's display name in their Auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(email.substringBefore("@")) // Use the part of the email before the @
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                // Step 3: Save additional user details in Firestore
                val userMap = hashMapOf(
                    "displayName" to email.substringBefore("@"),
                    "email" to email,
                    "phoneNumber" to phoneNumber,
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(firebaseUser.uid).set(userMap).await()

                // Step 4: Save FCM token for push notifications
                try {
                    FCMTokenManager.saveUserFCMToken(firebaseUser.uid)
                } catch (e: Exception) {
                    // Log error but don't fail registration if FCM token save fails
                    println("Warning: Failed to save FCM token: ${e.message}")
                }

                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun loginUser(credential: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val isEmail = Patterns.EMAIL_ADDRESS.matcher(credential).matches()

                val emailToLogin = if (isEmail) {
                    credential
                } else {
                    // If it's not an email, it must be a phone number. Look it up in Firestore.
                    val usersQuery = firestore.collection("users")
                        .whereEqualTo("phoneNumber", credential)
                        .limit(1)
                        .get()
                        .await()

                    if (usersQuery.isEmpty) {
                        throw Exception("No account found with this phone number.")
                    }
                    usersQuery.documents.first().getString("email")
                        ?: throw Exception("Could not retrieve email for this phone number.")
                }

                val authResult = auth.signInWithEmailAndPassword(emailToLogin, password).await()
                val firebaseUser = authResult.user
                
                // Save FCM token for push notifications
                if (firebaseUser != null) {
                    try {
                        FCMTokenManager.saveUserFCMToken(firebaseUser.uid)
                    } catch (e: Exception) {
                        // Log error but don't fail login if FCM token save fails
                        println("Warning: Failed to save FCM token: ${e.message}")
                    }
                }
                
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
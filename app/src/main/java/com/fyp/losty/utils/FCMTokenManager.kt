package com.fyp.losty.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Utility class for managing FCM tokens
 * Call saveUserFCMToken() when the user logs in or when the token is refreshed
 */
object FCMTokenManager {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
    
    /**
     * Retrieves the device's FCM token and updates the corresponding user document
     * in the users collection with the fcmToken field.
     * 
     * @param userId The ID of the user (typically from FirebaseAuth.currentUser?.uid)
     */
    suspend fun saveUserFCMToken(userId: String) {
        try {
            // Get the FCM token for this device
            val token = messaging.token.await()
            
            // Update the user document in Firestore with the FCM token
            firestore.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            // Handle error - token retrieval or Firestore update failed
            throw Exception("Failed to save FCM token: ${e.message}", e)
        }
    }
    
    /**
     * Convenience function that uses the current authenticated user
     */
    suspend fun saveCurrentUserFCMToken() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            saveUserFCMToken(userId)
        } else {
            throw Exception("No authenticated user found")
        }
    }
}





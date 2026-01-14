package com.fyp.losty

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

// Added imports for App Check
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize App Check with the debug provider
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )

        // Log the debug token
        firebaseAppCheck.getAppCheckToken(false).addOnSuccessListener { token ->
            Log.d("AppCheckDebug", "App Check debug token: ${token.token}")
        }
    }
}

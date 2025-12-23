# Fuzzy Matching System Implementation Summary

This document summarizes the complete implementation of the Fuzzy Matching algorithm and FCM notification system for the Lost and Found app.

## ✅ Completed Components

### 1. Kotlin - FCM Token Management
**File**: `app/src/main/java/com/fyp/losty/utils/FCMTokenManager.kt`

- **Function**: `saveUserFCMToken(userId: String)`
  - Retrieves the device's FCM token
  - Updates the user document in Firestore with the `fcmToken` field
  - Integrated into `AuthViewModel` for automatic token saving on login/registration

**Integration**: 
- Added to `AuthViewModel.registerUser()` - saves token after registration
- Added to `AuthViewModel.loginUser()` - saves token after login

### 2. Node.js Cloud Functions - Main Trigger
**File**: `functions/index.js`

- **Function**: `onReportCreate`
  - Triggered when a new document is created in the `reports` collection
  - Queries opposite type reports (LOST ↔ FOUND) without existing matches
  - Calculates match scores using weighted algorithm
  - Links reports and sends notification if score ≥ 0.85

### 3. Node.js Helper Functions

#### `getDistanceInKm(lat1, lon1, lat2, lon2)`
- Implements Haversine Formula
- Returns distance in kilometers between two coordinates
- Used for Geographic Score calculation

#### `diceCoefficient(str1, str2)`
- Implements Dice's Coefficient algorithm
- Calculates string similarity between descriptions
- Returns normalized score (0-1)
- Used for Description Score calculation

#### `getColorScore(rgb1, rgb2)`
- Calculates Euclidean distance between RGB color arrays
- Normalizes to 0-1 similarity score
- Used for Color Score calculation

#### `getTimeScore(timestamp1, timestamp2)`
- Calculates time-based similarity
- Returns normalized score based on time difference
- Used for Time Score calculation

#### `getGeographicScore(lat1, lon1, lat2, lon2)`
- Wraps `getDistanceInKm()` with normalization
- Returns geographic similarity score (0-1)
- Used for Geographic Score calculation

#### `sendMatchNotification(ownerId, lostReportId, foundReportId)`
- Retrieves FCM token from user document
- Sends push notification using Firebase Admin SDK
- Notification title: "Possible Match Found!"
- Includes report IDs in notification data

## 📊 Scoring Algorithm

The Final Match Score is calculated as a weighted average:

```
Final Score = (Geographic × 0.35) + (Description × 0.35) + (Color × 0.25) + (Time × 0.05)
```

- **Geographic Score (35%)**: Based on Haversine distance
- **Description Score (35%)**: Based on Dice's Coefficient
- **Color Score (25%)**: Based on Euclidean distance on RGB arrays
- **Time Score (5%)**: Based on timestamp difference

**Threshold**: Match is confirmed if Final Score ≥ 0.85

## 🔧 Dependencies Added

### Android (Kotlin)
- `com.google.firebase:firebase-messaging` (via Firebase BoM)

### Node.js
- `firebase-admin`: ^12.0.0
- `firebase-functions`: ^4.5.0

## 📋 Firestore Schema

### Reports Collection (`reports/{reportId}`)
```javascript
{
  userId: string,              // ID of user who posted
  type: "LOST" | "FOUND",     // Report type
  itemName: string,
  primaryCategory: string,
  description: string,
  latitude: number,
  longitude: number,
  dominantColorRgb: [R, G, B], // Array of 3 numbers
  timestamp: number,            // Milliseconds (use serverTimestamp())
  matchId: string | null        // Initially null, set when matched
}
```

### Users Collection (`users/{userId}`)
```javascript
{
  fcmToken: string,  // Required for notifications
  // ... other fields
}
```

## 🚀 Deployment Steps

1. **Install Firebase CLI**: `npm install -g firebase-tools`
2. **Login**: `firebase login`
3. **Initialize Functions** (if needed): `firebase init functions`
4. **Install Dependencies**: `cd functions && npm install`
5. **Deploy**: `firebase deploy --only functions`

## 📝 Usage Notes

- FCM tokens are automatically saved when users log in or register
- The Cloud Function automatically triggers on new report creation
- Only the owner (user who posted the LOST report) receives notifications
- Reports are automatically linked by setting `matchId` field in both documents
- The function skips reports that already have a match

## 🔍 Testing

Test locally using Firebase Emulators:
```bash
firebase emulators:start
```

Create test reports in the Firestore emulator to trigger the matching algorithm.





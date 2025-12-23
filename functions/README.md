# Firebase Cloud Functions - Fuzzy Matching System

This directory contains the Firebase Cloud Functions that implement the fuzzy matching algorithm for the Lost and Found app.

## Setup Instructions

1. **Install Firebase CLI** (if not already installed):
   ```bash
   npm install -g firebase-tools
   ```

2. **Login to Firebase**:
   ```bash
   firebase login
   ```

3. **Initialize Firebase Functions** (if not already done):
   ```bash
   firebase init functions
   ```
   Select your Firebase project when prompted.

4. **Install Dependencies**:
   ```bash
   cd functions
   npm install
   ```

5. **Deploy Functions**:
   ```bash
   firebase deploy --only functions
   ```

## Function Overview

### `onReportCreate`
- **Trigger**: Firestore document creation in `reports` collection
- **Purpose**: Executes fuzzy matching algorithm when a new report is posted
- **Process**:
  1. Queries opposite type reports (LOST ↔ FOUND) without existing matches
  2. Calculates match scores using:
     - Geographic Score (35%): Haversine distance
     - Description Score (35%): Dice's Coefficient
     - Color Score (25%): Euclidean distance on RGB arrays
     - Time Score (5%): Timestamp difference
  3. If final score ≥ 0.85:
     - Links both reports by setting `matchId` field
     - Sends FCM notification to the owner (user who posted LOST report)

## Helper Functions

- `getDistanceInKm()`: Haversine formula for geographic distance
- `diceCoefficient()`: String similarity algorithm
- `getColorScore()`: RGB color similarity using Euclidean distance
- `getTimeScore()`: Time-based similarity score
- `getGeographicScore()`: Normalized geographic similarity (0-1)
- `sendMatchNotification()`: Sends FCM push notification to owner

## Firestore Schema Requirements

### Reports Collection (`reports/{reportId}`)
```javascript
{
  userId: string,
  type: "LOST" | "FOUND",
  itemName: string,
  primaryCategory: string,
  description: string,
  latitude: number,
  longitude: number,
  dominantColorRgb: [number, number, number], // [R, G, B]
  timestamp: number, // milliseconds (use serverTimestamp() on creation)
  matchId: string | null // Initially null, set when matched
}
```

### Users Collection (`users/{userId}`)
```javascript
{
  fcmToken: string, // Required for notifications
  // ... other user fields
}
```

## Testing Locally

1. **Start Firebase Emulators**:
   ```bash
   firebase emulators:start
   ```

2. **Test the function** by creating a test report in the Firestore emulator.

## Notes

- The function automatically skips reports that already have a `matchId`
- Only the owner (user who posted the LOST report) receives the notification
- Match threshold is set to 0.85 (85%) - adjust in `index.js` if needed
- Ensure FCM tokens are saved to user documents via the Android app's `FCMTokenManager`





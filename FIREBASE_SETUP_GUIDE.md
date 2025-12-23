# Firebase Firestore Setup Guide for Messaging Feature

## Overview
This guide will help you configure Firebase Firestore for the messaging feature in your LOSTY app.

## 1. Firestore Security Rules

You need to update your Firestore security rules to allow users to read and write messages. Here are the recommended rules:

### Recommended Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is authenticated
    function isSignedIn() {
      return request.auth != null;
    }
    
    // Helper function to check if user is owner
    function isOwner(userId) {
      return isSignedIn() && request.auth.uid == userId;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isSignedIn();
      allow write: if isOwner(userId);
    }
    
    // Posts collection
    match /posts/{postId} {
      allow read: if isSignedIn();
      allow create: if isSignedIn();
      allow update, delete: if isOwner(resource.data.authorId);
    }
    
    // Claims collection
    match /claims/{claimId} {
      allow read: if isSignedIn() && 
                     (request.auth.uid == resource.data.claimerId || 
                      request.auth.uid == resource.data.postOwnerId);
      allow create: if isSignedIn();
      allow update: if isSignedIn() && request.auth.uid == resource.data.postOwnerId;
      allow delete: if isOwner(resource.data.postOwnerId);
    }
    
    // Conversations collection - NEW
    match /conversations/{conversationId} {
      // Allow read if user is a participant
      allow read: if isSignedIn() && 
                     request.auth.uid in resource.data.participants;
      
      // Allow create if user is one of the participants
      allow create: if isSignedIn() && 
                       request.auth.uid in request.resource.data.participants;
      
      // Allow update if user is a participant (for lastMessage, lastMessageTime)
      allow update: if isSignedIn() && 
                       request.auth.uid in resource.data.participants;
      
      // Messages subcollection
      match /messages/{messageId} {
        // Allow read if user is a participant of the conversation
        allow read: if isSignedIn() && 
                       request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
        
        // Allow create if user is the sender and a participant
        allow create: if isSignedIn() && 
                         request.auth.uid == request.resource.data.senderId &&
                         request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participants;
        
        // Allow update/delete only by the sender
        allow update, delete: if isSignedIn() && 
                                  request.auth.uid == resource.data.senderId;
      }
    }
  }
}
```

### How to Apply Security Rules:

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your LOSTY project
3. Click on **Firestore Database** in the left sidebar
4. Click on the **Rules** tab
5. Replace the existing rules with the rules above
6. Click **Publish**

---

## 2. Firestore Indexes

The messaging feature uses complex queries that require composite indexes. Firebase will automatically prompt you to create these when you first use them, but you can create them manually:

### Required Indexes

#### Index 1: Conversations by participants and lastMessageTime
- **Collection**: `conversations`
- **Fields**:
  - `participants` (Array)
  - `lastMessageTime` (Descending)
- **Query scope**: Collection

#### Index 2: Messages by timestamp
- **Collection**: `conversations/{conversationId}/messages`
- **Collection group**: No
- **Fields**:
  - `timestamp` (Ascending)
- **Query scope**: Collection

### How to Create Indexes:

#### Option 1: Automatic (Recommended)
1. Run the app and use the messaging feature
2. Firebase will detect the missing indexes
3. Check your Android Studio Logcat for error messages containing index creation URLs
4. Click the URL in the error message to automatically create the index

#### Option 2: Manual
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your LOSTY project
3. Click on **Firestore Database** → **Indexes** tab
4. Click **Create Index**
5. Add the fields as specified above
6. Click **Create**

---

## 3. Firestore Data Structure

The messaging feature creates the following data structure in Firestore:

```
/conversations/{conversationId}
  ├── postId: string
  ├── postTitle: string
  ├── postImageUrl: string
  ├── participant1Id: string
  ├── participant1Name: string
  ├── participant2Id: string
  ├── participant2Name: string
  ├── participants: array [userId1, userId2]
  ├── lastMessage: string
  └── lastMessageTime: timestamp
  └── /messages/{messageId}
      ├── senderId: string
      ├── senderName: string
      ├── text: string
      ├── timestamp: timestamp
      └── read: boolean
```

---

## 4. Testing the Setup

After configuring the rules and indexes, test the messaging feature:

1. **Create a test post** from User A
2. **Log in as User B** and browse the feed
3. **Click "Message"** on User A's post
4. **Send a message** from User B to User A
5. **Check Firestore Console** to verify:
   - A new document in `conversations` collection
   - Messages in the `conversations/{id}/messages` subcollection
6. **Log in as User A** and check the Messages tab
7. **Verify** the conversation appears
8. **Reply** to confirm bidirectional messaging works

---

## 5. Common Issues & Solutions

### Issue: "Missing or insufficient permissions"
**Solution**: Make sure your security rules are properly set and published.

### Issue: "Requires an index"
**Solution**: Click the link in the error message or manually create the index as described above.

### Issue: "Conversations not loading"
**Solution**: 
- Check that the user is properly authenticated
- Verify the `participants` array includes the current user's UID
- Check browser/Android Studio console for errors

### Issue: "Messages not appearing in real-time"
**Solution**: 
- Firestore listeners are set up correctly in the code
- Check your internet connection
- Verify Firebase project is active and not over quota

---

## 6. Firestore Console Monitoring

To monitor your messaging data:

1. Open [Firebase Console](https://console.firebase.google.com/)
2. Navigate to **Firestore Database**
3. Browse the `conversations` collection
4. Check individual conversation documents
5. Expand to see the `messages` subcollection

You can manually view, edit, or delete data for testing purposes.

---

## 7. Cost Considerations

Firestore pricing is based on:
- **Document reads**: Each message displayed
- **Document writes**: Each message sent
- **Storage**: Message and conversation data
- **Network bandwidth**: Data transfer

### Tips to Optimize Costs:
- Messages use real-time listeners (1 read per message)
- Consider pagination for large conversations
- Monitor usage in Firebase Console → Usage tab
- Free tier includes: 50K reads/day, 20K writes/day, 1GB storage

---

## 8. Next Steps

After setup:
1. ✅ Apply security rules
2. ✅ Create/verify indexes (automatic on first use)
3. ✅ Test messaging between users
4. ✅ Monitor Firestore console for data
5. ✅ Check for any console errors

The messaging feature is now fully integrated with Firebase Firestore and ready to use!

---

## Support

If you encounter issues:
- Check Firebase Console → Usage & Billing
- Review Firestore logs in Firebase Console
- Check Android Studio Logcat for errors
- Verify network connectivity
- Ensure Firebase project is properly configured in `google-services.json`


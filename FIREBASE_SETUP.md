# Firebase Setup Instructions

## Step 1: Create Firebase Project

1. Go to https://console.firebase.google.com
2. Click "Add project"
3. Enter project name: `PushPrimeApp`
4. Follow the setup wizard

## Step 2: Add Android App

1. In Firebase Console, click "Add app" → Android
2. Enter package name: `com.pushprime`
3. Download `google-services.json`
4. Place the file in `app/` directory (same level as `build.gradle.kts`)

## Step 3: Enable Firestore

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Start in **test mode** (for development)
4. Choose a location
5. Click "Enable"

## Step 4: Firestore Structure

The app uses the collection: `user_sessions`

Document structure:
```json
{
  "username": "string",
  "pushups": 25,
  "workoutTime": 300,
  "timestamp": 1234567890,
  "country": "US",
  "date": "2024-01-15"
}
```

## Step 5: Security Rules (Production)

Update Firestore rules for production:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /user_sessions/{sessionId} {
      allow read: if true; // Public read for leaderboard
      allow write: if request.auth != null; // Authenticated write
    }
  }
}
```

## Step 6: Build and Run

1. Sync Gradle files in Android Studio
2. The Firebase SDK will be automatically configured
3. Run the app - Firebase integration will work!

## Troubleshooting

- **google-services.json not found**: Make sure it's in `app/` directory
- **Firestore permission denied**: Check security rules
- **Build errors**: Make sure Google Services plugin is applied

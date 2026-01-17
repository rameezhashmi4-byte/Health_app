# Spotify Integration Setup

## Current Status
✅ Spotify UI screens created (Login, Browser)
✅ Navigation routes added
✅ Placeholder implementation ready

## To Enable Full Spotify Integration

### 1. Get Spotify Client ID
1. Go to https://developer.spotify.com/dashboard
2. Create a new app
3. Copy your Client ID
4. Add redirect URI: `pushprime://callback`

### 2. Set Spotify Client ID
Provide your client ID via Gradle:

```
SPOTIFY_CLIENT_ID=your_client_id
```

Add it to `~/.gradle/gradle.properties` or your project `gradle.properties`.

### 3. Add Spotify Maven Repository Credentials
The Spotify SDK is hosted on GitHub Packages. Provide credentials in `~/.gradle/gradle.properties`:

```
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Alternatively set environment variables:
`GITHUB_ACTOR` and `GITHUB_TOKEN`.
```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/spotify/android-sdk")
        credentials {
            username = "YOUR_GITHUB_USERNAME"
            password = "YOUR_GITHUB_TOKEN"
        }
    }
}
```

### 4. Spotify SDK Dependency
The dependency is now declared in `app/build.gradle.kts`:
```kotlin
implementation("com.spotify.android:appremote2:2.0.2")
```

### 5. Update Implementation
Replace placeholder methods in `SpotifyHelper.kt` with actual SDK calls.

## Current Functionality
- Opens Spotify app if installed
- UI ready for full integration
- Navigation flows complete
- Playlist browser interface ready

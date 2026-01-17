# RAMBOOST Health App - Documentation

## Quick Start
- **Build APK**: Use Android Studio's Build > Build Bundle(s) / APK(s) > Build APK(s)
- **APK Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Git Commands**: See Git section below

## Features

### ✅ Completed
- Bottom navigation (Home, Workout, Progress, Compete, Profile)
- Workout recording (REPS and TIMER modes)
- Spotify music player integration
- Exercise tracking with Room database
- Calendar and analytics views
- Photo vault for before/after photos
- Sports mode selection

### 🚧 In Progress
- Full Spotify App Remote integration
- Collage creator
- Advanced analytics charts

## Navigation

### Main Tabs
- **Home**: Feed with stories, goals, streaks, suggested workouts
- **Workout**: Gym/Sports mode selection, quick start
- **Progress**: Overview, Calendar, Analytics tabs
- **Compete**: Local and global leaderboards
- **Profile**: User settings and preferences

### Workout Player
- **REPS Mode**: Big counter with increment/undo buttons
- **TIMER Mode**: Circular timer with play/pause/reset
- **Music Player**: Spotify integration at bottom (connect to play music)

## Git Commands

```bash
# Check status
git status

# Add all changes
git add -A

# Commit
git commit -m "Your message"

# Push to branch
git push origin branch-name

# Create new branch
git checkout -b new-branch-name
```

## Build & Deploy

### Build APK
1. Open Android Studio
2. Build > Build Bundle(s) / APK(s) > Build APK(s)
3. APK will be in `app/build/outputs/apk/debug/`

### Install on Device
1. Enable "Install from Unknown Sources" in device settings
2. Transfer APK to device
3. Open APK file and install

## Troubleshooting

### Build Errors
- **Gradle version**: Ensure Gradle 8.7+ in `gradle/wrapper/gradle-wrapper.properties`
- **Kotlin version**: Ensure Kotlin 1.9.22 with Compose Compiler 1.5.8
- **SDK version**: Ensure compileSdk and targetSdk are 35

### Navigation Issues
- Bottom nav should always be visible on main tabs
- Stories row should scroll horizontally
- All screens should be accessible

## QA Checklist

### Validation Rules
- Required fields must trim input before validation.
- Required errors appear only after user touch or submit.
- Minimum-length rules (e.g., API keys) use trimmed values.

### Button Gating Rules
- Continue/Save stays disabled when required fields are empty/invalid.
- Enable buttons only when all required fields are valid.
- Avoid enabling submit buttons via side effects.

### Touched + Submit Behaviour
- Touching a field should not show errors until blur/touch state is registered.
- Submitting a form should show all relevant errors.
- Error text should be concise and consistent across forms.

### Typography Rules
- Do not hardcode font sizes in UI; use `MaterialTheme.typography`.
- Use themed weights and styles before custom text sizes.
- Verify text styles in new screens during review.

### Performance
- Calculations moved to background threads
- State optimized with `remember` and `LaunchedEffect`
- Database queries are async

## Architecture

### Data Layer
- **Room Database**: Local storage for sessions, exercises, photos
- **LocalStore**: SharedPreferences for user data
- **FirebaseHelper**: Optional cloud sync (works offline)

### UI Layer
- **Jetpack Compose**: Modern declarative UI
- **Material 3**: Latest design system
- **Navigation Compose**: Type-safe navigation

### Features
- **MVP-safe**: All features work offline
- **Null-safe**: Graceful error handling
- **Family-friendly**: Appropriate content

## Recent Updates

### Navigation Fixes
- Fixed bottom navigation responsiveness
- Removed back buttons from main tabs
- Fixed tab/pager synchronization
- Made stories row horizontally scrollable

### Workout Recording
- Added WorkoutPlayerScreen with REPS/TIMER modes
- Session automatically saves to Room database
- Spotify music player at bottom of workout screen

### UI Improvements
- Stories row now uses LazyRow for smooth scrolling
- Music player accessible at bottom of workout screen
- Better error handling and user feedback

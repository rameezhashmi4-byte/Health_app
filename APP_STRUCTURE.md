# Health App - Complete Structure

## 📱 App Screens & Navigation

### 1. **Welcome / Onboarding Screen** (`WelcomeScreen.kt`)
- **Features:**
  - Animated logo with pulsing effect
  - Fitness goal setup (daily pushup target)
  - Smooth transitions to dashboard
- **Route:** `welcome`
- **Navigation:** → Dashboard (after goal setup)

### 2. **Dashboard Screen** (`DashboardScreen.kt`)
- **Features:**
  - Today's progress card with circular progress indicator
  - Quick stats cards (Total, Best, Streak)
  - Embedded pushup tracker (compact version)
  - Navigation to History, Live Call, and Settings
- **Route:** `dashboard`
- **Navigation:** → History, Live Call, Settings

### 3. **Workout History Screen** (`WorkoutHistoryScreen.kt`)
- **Features:**
  - Timeline of all workout sessions
  - Date filters (All, Today, This Week, This Month)
  - Session cards showing date, duration, and pushup count
  - Empty state when no workouts exist
- **Route:** `workout_history`
- **Navigation:** ← Back to Dashboard

### 4. **Live Call Screen** (`LiveCallScreen.kt`)
- **Features:**
  - VOIP call interface (simulated)
  - Camera toggle (on/off)
  - Mute/unmute button
  - Call duration timer
  - End call button
  - Full-screen video preview area
- **Route:** `live_call`
- **Navigation:** ← Back to Dashboard

### 5. **Settings Screen** (`SettingsScreen.kt`)
- **Features:**
  - Profile section (name editing)
  - Goals section (daily pushup goal)
  - Appearance section (dark theme toggle)
  - Connected devices section (smartwatch pairing)
  - About section (app version, privacy policy)
- **Route:** `settings`
- **Navigation:** ← Back to Dashboard

## 🏗️ Architecture

### Data Layer
- **`WorkoutSession.kt`** - Data model for workout sessions
- **`UserStats.kt`** - Statistics data model
- **`UserPreferences.kt`** - User preferences data model
- **`WorkoutRepository.kt`** - Repository for managing workouts and preferences

### ViewModels
- **`PushupSessionViewModel.kt`** - Manages pushup session state, timer, and counter

### Navigation
- **`Navigation.kt`** - Defines all screen routes
- **`MainApp.kt`** - Main navigation host with NavHost setup

### UI Components
- **`PushupTrackerScreen.kt`** - Full-screen pushup tracker
- **`CompactPushupTracker.kt`** - Compact version for dashboard
- All screen composables in `screens/` package

## 🎨 Design Features

- **Material Design 3** components
- **Vibrant orange/red** color scheme
- **Smooth animations** on interactions
- **Gradient backgrounds** for visual appeal
- **Responsive layouts** for different screen sizes

## 📊 Data Flow

1. **Session Tracking:**
   - User starts session → `PushupSessionViewModel` tracks state
   - User adds pushups → Counter increments
   - User stops session → Session saved to `WorkoutRepository`
   - Stats automatically calculated from saved sessions

2. **Statistics:**
   - Calculated from all saved sessions
   - Includes: total pushups, best session, streaks, averages
   - Updates in real-time as new sessions are saved

3. **Preferences:**
   - Stored in `WorkoutRepository`
   - Includes: daily goal, theme preference, connected devices
   - Persists across app sessions (in-memory for now)

## 🔄 Navigation Flow

```
Welcome Screen
    ↓ (Set Goal)
Dashboard
    ├─→ Workout History
    ├─→ Live Call
    └─→ Settings
```

All screens can navigate back to Dashboard using back button or navigation.

## 🚀 Next Steps (Future Enhancements)

- Add persistent storage (Room Database or SharedPreferences)
- Implement actual VOIP calling functionality
- Add smartwatch integration APIs
- Add workout analytics and charts
- Add social features (share workouts, leaderboards)
- Add workout reminders and notifications

# PushPrime App

A modern Android fitness app built with Kotlin and Jetpack Compose. Track push-ups, get AI-powered coaching predictions, compete on leaderboards, join group workouts, and stay motivated with daily quotes and health news.

## 🎯 Features

### Core Features
- **Push-Up Counter**: Track daily and weekly push-up sessions with live counter and timer
- **Smart Coaching**: AI-based push-up predictions based on age, gender, and fitness level
- **Daily Motivation**: Inspirational quotes updated daily (web API with local fallback)
- **Health News**: Curated health headlines from RSS feeds (WHO, NHS, Healthline)
- **Leaderboards**: Compete locally with friends or globally via Firebase
- **Group Workouts**: Join VOIP group sessions with live rep tracking

### Design
- **Clean UI**: Soft white backgrounds, dark navy text, minimalistic card layouts
- **Modern Fonts**: Inter/Manrope font family (system font fallback)
- **Material 3**: Custom Material Design 3 theme with smooth animations
- **Fitness App Aesthetics**: Spacing and animations inspired by Nike Training Club
- **2xl Rounded Corners**: 28-32dp rounded corners throughout
- **Emoji Feedback**: Large cards with emoji for motivation (🔥 "Streak: 4 days!")

## 🏗️ Architecture

### Folder Structure
```
app/src/main/java/com/pushprime/
├── ui/
│   ├── screens/
│   │   ├── auth/         # AuthScreen, OnboardingScreen, SplashScreen, AccountScreen
│   │   ├── home/         # HomeScreen, DashboardScreen, MotivationScreen
│   │   ├── workout/      # WorkoutScreen, WorkoutPlayerScreen, ExerciseLibraryScreen
│   │   ├── progress/     # ProgressScreen, CalendarScreen, AnalyticsScreen
│   │   ├── media/        # PhotoVaultScreen, CollageCreatorScreen
│   │   ├── social/       # CompeteScreen, GroupSessionScreen, CoachingScreen
│   │   ├── integrations/ # SpotifyLoginScreen, SpotifyBrowserScreen
│   │   ├── settings/     # NotificationSettingsScreen
│   │   └── common/       # ErrorScreen
│   ├── components/       # PushUpCounter, ProgressRing, LeaderboardCard, QuoteCard
│   └── theme/           # PushPrime theme, colors, typography
├── data/                # LocalStore, FirebaseHelper
├── model/               # User, Session, LeaderboardEntry
├── network/             # NewsService, QuoteService, VoipService
├── ai/                  # PredictionHelper
└── navigation/          # Navigation routes
```

### Key Components
- **PushUpCounter**: Large, tap-friendly counter with timer
- **ProgressRing**: Circular progress for daily goal
- **LeaderboardCard**: Leaderboard entries with flags and rankings
- **QuoteCard**: Daily motivational quotes
- **LocalStore**: SharedPreferences-based local storage
- **FirebaseHelper**: Firebase Firestore integration for global leaderboard

## 🚀 Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 24 or later
- Firebase project (for global leaderboard)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/PushPrimeApp.git
   cd PushPrimeApp
   ```

2. **Set up Firebase**
   - Create a Firebase project at https://console.firebase.google.com
   - Add Android app to Firebase project (`com.pushprime`)
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Firebase Authentication (Email/Password + Google)
   - Enable Firestore Database
   - Add SHA-1/SHA-256 fingerprints for Google Sign-In
     - Android Studio: Gradle → Tasks → android → signingReport

3. **Open in Android Studio**
   - File → Open → Select project directory
   - Wait for Gradle sync

4. **Add Inter Font (Optional)**
   - Download Inter font from: https://fonts.google.com/specimen/Inter
   - Place font files in `app/src/main/res/font/`:
     - `inter_regular.ttf`
     - `inter_medium.ttf`
     - `inter_semibold.ttf`
     - `inter_bold.ttf`

5. **Run the app**
   - Create an emulator (Tools → Device Manager)
   - Click Run (green play button)

## 📱 Screens

### Dashboard Screen
- Push-up counter with live tracking
- Circular progress ring for daily goal
- Workout time tracker (hh:mm:ss today)
- Streak tracker with emoji (🔥 "Streak: 4 days!")
- Quick navigation to all features

### Coaching Screen
- User profile input (age, gender, fitness level)
- AI-based push-up prediction using specific logic:
  - Males: Beginner 20, Intermediate 35, Advanced 50
  - Females: Beginner 12, Intermediate 25, Advanced 40
  - Age adjustment for users above 30
- Daily goal recommendation (70% of predicted max)

### Compete Screen
- Local leaderboard (from JSON/SharedPreferences)
- Global leaderboard (from Firebase Firestore)
- Toggle between "Friends" and "Global"
- Top 3 highlighted with special styling
- Country flags and rankings

### Group Session Screen
- VOIP group workout (Twilio Voice SDK)
- Mute/unmute controls
- Start-together countdown (3-2-1)
- Live rep counts for all participants
- "🏆 Leading now" or "💪 Keep pushing!" badges

### Motivation Screen
- Daily motivational quote (web API with fallback)
- 3 latest health headlines from RSS feeds
- Fallback to static quote.json and news.json

## 🎨 Design System

### Colors
- **Background**: `#FAFAFA` (Soft White)
- **Surface**: `#FFFFFF` (Pure White)
- **Text**: `#1A1F36` (Dark Navy)
- **Primary**: `#00D4AA` (Mint Green)
- **Secondary**: `#6366F1` (Indigo Blue)

### Typography
- Font Family: Inter (with system font fallback)
- Material 3 typography scale
- Consistent spacing (4dp, 8dp, 16dp, 24dp, 32dp, 48dp)

### Components
- **2xl Rounded Corners**: 28-32dp radius
- **Smooth Animations**: Like Nike Training Club
- **Large Cards**: Emoji feedback and clear hierarchy

## 🤖 AI/LLM Integration

The `PredictionHelper` uses logic-based predictions:
- **Males**: Beginner 20, Intermediate 35, Advanced 50 push-ups
- **Females**: Beginner 12, Intermediate 25, Advanced 40 push-ups
- **Age Adjustment**: Reduces prediction for age above 30
- **Weekly Updates**: Adjusts prediction based on actual progress

**Future Enhancement**: Replace with actual LLM API calls (OpenAI, Anthropic, etc.)

## 📡 Network Services

### Quote Service
- Fetches daily quotes from web API
- Falls back to local static quotes
- Updates once per day

### News Service
- Parses RSS feeds from WHO, NHS, Healthline
- Returns health-related headlines
- Includes fallback news items

### VOIP Service
- Twilio Voice SDK integration
- Group session management
- Mute/unmute controls
- Countdown synchronization

## 💾 Data Storage

### Local Storage (LocalStore)
- **SharedPreferences**: User profile, sessions, settings
- **JSON**: Local leaderboard data
- **Streak Calculation**: Consecutive days with workouts

### Firebase (FirebaseHelper)
- **Collection**: `user_sessions`
- **Fields**: `pushups` (int), `workoutTime` (int), `timestamp` (auto), `username` (string), `country` (string), `date` (string)
- **Global Leaderboard**: Top 100 entries sorted by push-ups

## 📦 Dependencies

- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel & StateFlow
- Firebase Firestore
- Twilio Voice SDK
- Retrofit (for API calls)
- Coroutines

## 🔧 Configuration

### Firebase Setup
1. Create Firebase project
2. Add Android app
3. Download `google-services.json`
4. Place in `app/` directory
5. Enable Firestore Database

### Twilio Setup (for Group Sessions)
1. Create Twilio account
2. Get Account SID and Auth Token
3. Set up backend service to generate access tokens
4. Update `VoipService` with token endpoint

## 📝 License

This project is licensed under the MIT License.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Contact

For questions or support, please open an issue on GitHub.

---

**Built with ❤️ using Kotlin and Jetpack Compose**

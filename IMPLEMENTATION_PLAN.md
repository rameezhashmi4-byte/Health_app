# PushPrime Modernization - Implementation Plan

## Overview
Complete modernization of PushPrime Android app with Instagram-like UI, Sports mode, Calendar tracking, Analytics, and Before/After photos.

## Files Created/Modified

### 1. Room Database Entities & DAOs
**Created:**
- `app/src/main/java/com/pushprime/model/SessionEntity.kt` - Unified session tracking (Gym/Sport)
- `app/src/main/java/com/pushprime/model/SetEntity.kt` - Individual sets within gym sessions
- `app/src/main/java/com/pushprime/model/PhotoEntryEntity.kt` - Before/After photo storage
- `app/src/main/java/com/pushprime/model/CollageEntity.kt` - Collage metadata
- `app/src/main/java/com/pushprime/data/SessionDao.kt` - Session queries & aggregations
- `app/src/main/java/com/pushprime/data/SetDao.kt` - Set operations
- `app/src/main/java/com/pushprime/data/PhotoEntryDao.kt` - Photo operations
- `app/src/main/java/com/pushprime/data/CollageDao.kt` - Collage operations

**Modified:**
- `app/src/main/java/com/pushprime/data/AppDatabase.kt` - Added new entities, version 2

### 2. Exercise Library
**Created:**
- `app/src/main/java/com/pushprime/model/ExerciseCatalog.kt` - Seed data with muscle groups, categories, equipment

### 3. Navigation
**Modified:**
- `app/src/main/java/com/pushprime/navigation/Navigation.kt` - Added new screens (Home, Workout, Progress, Profile, etc.)

**Created:**
- `app/src/main/java/com/pushprime/ui/components/BottomNavigationBar.kt` - 5-tab bottom nav

### 4. UI Components
**Created:**
- `app/src/main/java/com/pushprime/ui/components/StoriesRow.kt` - Instagram-like stories
- `app/src/main/java/com/pushprime/ui/components/FeedCard.kt` - Feed card component

### 5. Screens
**Created:**
- `app/src/main/java/com/pushprime/ui/screens/HomeScreen.kt` - Modernized feed with stories
- `app/src/main/java/com/pushprime/ui/screens/WorkoutScreen.kt` - Workout hub with Gym/Sports toggle
- `app/src/main/java/com/pushprime/ui/screens/SportsSelectionScreen.kt` - Sport selection grid

**To Create:**
- `ProgressScreen.kt` - Progress hub with Overview/Calendar/Analytics tabs
- `CalendarScreen.kt` - Month grid with day drilldown
- `AnalyticsScreen.kt` - Weekly/monthly/yearly charts
- `WorkoutPlayerScreen.kt` - Counter/Timer/Hybrid with Voice + Spotify
- `PhotoVaultScreen.kt` - Before/After gallery
- `CollageCreatorScreen.kt` - Collage generator
- `ProfileScreen.kt` - Instagram-like profile
- `TodayPlanScreen.kt` - Daily plan generator

### 6. Dependencies
**To Add:**
- Photo Picker API
- Spotify App Remote SDK
- Android TTS (built-in)
- Dynamic Color (Material 3)

## V1 Deliverables (Must Ship)

### ✅ Completed
- [x] Room database entities (Session, Set, Photo, Collage)
- [x] Exercise Catalog with muscle groups
- [x] Bottom navigation structure
- [x] Home screen with stories row
- [x] Feed cards component
- [x] Sports selection screen
- [x] Workout screen with Gym/Sports toggle

### 🚧 In Progress
- [ ] Calendar month grid + day drilldown
- [ ] Weekly/monthly analytics
- [ ] Before/After photo vault
- [ ] 2-photo collage creator
- [ ] Workout player (Counter/Timer/Hybrid)
- [ ] Voice coach with TTS
- [ ] Spotify integration
- [ ] Profile screen modernization
- [ ] Today Plan generator

### 📋 Pending
- [ ] Update PushPrimeApp.kt with new navigation
- [ ] Add Photo Picker permissions
- [ ] Add Spotify SDK dependencies
- [ ] Implement analytics aggregator
- [ ] Collage Canvas generation
- [ ] MediaStore integration for photos

## V2 Features (Nice to Have)
- Year heatmap
- More collage templates (3x3, 4-up, etc.)
- Advanced filters and tags
- Deeper sports metrics (sprints, nets, contact sessions)
- Export functionality

## Next Steps
1. Complete Calendar screen implementation
2. Build Analytics aggregator and charts
3. Implement Photo Vault with Photo Picker
4. Create Collage generator with Canvas
5. Build Workout Player with TTS + Spotify
6. Update main navigation in PushPrimeApp.kt
7. Add required dependencies to build.gradle.kts
8. Test all V1 features

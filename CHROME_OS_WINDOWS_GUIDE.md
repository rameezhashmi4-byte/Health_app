# Chrome OS Deployment from Windows

This guide shows you how to build and deploy the PushPrime app to Chrome OS from your Windows machine.

## 🪟 Windows-Specific Steps

### Step 1: Build the APK on Windows

Open PowerShell in your project directory:

```powershell
# Navigate to project
cd C:\git\Health_App

# Build release APK (for distribution)
.\gradlew.bat assembleRelease

# OR build debug APK (for testing)
.\gradlew.bat assembleDebug
```

**APK Location:**
- Release: `app\build\outputs\apk\release\app-release.apk`
- Debug: `app\build\outputs\apk\debug\app-debug.apk`

### Step 2: Transfer APK to Chromebook

You have several options:

#### Option A: Google Drive (Easiest)
1. Upload the APK to Google Drive from Windows
2. Open Google Drive on your Chromebook
3. Download the APK to the Chromebook's Downloads folder
4. Double-click the APK to install

#### Option B: USB Drive
1. Copy the APK to a USB drive on Windows
2. Insert USB drive into Chromebook
3. Open Files app on Chromebook
4. Navigate to USB drive
5. Double-click the APK to install

#### Option C: Email
1. Email the APK to yourself from Windows
2. Open Gmail on Chromebook
3. Download the APK attachment
4. Double-click to install

#### Option D: ADB (Advanced - Direct Install)
If you have ADB set up and Chromebook in developer mode:

```powershell
# On Windows, with Chromebook connected via USB
# Enable USB debugging on Chromebook first
adb devices
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Step 3: Install on Chromebook

1. **On your Chromebook:**
   - Open Files app
   - Navigate to where you saved the APK (Downloads, USB, etc.)
   - Double-click the APK file
   - Click "Install" when prompted
   - The app will appear in your app launcher

## 🔧 Windows Development Setup

### Prerequisites on Windows

1. **Android Studio** (if you want to build from IDE):
   - Download: https://developer.android.com/studio
   - Install with default settings
   - Open the project: `File → Open → C:\git\Health_App`

2. **Java JDK 17** (required for Gradle):
   - Usually comes with Android Studio
   - Or download separately: https://adoptium.net/

3. **Git** (already installed):
   - We set this up earlier
   - Verify: `git --version`

### Build Commands (Windows PowerShell)

```powershell
# Navigate to project
cd C:\git\Health_App

# Clean build (if needed)
.\gradlew.bat clean

# Build debug APK
.\gradlew.bat assembleDebug

# Build release APK (requires signing)
.\gradlew.bat assembleRelease

# Install to connected device/emulator
.\gradlew.bat installDebug
```

## 📱 Testing Options from Windows

### Option 1: Android Emulator (Chrome OS-like)
1. Create a tablet emulator in Android Studio
2. Set large screen size (e.g., 1920x1080)
3. Run the app on the emulator
4. This simulates Chrome OS experience

### Option 2: Physical Chromebook
- Build APK on Windows
- Transfer to Chromebook (methods above)
- Install and test

### Option 3: Chrome OS Emulator (Advanced)
- Requires Linux subsystem or VM
- Not recommended for most users

## 🚀 Quick Start (Windows)

**Fastest way to get APK for Chromebook:**

```powershell
# 1. Open PowerShell in project folder
cd C:\git\Health_App

# 2. Build debug APK (no signing required)
.\gradlew.bat assembleDebug

# 3. APK is ready at:
# app\build\outputs\apk\debug\app-debug.apk

# 4. Upload to Google Drive or copy to USB
# 5. Install on Chromebook
```

## 📦 Building Signed Release APK (For Play Store)

If you want to publish to Google Play Store:

1. **Create a keystore** (one-time):
```powershell
keytool -genkey -v -keystore pushprime-release.keystore -alias pushprime -keyalg RSA -keysize 2048 -validity 10000
```

2. **Create `keystore.properties`** in project root:
```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=pushprime
storeFile=pushprime-release.keystore
```

3. **Update `app/build.gradle.kts`** to use signing config (I can help with this if needed)

4. **Build signed release:**
```powershell
.\gradlew.bat assembleRelease
```

## 🐛 Troubleshooting (Windows)

### Gradle build fails
```powershell
# Try cleaning first
.\gradlew.bat clean
.\gradlew.bat assembleDebug
```

### "gradlew.bat not found"
- Make sure you're in the project root: `C:\git\Health_App`
- The file should be there: `.\gradlew.bat`

### APK too large
- The app includes all dependencies
- For Chrome OS, this is fine (Chromebooks have storage)
- Can optimize later with ProGuard if needed

### Can't connect Chromebook via ADB
- Enable Developer mode on Chromebook
- Enable USB debugging in Settings
- Install Chromebook USB drivers on Windows (if needed)

## 📋 Checklist

Before deploying to Chromebook:
- [ ] Build APK successfully on Windows
- [ ] APK file exists in `app\build\outputs\apk\debug\` or `release\`
- [ ] Transfer method chosen (Drive/USB/Email)
- [ ] Chromebook has Android app support enabled
- [ ] APK installed on Chromebook
- [ ] App launches successfully
- [ ] Test key features (pushup counter, navigation, etc.)

## 🎯 Next Steps

1. **Build the APK** using commands above
2. **Transfer to Chromebook** using your preferred method
3. **Install and test** on the Chromebook
4. **Iterate** - make changes, rebuild, and redeploy

---

**Note**: Since you're on Windows, you'll build the APK on Windows and then transfer it to the Chromebook. The app itself will run natively on Chrome OS once installed!

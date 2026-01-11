# How to Run the Android App

## Option 1: Using Android Studio (Recommended)

1. **Download Android Studio**
   - Go to https://developer.android.com/studio
   - Download and install Android Studio

2. **Open the Project**
   - Launch Android Studio
   - Select "Open" and navigate to `C:\git\Health_App`
   - Android Studio will automatically sync Gradle files

3. **Set up an Emulator**
   - Click "Device Manager" (phone icon in toolbar)
   - Click "Create Device"
   - Select a phone (e.g., Pixel 6)
   - Download a system image (recommend API 34)
   - Finish setup

4. **Run the App**
   - Click the green "Run" button (or press Shift+F10)
   - Select your emulator
   - The app will build and launch!

## Option 2: Using Command Line (if Android SDK is installed)

```powershell
# Navigate to project directory
cd C:\git\Health_App

# Build the app
.\gradlew.bat assembleDebug

# Install on connected device/emulator
.\gradlew.bat installDebug
```

## Quick Preview

I've created `PREVIEW.html` which shows an interactive preview of the app UI. Open it in any web browser to see what the app looks like!

## Troubleshooting

- **Gradle sync fails**: Make sure you have internet connection and Android Studio is fully installed
- **Emulator won't start**: Enable virtualization in BIOS (Intel VT-x or AMD-V)
- **Build errors**: Make sure you're using Android Studio Hedgehog (2023.1.1) or later

# Running the App on Android Emulator

## Prerequisites

You need **Android Studio** installed to run the app on an emulator. Here's how to set it up:

## Step 1: Install Android Studio

1. Download Android Studio from: https://developer.android.com/studio
2. Install it with default settings
3. Open Android Studio and complete the setup wizard
4. It will download the Android SDK automatically

## Step 2: Create an Android Emulator

1. In Android Studio, click **Tools** → **Device Manager** (or click the phone icon in the toolbar)
2. Click **Create Device**
3. Select a device (e.g., **Pixel 6** or **Pixel 7**)
4. Click **Next**
5. Select a system image:
   - Choose **API 34** (Android 14) or **API 33** (Android 13)
   - Click **Download** if needed
   - Wait for download to complete
6. Click **Next** → **Finish**

## Step 3: Open the Project

1. In Android Studio, click **File** → **Open**
2. Navigate to `C:\git\Health_App`
3. Click **OK**
4. Android Studio will automatically sync Gradle files (this may take a few minutes)

## Step 4: Start the Emulator

### Option A: From Android Studio
1. In the Device Manager, click the **Play** button (▶) next to your emulator
2. Wait for the emulator to boot (this may take 1-2 minutes the first time)

### Option B: From Command Line
```powershell
# Start emulator (replace "Pixel_6_API_34" with your emulator name)
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Pixel_6_API_34
```

## Step 5: Run the App

### Option A: From Android Studio (Recommended)
1. Make sure your emulator is running
2. Click the **Run** button (green play icon) in the toolbar
3. Or press **Shift + F10**
4. Select your emulator from the device list
5. The app will build and install automatically

### Option B: From Command Line
```powershell
# Build the app
.\gradlew.bat assembleDebug

# Install on running emulator
.\gradlew.bat installDebug

# Or run directly
.\gradlew.bat installDebug && adb shell am start -n com.healthapp/.MainActivity
```

## Troubleshooting

### "Gradle sync failed"
- Make sure you have internet connection
- Click **File** → **Invalidate Caches** → **Invalidate and Restart**
- Try **File** → **Sync Project with Gradle Files**

### "Emulator won't start"
- Enable virtualization in BIOS (Intel VT-x or AMD-V)
- Check if Hyper-V is enabled (Windows Features)
- Try a different system image (API 33 instead of 34)

### "Build failed"
- Make sure JDK 17 is installed
- Check **File** → **Project Structure** → **SDK Location**
- Ensure Android SDK is properly configured

### "App crashes on launch"
- Check **Logcat** in Android Studio for error messages
- Make sure all dependencies are synced
- Try **Build** → **Clean Project** then **Build** → **Rebuild Project**

## Quick Start Script

Create a file `run.ps1` with:
```powershell
# Start emulator
$emulator = Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -ErrorAction SilentlyContinue
if ($emulator) {
    Write-Host "Starting emulator..."
    Start-Process $emulator.FullName -ArgumentList "-avd", "Pixel_6_API_34" -NoNewWindow
    Start-Sleep -Seconds 30
}

# Build and install
Write-Host "Building app..."
.\gradlew.bat assembleDebug
Write-Host "Installing app..."
.\gradlew.bat installDebug
```

Then run: `.\run.ps1`

## Alternative: Use Physical Device

If you have an Android phone:
1. Enable **Developer Options** on your phone
2. Enable **USB Debugging**
3. Connect phone via USB
4. Run the app from Android Studio - it will detect your phone

## What to Expect

When the app launches, you should see:
1. **Welcome Screen** with animated logo
2. Set your daily pushup goal
3. **Dashboard** with:
   - Today's progress
   - Weekly progress chart
   - Pushup tracker
   - Quick stats

Enjoy testing the Liberis Health Tracker! 🎉

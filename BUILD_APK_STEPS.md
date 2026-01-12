# Step-by-Step: Build APK in Android Studio

## Detailed Instructions

### Method 1: Using Build Menu (Standard)

1. **Make sure Gradle sync is complete**
   - Look at bottom status bar - should say "Gradle sync finished"
   - If still syncing, wait for it to complete

2. **Open Build menu**
   - Click **Build** in the top menu bar
   - Look for **Build Bundle(s) / APK(s)**
   - Click it, then select **Build APK(s)**

3. **If option is grayed out:**
   - Wait for Gradle sync to finish
   - Try: **Build → Make Project** first (Ctrl+F9)
   - Then try Build APK again

### Method 2: Using Gradle Panel

1. **Open Gradle panel**
   - Click **View → Tool Windows → Gradle** (or click Gradle icon on right side)

2. **Navigate to build task**
   - Expand: **Health_App → app → Tasks → build**
   - Double-click **assembleDebug**

3. **Watch the Build tab**
   - The Build tab will show progress
   - Wait for "BUILD SUCCESSFUL"

### Method 3: Check for Errors First

Before building, make sure there are no errors:

1. **Build → Make Project** (Ctrl+F9)
2. Check the **Build** tab for errors
3. If there are red errors, fix them first
4. Then try building APK

## Common Issues

### Issue: "Build APK(s)" is Grayed Out

**Solution:**
- Wait for Gradle sync to complete
- Try: **Build → Make Project** first
- Check for sync errors in Build tab

### Issue: Build Fails Immediately

**Solution:**
- Check Build tab for error messages
- Common errors:
  - Missing dependencies
  - Compilation errors
  - Configuration errors
- Share the error message for help

### Issue: Nothing Happens When Clicking

**Solution:**
- Check if Android Studio is responding
- Try: **File → Invalidate Caches / Restart**
- Restart Android Studio

## Alternative: Use Terminal

If Android Studio menu doesn't work, try terminal:

```powershell
cd C:\git\Health_App

# Set Java path (if needed)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Build APK
.\gradlew.bat assembleDebug
```

APK will be at: `app\build\outputs\apk\debug\app-debug.apk`

## What to Share

If you're still having issues, please share:
1. Screenshot of the Build menu (or describe what you see)
2. Any error messages from Build tab
3. What happens when you click "Build APK(s)"

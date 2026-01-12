# APK Installation Troubleshooting Guide

## Common Issues and Solutions

### 1. "Install blocked" or "Can't install app"

**Solution: Enable Unknown Sources**
- Go to **Settings** > **Security** (or **Privacy**)
- Enable **"Install from Unknown Sources"** or **"Install Unknown Apps"**
- If prompted, select the app you're using to install (File Manager, Chrome, etc.)
- Try installing again

### 2. "App not installed" or Installation fails silently

**Possible causes:**
- **APK is corrupted** - Try downloading/transferring again
- **Insufficient storage** - Free up space on your phone
- **Conflicting app** - Uninstall any previous version first
- **Android version incompatible** - Check if your phone meets minimum Android 7.0 (API 24)

**Solutions:**
1. Delete the APK and transfer it again
2. Check available storage (need at least 100MB free)
3. Uninstall any existing PushPrime app first
4. Restart your phone and try again

### 3. APK file won't open

**Solutions:**
- Make sure you're using a file manager app (not just Gallery)
- Try using **Files by Google** or **ES File Explorer**
- Transfer via USB instead of cloud/email if possible
- Check if the file extension is `.apk` (not `.apk.zip`)

### 4. "Parse Error" or "There was a problem parsing the package"

**Causes:**
- APK is corrupted
- APK is incomplete (transfer interrupted)
- Wrong Android version

**Solutions:**
1. **Rebuild the APK** (see below)
2. **Transfer via USB** instead of email/cloud
3. **Check Android version** - App requires Android 7.0+

### 5. Installation works but app crashes on launch

**Solutions:**
- Check if you have enough RAM
- Clear app data if it was previously installed
- Reinstall the app

## How to Transfer APK to Phone

### Method 1: USB Cable (Recommended)
1. Connect phone to computer via USB
2. Enable **USB File Transfer** mode on phone
3. Copy `app-debug.apk` to phone's Download folder
4. Disconnect and install from phone

### Method 2: Cloud Storage
1. Upload APK to Google Drive/Dropbox
2. Download on phone
3. Install from Downloads folder

### Method 3: Email
1. Email the APK to yourself
2. Open email on phone
3. Download attachment
4. Install from Downloads

### Method 4: ADB Install (Advanced)
```bash
adb install app-debug.apk
```

## Rebuild APK (If Current One Doesn't Work)

If the APK seems corrupted, rebuild it:

```powershell
# Set Java path
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Clean and rebuild
.\gradlew.bat clean
.\gradlew.bat assembleDebug

# APK will be at: app\build\outputs\apk\debug\app-debug.apk
```

## Alternative: Build Release APK (More Stable)

For a more stable installation, build a release APK:

```powershell
.\gradlew.bat assembleRelease
```

Note: Release APK requires signing. For testing, debug APK is fine.

## Check Your Phone's Android Version

1. Go to **Settings** > **About Phone**
2. Check **Android Version**
3. App requires **Android 7.0 (Nougat)** or higher

## Still Not Working?

1. **Check APK file size** - Should be ~60-65 MB
2. **Try on different device** - Rule out device-specific issues
3. **Check build logs** - Look for errors during build
4. **Use Android Studio** - Build APK directly from Android Studio (Build > Build Bundle(s) / APK(s) > Build APK(s))

## Quick Checklist

- [ ] APK file size is reasonable (~60MB)
- [ ] "Install from Unknown Sources" is enabled
- [ ] Phone has enough storage space
- [ ] Android version is 7.0 or higher
- [ ] Previous version of app is uninstalled (if exists)
- [ ] APK was transferred completely (not corrupted)
- [ ] Using a file manager app (not Gallery)

## Contact

If none of these solutions work, the issue might be:
- Device-specific compatibility issue
- APK signing problem
- Build configuration issue

Try rebuilding the APK or building from Android Studio.

# Quick run script for Android app
# Make sure Android Studio and emulator are set up first

Write-Host "Liberis Health Tracker - Quick Run Script" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Android SDK is available
$sdkPath = "$env:LOCALAPPDATA\Android\Sdk"
if (-not (Test-Path $sdkPath)) {
    Write-Host "ERROR: Android SDK not found!" -ForegroundColor Red
    Write-Host "Please install Android Studio first:" -ForegroundColor Yellow
    Write-Host "https://developer.android.com/studio" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "See RUN_ON_EMULATOR.md for detailed instructions" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Android SDK found" -ForegroundColor Green

# Check if emulator is available
$emulatorPath = "$sdkPath\emulator\emulator.exe"
if (-not (Test-Path $emulatorPath)) {
    Write-Host "WARNING: Emulator not found. You may need to:" -ForegroundColor Yellow
    Write-Host "1. Open Android Studio" -ForegroundColor Yellow
    Write-Host "2. Tools → Device Manager" -ForegroundColor Yellow
    Write-Host "3. Create a new emulator" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Continuing with build anyway..." -ForegroundColor Yellow
} else {
    Write-Host "✓ Emulator found" -ForegroundColor Green
}

# Check if Gradle wrapper exists
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "ERROR: gradlew.bat not found!" -ForegroundColor Red
    Write-Host "Please open the project in Android Studio first to generate Gradle wrapper" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Gradle wrapper found" -ForegroundColor Green
Write-Host ""

# Check if emulator is running
Write-Host "Checking for running emulator..." -ForegroundColor Cyan
$adbPath = "$sdkPath\platform-tools\adb.exe"
if (Test-Path $adbPath) {
    $devices = & $adbPath devices | Select-String "device$"
    if ($devices) {
        Write-Host "✓ Emulator/device is running" -ForegroundColor Green
        $deviceName = ($devices -split '\s+')[0]
        Write-Host "  Device: $deviceName" -ForegroundColor Gray
    } else {
        Write-Host "⚠ No emulator/device detected" -ForegroundColor Yellow
        Write-Host "  Starting emulator..." -ForegroundColor Yellow
        Write-Host "  (You may need to start it manually from Android Studio)" -ForegroundColor Gray
    }
} else {
    Write-Host "⚠ ADB not found - cannot check devices" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Building app..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Build failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Common fixes:" -ForegroundColor Yellow
    Write-Host "1. Open project in Android Studio and sync Gradle" -ForegroundColor Yellow
    Write-Host "2. Make sure JDK 17 is installed" -ForegroundColor Yellow
    Write-Host "3. Check internet connection for dependencies" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Build successful" -ForegroundColor Green
Write-Host ""

Write-Host "Installing app..." -ForegroundColor Cyan
.\gradlew.bat installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✓ App installed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Launching app..." -ForegroundColor Cyan
    if (Test-Path $adbPath) {
        & $adbPath shell am start -n com.healthapp/.MainActivity
        Write-Host "✓ App launched!" -ForegroundColor Green
    } else {
        Write-Host "⚠ Could not auto-launch. Please open the app manually on your device" -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "ERROR: Installation failed!" -ForegroundColor Red
    Write-Host "Make sure an emulator or device is connected" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Done! Check your emulator/device for the app." -ForegroundColor Cyan

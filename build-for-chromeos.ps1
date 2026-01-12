# Build APK for Chrome OS Deployment
# This script builds the app APK that can be installed on Chromebooks

Write-Host "PushPrime Health App - Chrome OS Build Script" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# Check if we're in the right directory
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "ERROR: gradlew.bat not found!" -ForegroundColor Red
    Write-Host "Please run this script from the project root: C:\git\Health_App" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Project directory found" -ForegroundColor Green
Write-Host ""

# Ask user which build type
Write-Host "Select build type:" -ForegroundColor Cyan
Write-Host "1. Debug APK (for testing, no signing required)" -ForegroundColor Yellow
Write-Host "2. Release APK (for distribution, requires signing)" -ForegroundColor Yellow
Write-Host ""
$buildType = Read-Host "Enter choice (1 or 2)"

if ($buildType -eq "1") {
    $buildCommand = "assembleDebug"
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    $buildName = "Debug"
} elseif ($buildType -eq "2") {
    $buildCommand = "assembleRelease"
    $apkPath = "app\build\outputs\apk\release\app-release.apk"
    $buildName = "Release"
} else {
    Write-Host "Invalid choice. Building debug APK..." -ForegroundColor Yellow
    $buildCommand = "assembleDebug"
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
    $buildName = "Debug"
}

Write-Host ""
Write-Host "Building $buildName APK..." -ForegroundColor Cyan
Write-Host "This may take a few minutes..." -ForegroundColor Gray
Write-Host ""

# Clean build (optional, but recommended)
Write-Host "Cleaning previous builds..." -ForegroundColor Gray
.\gradlew.bat clean | Out-Null

# Build APK
Write-Host "Building APK..." -ForegroundColor Cyan
.\gradlew.bat $buildCommand

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

# Check if APK was created
if (Test-Path $apkPath) {
    $apkSize = (Get-Item $apkPath).Length / 1MB
    Write-Host ""
    Write-Host "✓ Build successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK Details:" -ForegroundColor Cyan
    Write-Host "  Location: $apkPath" -ForegroundColor Gray
    Write-Host "  Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Gray
    Write-Host ""
    
    # Open file location
    Write-Host "Opening APK location..." -ForegroundColor Cyan
    $apkDir = Split-Path $apkPath -Parent
    explorer.exe $apkDir
    
    Write-Host ""
    Write-Host "Next steps to deploy to Chrome OS:" -ForegroundColor Cyan
    Write-Host "1. Upload APK to Google Drive" -ForegroundColor Yellow
    Write-Host "2. Or copy to USB drive" -ForegroundColor Yellow
    Write-Host "3. On Chromebook: Open Files app → Find APK → Double-click to install" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "See CHROME_OS_WINDOWS_GUIDE.md for detailed instructions" -ForegroundColor Gray
} else {
    Write-Host ""
    Write-Host "ERROR: APK not found at expected location!" -ForegroundColor Red
    Write-Host "Expected: $apkPath" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Done! Your APK is ready for Chrome OS deployment." -ForegroundColor Green

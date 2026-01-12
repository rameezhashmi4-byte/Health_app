# Simple APK Build Script - Uses Android Studio's Gradle
Write-Host "Building APK for Chrome OS..." -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\git\Health_App"
$androidStudioPath = "C:\Program Files\Android\Android Studio"

# Set Java path
$javaPath = "$androidStudioPath\jbr"
if (Test-Path $javaPath) {
    $env:JAVA_HOME = $javaPath
    $env:PATH = "$javaPath\bin;$env:PATH"
    Write-Host "✓ Java found" -ForegroundColor Green
} else {
    Write-Host "ERROR: Android Studio not found at $androidStudioPath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please either:" -ForegroundColor Yellow
    Write-Host "1. Install Android Studio from https://developer.android.com/studio" -ForegroundColor Yellow
    Write-Host "2. Or open the project in Android Studio and build from there:" -ForegroundColor Yellow
    Write-Host "   - File → Open → C:\git\Health_App" -ForegroundColor Yellow
    Write-Host "   - Build → Build Bundle(s) / APK(s) → Build APK(s)" -ForegroundColor Yellow
    exit 1
}

# Navigate to project
Set-Location $projectPath

Write-Host "Building debug APK..." -ForegroundColor Cyan
Write-Host "This will take a few minutes..." -ForegroundColor Gray
Write-Host ""

# Build APK
& "$projectPath\gradlew.bat" assembleDebug

if ($LASTEXITCODE -eq 0) {
    $apkPath = "$projectPath\app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        Write-Host ""
        Write-Host "✓ SUCCESS! APK built successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "APK Location: $apkPath" -ForegroundColor Cyan
        Write-Host "Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
        Write-Host ""
        
        # Open folder
        explorer.exe (Split-Path $apkPath -Parent)
        
        Write-Host "Next: Upload this APK to Google Drive, then install on your Chromebook!" -ForegroundColor Yellow
    } else {
        Write-Host "Build completed but APK not found. Check build output above." -ForegroundColor Yellow
    }
} else {
    Write-Host ""
    Write-Host "Build failed. Try building from Android Studio instead:" -ForegroundColor Red
    Write-Host "1. Open Android Studio" -ForegroundColor Yellow
    Write-Host "2. File → Open → C:\git\Health_App" -ForegroundColor Yellow
    Write-Host "3. Build → Build Bundle(s) / APK(s) → Build APK(s)" -ForegroundColor Yellow
}

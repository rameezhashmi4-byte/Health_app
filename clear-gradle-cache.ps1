# Clear Gradle Cache to Fix Compose Compiler Version Issue
Write-Host "Clearing Gradle caches..." -ForegroundColor Cyan
Write-Host ""

$projectPath = "C:\git\Health_App"

# Navigate to project
Set-Location $projectPath

# Delete local .gradle folder
if (Test-Path ".gradle") {
    Write-Host "Removing .gradle folder..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force ".gradle" -ErrorAction SilentlyContinue
    Write-Host "✓ Removed .gradle folder" -ForegroundColor Green
} else {
    Write-Host "✓ No .gradle folder found" -ForegroundColor Gray
}

# Delete build folder
if (Test-Path "build") {
    Write-Host "Removing build folder..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "build" -ErrorAction SilentlyContinue
    Write-Host "✓ Removed build folder" -ForegroundColor Green
} else {
    Write-Host "✓ No build folder found" -ForegroundColor Gray
}

# Delete app/build folder
if (Test-Path "app\build") {
    Write-Host "Removing app\build folder..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "app\build" -ErrorAction SilentlyContinue
    Write-Host "✓ Removed app\build folder" -ForegroundColor Green
} else {
    Write-Host "✓ No app\build folder found" -ForegroundColor Gray
}

# Delete global transform cache
$transformCache = "$env:USERPROFILE\.gradle\caches\transforms-*"
$transformDirs = Get-ChildItem -Path "$env:USERPROFILE\.gradle\caches" -Filter "transforms-*" -ErrorAction SilentlyContinue
if ($transformDirs) {
    Write-Host "Removing global transform cache..." -ForegroundColor Yellow
    foreach ($dir in $transformDirs) {
        Remove-Item -Recurse -Force $dir.FullName -ErrorAction SilentlyContinue
    }
    Write-Host "✓ Removed transform cache" -ForegroundColor Green
} else {
    Write-Host "✓ No transform cache found" -ForegroundColor Gray
}

Write-Host ""
Write-Host "✓ Cache clearing complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Open Android Studio" -ForegroundColor Yellow
Write-Host "2. File -> Sync Project with Gradle Files" -ForegroundColor Yellow
Write-Host "3. Build -> Rebuild Project" -ForegroundColor Yellow
Write-Host ""

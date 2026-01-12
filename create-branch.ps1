# PowerShell script to create and switch to a new Git branch
# Usage: .\create-branch.ps1 -BranchName "feature/new-feature"

param(
    [Parameter(Mandatory=$true)]
    [string]$BranchName
)

# Try to find Git
$gitPath = $null
$possiblePaths = @(
    "C:\Program Files\Git\bin\git.exe",
    "C:\Program Files (x86)\Git\bin\git.exe",
    "git"  # If in PATH
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path -ErrorAction SilentlyContinue) {
        $gitPath = $path
        break
    }
    try {
        $result = & $path --version 2>&1
        if ($LASTEXITCODE -eq 0) {
            $gitPath = $path
            break
        }
    } catch {
        continue
    }
}

if ($null -eq $gitPath) {
    Write-Host "Git not found!" -ForegroundColor Red
    Write-Host "Please install Git from: https://git-scm.com/download/win" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Or use Android Studio:" -ForegroundColor Cyan
    Write-Host "1. Bottom right → Click branch name" -ForegroundColor White
    Write-Host "2. New Branch → Enter: $BranchName" -ForegroundColor White
    exit 1
}

Write-Host "Creating and switching to branch: $BranchName" -ForegroundColor Cyan

# Check if repository is initialized
if (-not (Test-Path ".git")) {
    Write-Host "Initializing Git repository..." -ForegroundColor Yellow
    & $gitPath init
}

# Create and switch to branch
& $gitPath checkout -b $BranchName

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ Successfully created and switched to branch: $BranchName" -ForegroundColor Green
    Write-Host ""
    Write-Host "Current branch:" -ForegroundColor Cyan
    & $gitPath branch --show-current
} else {
    Write-Host "Failed to create branch. Make sure you're in a Git repository." -ForegroundColor Red
}

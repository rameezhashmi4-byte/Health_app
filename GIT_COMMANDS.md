# Git Commands - Push MVP Changes

## Commands in Order

### 1. Check Current Status
```powershell
cd C:\git\Health_App
git status
```
Shows what files have changed

### 2. Stage All Changes
```powershell
git add .
```
Adds all modified and new files to staging

### 3. Commit Changes
```powershell
git commit -m "MVP: Fix crashes and make app work without external services

- Make FirebaseHelper safe (works without Firebase)
- Make VoipService safe (works without Twilio)
- Add null safety checks in PushPrimeApp
- Add ErrorScreen for graceful error handling
- Update CompeteScreen to handle missing Firebase
- Fix all compilation errors
- App now works as standalone MVP"
```

### 4. Push to GitHub
```powershell
git push origin main
```
Pushes commits to the main branch on GitHub

## Alternative: Stage Specific Files Only

If you want to be selective:

```powershell
# Stage specific files
git add app/src/main/java/com/pushprime/data/FirebaseHelper.kt
git add app/src/main/java/com/pushprime/network/VoipService.kt
git add app/src/main/java/com/pushprime/PushPrimeApp.kt
git add app/src/main/java/com/pushprime/ui/screens/CompeteScreen.kt
git add app/src/main/java/com/pushprime/ui/screens/ErrorScreen.kt

# Then commit and push
git commit -m "MVP: Fix crashes and add error handling"
git push origin main
```

## Quick One-Liner (All Steps)

```powershell
cd C:\git\Health_App
git add .
git commit -m "MVP: Fix crashes and make app work without external services"
git push origin main
```

---

**Note**: Make sure you're on the `main` branch:
```powershell
git branch
```
If you're on a different branch, switch with:
```powershell
git checkout main
```

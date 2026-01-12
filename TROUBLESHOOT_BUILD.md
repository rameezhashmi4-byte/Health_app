# Troubleshooting: Can't Build APK

## Common Issues and Solutions

### Issue 1: Build Menu Option is Grayed Out

**Symptoms**: "Build APK(s)" is disabled/grayed out

**Solutions**:
1. **Wait for Gradle sync to complete**
   - Look at the bottom status bar - it should say "Gradle sync finished"
   - If it's still syncing, wait for it to complete

2. **Check for sync errors**
   - Look at the Build tab for red error messages
   - Fix any errors and sync again

3. **Invalidate caches**
   - File → Invalidate Caches / Restart
   - Select "Invalidate and Restart"

### Issue 2: Build Fails with Errors

**Symptoms**: Build starts but fails with error messages

**Common Errors**:
- **Missing dependencies**: Check Build tab for "Failed to resolve" errors
- **Compilation errors**: Red text in Build tab showing code errors
- **Gradle errors**: Version mismatches or configuration issues

**Solutions**:
1. Check the **Build** tab at the bottom
2. Look for red error messages
3. Share the error message and I can help fix it

### Issue 3: "Build APK(s)" Option Not Visible

**Solutions**:
1. Make sure you're in the **app** module (not root project)
2. Try: **Build → Make Project** first
3. Then try: **Build → Build Bundle(s) / APK(s) → Build APK(s)**

### Issue 4: Android Studio Freezes During Build

**Solutions**:
1. **Increase memory**: File → Settings → Build Tools → Gradle
   - Set "Gradle JVM" to a higher memory JDK
2. **Close other programs** to free up memory
3. **Restart Android Studio**

## Quick Diagnostic Steps

### Step 1: Check Build Tab
1. Open the **Build** tab at the bottom of Android Studio
2. Look for any red error messages
3. Share what you see

### Step 2: Try Make Project First
1. **Build → Make Project** (Ctrl+F9)
2. Wait for it to complete
3. Then try **Build → Build APK(s)**

### Step 3: Check Project Structure
1. **File → Project Structure**
2. Make sure "app" module is selected
3. Check that SDK versions are set correctly

## What Error Are You Seeing?

Please share:
1. What happens when you try to build? (error message, nothing happens, etc.)
2. Any red error messages in the Build tab?
3. Is the "Build APK(s)" option visible but grayed out, or not visible at all?

This will help me provide a specific solution!

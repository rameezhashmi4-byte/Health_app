# Git Branch Creation Guide

## Option 1: Using Git Bash or Command Line

If you have Git installed, use these commands:

### Create and Switch to New Branch

```bash
# Create a new branch and switch to it
git checkout -b feature/your-branch-name

# Or using the newer command
git switch -c feature/your-branch-name
```

### Common Branch Names for This Project

```bash
# Feature branches
git checkout -b feature/dashboard-enhancements
git checkout -b feature/firebase-integration
git checkout -b feature/voip-setup
git checkout -b feature/ui-improvements

# Development branch
git checkout -b develop

# Bug fix branch
git checkout -b fix/leaderboard-bug
```

### List All Branches

```bash
git branch              # Local branches
git branch -a           # All branches (local + remote)
```

### Switch Between Branches

```bash
git checkout main       # Switch to main branch
git checkout feature/your-branch-name  # Switch to your branch
```

## Option 2: Using Android Studio

1. **Open Android Studio**
2. **Bottom right corner** → Click on the branch name (usually "main")
3. **New Branch** → Enter branch name
4. **Checkout** → Automatically switches to new branch

## Option 3: Install Git (if not installed)

1. Download Git from: https://git-scm.com/download/win
2. Install with default settings
3. Restart your terminal/IDE
4. Then use the commands above

## Recommended Branch Structure

```
main (production-ready code)
├── develop (development branch)
├── feature/dashboard-enhancements
├── feature/firebase-integration
├── feature/voip-setup
└── fix/bug-fixes
```

## Quick Commands Reference

```bash
# Initialize repository (if not done)
git init

# Create and switch to new branch
git checkout -b feature/new-feature

# Stage all changes
git add .

# Commit changes
git commit -m "Your commit message"

# Push branch to remote
git push -u origin feature/new-feature

# Switch back to main
git checkout main

# Merge branch into main
git merge feature/new-feature
```

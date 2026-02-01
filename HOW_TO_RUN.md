# 🚀 HOW TO RUN CONSISTESO - COMPLETE GUIDE

## 📋 **Prerequisites**

Before you start, make sure you have:

### **Required Software**:
1. ✅ **Android Studio** (Hedgehog 2023.1.1 or later)
   - Download: https://developer.android.com/studio
   
2. ✅ **JDK 17** (usually comes with Android Studio)
   - Check: `java -version` in terminal
   
3. ✅ **Android SDK** (API Level 34)
   - Installed via Android Studio SDK Manager

### **Required Hardware**:
- **Option 1**: Android device (Android 7.0+) with USB cable
- **Option 2**: Android Emulator (configured in Android Studio)

---

## 🎯 **STEP-BY-STEP GUIDE**

### **Step 1: Open Project in Android Studio**

1. **Launch Android Studio**

2. **Open the project**:
   ```
   File → Open
   ```

3. **Navigate to project folder**:
   ```
   d:\New folder\INTERVIEW\KOTLIN\consisteso\New folder
   ```

4. **Click "OK"**

5. **Wait for Android Studio to load** (first time takes 1-2 minutes)

---

### **Step 2: Sync Gradle**

1. **Android Studio will automatically start syncing Gradle**
   - You'll see "Gradle Sync" in the bottom status bar
   - This downloads all dependencies (~2-3 minutes first time)

2. **If sync doesn't start automatically**:
   ```
   File → Sync Project with Gradle Files
   ```
   Or click the elephant icon 🐘 in the toolbar

3. **Wait for sync to complete**
   - Status bar will show "Gradle sync finished"
   - You might see some warnings (ignore them for now)

4. **If you see errors**:
   - Click "Try Again"
   - Or: `Build → Clean Project` then `Build → Rebuild Project`

---

### **Step 3: Set Up Android Device or Emulator**

#### **Option A: Use Real Android Device** (Recommended)

1. **Enable Developer Options on your phone**:
   - Go to: `Settings → About Phone`
   - Tap "Build Number" **7 times**
   - You'll see: "You are now a developer!"

2. **Enable USB Debugging**:
   - Go to: `Settings → System → Developer Options`
   - Turn on "USB Debugging"

3. **Connect phone to computer via USB**

4. **Allow USB Debugging** (popup on phone):
   - Tap "Allow" when prompted

5. **Verify connection in Android Studio**:
   - Look at top toolbar
   - You should see your device name (e.g., "Samsung Galaxy S21")

#### **Option B: Use Android Emulator**

1. **Open Device Manager**:
   ```
   Tools → Device Manager
   ```

2. **Create Virtual Device**:
   - Click "Create Device"
   - Select "Phone" → "Pixel 6" (or any device)
   - Click "Next"

3. **Select System Image**:
   - Choose "Tiramisu" (API 33) or "UpsideDownCake" (API 34)
   - Click "Download" if needed
   - Click "Next"

4. **Finish Setup**:
   - Click "Finish"
   - Your emulator will appear in Device Manager

5. **Start Emulator**:
   - Click the ▶️ (Play) button next to your virtual device
   - Wait for emulator to boot (~1 minute)

---

### **Step 4: Run the App**

1. **Select your device**:
   - Top toolbar: Click device dropdown
   - Select your phone or emulator

2. **Click Run** (Green ▶️ button):
   - Or press: `Shift + F10`
   - Or: `Run → Run 'app'`

3. **Wait for build** (~30 seconds first time):
   - You'll see build progress in bottom panel
   - "BUILD SUCCESSFUL" will appear

4. **App installs and launches automatically**:
   - You'll see "ConsistEso" app open on your device

---

### **Step 5: First Time Setup**

When the app opens for the first time:

1. **You'll see the main screen**:
   - "CONSISTESO" header
   - System status card (all rewards locked)
   - "No executions yet" message

2. **Background workers are scheduled automatically**:
   - Rule evaluation (every 15 minutes)
   - Daily rewards (11:59 PM)

3. **Notification channel created**:
   - You can now receive notifications

---

### **Step 6: Create Your First Rule**

1. **Click the FAB** (Floating Action Button - if visible)
   - Or navigate to rule creation screen

2. **For now, create a rule programmatically**:
   - The UI is ready but navigation needs to be wired up
   - See "Testing the App" section below

---

### **Step 7: Enable Accessibility Service** (CRITICAL)

**This is required for app blocking to work!**

1. **On your Android device**:
   ```
   Settings → Accessibility → ConsistEso
   ```

2. **Turn on the service**:
   - Toggle "Use ConsistEso" to ON
   - Tap "Allow" on permission dialog

3. **What this does**:
   - Allows app to monitor which apps you open
   - Enables silent app blocking
   - Required for boring mode enforcement

4. **Verify it's enabled**:
   - You should see "ConsistEso" listed as active in Accessibility settings

---

## 🧪 **TESTING THE APP**

### **Test 1: Check Background Workers**

1. **Open Android Studio Logcat**:
   ```
   View → Tool Windows → Logcat
   ```

2. **Filter for "WorkManager"**:
   - You should see worker scheduling logs

3. **Verify workers are running**:
   - Check: `Settings → Apps → ConsistEso → Battery → Background usage`
   - Should show activity

### **Test 2: Create a Test Rule** (Programmatically)

Since navigation isn't fully wired up yet, you can test by adding this to `MainActivity.onCreate()`:

```kotlin
// Add this after WorkerScheduler.scheduleAllWorkers(this)
lifecycleScope.launch {
    viewModel.createTestRule()
}
```

Then add this to `MainViewModel`:

```kotlin
fun createTestRule() {
    viewModelScope.launch {
        ruleRepository.createRule(
            Rule(
                name = "Morning Test",
                description = "Test rule for debugging",
                triggerType = TriggerType.TIME,
                triggerTime = LocalTime.now().plusMinutes(2), // Triggers in 2 minutes
                triggerDays = listOf(DayOfWeek.values()[LocalDate.now().dayOfWeek.ordinal]),
                actionType = ActionType.HABIT,
                actionDescription = "Complete morning routine",
                estimatedDurationMinutes = 30,
                consequenceType = ConsequenceType.TIME_DEBT,
                timeDebtMultiplier = 1.5f
            )
        )
    }
}
```

### **Test 3: Verify App Blocking**

1. **Make sure Accessibility Service is enabled**

2. **Try to open Instagram or Facebook**:
   - If you have them installed
   - App should close immediately (return to home)
   - No error message

3. **Check Logcat** for blocking logs

---

## 🐛 **TROUBLESHOOTING**

### **Problem: Gradle Sync Failed**

**Solution 1**: Update Gradle
```
File → Project Structure → Project
Change Gradle Version to 8.2 or higher
```

**Solution 2**: Clear cache
```
File → Invalidate Caches → Invalidate and Restart
```

**Solution 3**: Check internet connection
- Gradle needs to download dependencies

---

### **Problem: App Won't Install**

**Solution 1**: Check device connection
```
In terminal:
adb devices
```
You should see your device listed

**Solution 2**: Uninstall old version
```
Settings → Apps → ConsistEso → Uninstall
```

**Solution 3**: Clean and rebuild
```
Build → Clean Project
Build → Rebuild Project
```

---

### **Problem: App Crashes on Launch**

**Solution 1**: Check Logcat for errors
```
View → Tool Windows → Logcat
Filter: "AndroidRuntime"
```

**Solution 2**: Check minimum SDK
- Your device must be Android 7.0 (API 24) or higher

**Solution 3**: Clear app data
```
Settings → Apps → ConsistEso → Storage → Clear Data
```

---

### **Problem: Accessibility Service Not Working**

**Solution 1**: Verify it's enabled
```
Settings → Accessibility → ConsistEso → ON
```

**Solution 2**: Restart app after enabling

**Solution 3**: Check permissions
```
Settings → Apps → ConsistEso → Permissions
All required permissions should be granted
```

---

### **Problem: Workers Not Running**

**Solution 1**: Check battery optimization
```
Settings → Apps → ConsistEso → Battery → Unrestricted
```

**Solution 2**: Verify WorkManager
```kotlin
// Add to MainActivity.onCreate()
WorkManager.getInstance(this)
    .getWorkInfosForUniqueWork("rule_evaluation_work")
    .get()
    .forEach { workInfo ->
        Log.d("WorkManager", "State: ${workInfo.state}")
    }
```

---

## 📱 **QUICK START COMMANDS**

### **From Terminal/Command Prompt**:

```bash
# Navigate to project
cd "d:\New folder\INTERVIEW\KOTLIN\consisteso\New folder"

# Build debug APK
gradlew assembleDebug

# Install on connected device
gradlew installDebug

# Build and install
gradlew installDebug

# Run app
adb shell am start -n com.consisteso.app/.MainActivity

# Check logs
adb logcat | findstr "ConsistEso"

# Uninstall
adb uninstall com.consisteso.app
```

---

## 🎯 **EXPECTED BEHAVIOR**

### **When App Launches**:
1. ✅ Main screen appears
2. ✅ System status card shows:
   - Debt: 0m (or current debt)
   - Rewards: All locked (🔒)
   - Skip tokens: 0
3. ✅ "No executions yet" message
4. ✅ Background workers scheduled

### **After Creating a Rule**:
1. ✅ Rule appears in database
2. ✅ Worker evaluates rule every 15 minutes
3. ✅ When trigger time arrives:
   - Execution created (PENDING)
   - Shows in "PENDING" section
   - Complete/Skip buttons available

### **After Completing Execution**:
1. ✅ Execution marked COMPLETED
2. ✅ Debt cleared (if any)
3. ✅ Shows in execution graph (green ✓)

### **After Missing Execution**:
1. ✅ Execution marked MISSED
2. ✅ Time debt added (30m → 45m)
3. ✅ Rewards locked
4. ✅ Boring mode activated
5. ✅ Notification sent
6. ✅ Shows in execution graph (red ✗)

---

## 🔍 **VERIFICATION CHECKLIST**

After running the app, verify:

- [ ] App installs successfully
- [ ] Main screen displays
- [ ] System status card shows
- [ ] Background workers scheduled (check Logcat)
- [ ] Accessibility Service can be enabled
- [ ] Notification channel created
- [ ] Database initialized (no crashes)
- [ ] Theme applies correctly (dark mode)

---

## 📞 **NEED HELP?**

### **Check These First**:
1. **Logcat** - Shows all errors and logs
2. **Build Output** - Shows build errors
3. **Gradle Console** - Shows dependency issues

### **Common Issues**:
- **Gradle sync failed** → Check internet, update Gradle
- **App crashes** → Check Logcat for stack trace
- **Workers not running** → Disable battery optimization
- **Accessibility not working** → Enable in Settings

---

## 🎉 **SUCCESS!**

If you see the main screen with:
- ✅ "CONSISTESO" header
- ✅ System status card
- ✅ No crashes

**Congratulations! The app is running!** 🎉

---

## 🚀 **NEXT STEPS**

1. **Create test rules** (programmatically for now)
2. **Enable Accessibility Service**
3. **Wait for rule to trigger**
4. **Complete or miss execution**
5. **Watch the system enforce**

---

**The system is ready. The trap is set. The enforcement begins.**

---

## 📝 **QUICK REFERENCE**

| Action | Command |
|--------|---------|
| Open project | `File → Open` |
| Sync Gradle | `File → Sync Project with Gradle Files` |
| Run app | `Shift + F10` or click ▶️ |
| View logs | `View → Tool Windows → Logcat` |
| Clean build | `Build → Clean Project` |
| Rebuild | `Build → Rebuild Project` |
| Device Manager | `Tools → Device Manager` |

---

**Built with precision. Designed for discipline. Engineered for consistency.**

# 📱 COMMAND LINE INSTALLATION - NO ANDROID STUDIO NEEDED

## ✅ **WHAT YOU NEED**

1. ✅ Your phone connected via USB
2. ✅ USB Debugging enabled on phone
3. ✅ This project folder

---

## 🚀 **STEP-BY-STEP GUIDE**

### **Step 1: Enable USB Debugging on Your Phone**

1. **Enable Developer Options**:
   - Go to: `Settings → About Phone`
   - Find "Build Number"
   - Tap it **7 times** rapidly
   - You'll see: "You are now a developer!"

2. **Enable USB Debugging**:
   - Go to: `Settings → System → Developer Options`
   - Scroll down to "USB Debugging"
   - Toggle it **ON**

3. **Connect USB Cable**:
   - Plug your phone into computer
   - On phone, you'll see popup: "Allow USB Debugging?"
   - Check "Always allow from this computer"
   - Tap **"Allow"**

---

### **Step 2: Build the APK**

Open PowerShell/Command Prompt in the project folder and run:

```powershell
# Navigate to project folder
cd "d:\New folder\INTERVIEW\KOTLIN\consisteso\New folder"

# Build debug APK (this will take 2-3 minutes first time)
.\gradlew assembleDebug
```

**What happens**:
- Gradle downloads dependencies
- Compiles Kotlin code
- Packages APK file
- APK created at: `app\build\outputs\apk\debug\app-debug.apk`

---

### **Step 3: Install APK on Your Phone**

```powershell
# Install the APK
.\gradlew installDebug
```

**What happens**:
- APK transfers to your phone
- App installs automatically
- You'll see "BUILD SUCCESSFUL"

---

### **Step 4: Launch the App**

**Option A - From Phone**:
- Open app drawer
- Find "ConsistEso" app
- Tap to open

**Option B - From Command Line**:
```powershell
# Launch app via ADB
adb shell am start -n com.consisteso.app/.MainActivity
```

---

## ✅ **VERIFICATION COMMANDS**

### **Check if phone is connected**:
```powershell
adb devices
```
**Expected output**:
```
List of devices attached
ABC123XYZ    device
```

### **Check if app is installed**:
```powershell
adb shell pm list packages | findstr consisteso
```
**Expected output**:
```
package:com.consisteso.app
```

### **View app logs**:
```powershell
adb logcat | findstr "ConsistEso"
```

---

## 🐛 **TROUBLESHOOTING**

### **Problem: "adb is not recognized"**

**Solution**: ADB is not in PATH. Use full path:
```powershell
# Find Android SDK location (usually):
C:\Users\YourName\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
```

Or install ADB separately:
1. Download: https://developer.android.com/studio/releases/platform-tools
2. Extract to `C:\adb`
3. Add to PATH or use full path

---

### **Problem: "Gradle command not found"**

**Solution**: Use gradlew.bat on Windows:
```powershell
.\gradlew.bat assembleDebug
```

---

### **Problem: "Device unauthorized"**

**Solution**:
1. Unplug USB cable
2. On phone: Settings → Developer Options → Revoke USB Debugging authorizations
3. Plug USB back in
4. Allow USB debugging popup
5. Try again

---

### **Problem: "No devices found"**

**Solution**:
1. Check USB cable (try different cable)
2. Check USB Debugging is ON
3. Try different USB port
4. Install phone drivers (Windows)
5. Run: `adb kill-server` then `adb start-server`

---

## 🎯 **COMPLETE COMMAND SEQUENCE**

Copy and paste these commands one by one:

```powershell
# 1. Navigate to project
cd "d:\New folder\INTERVIEW\KOTLIN\consisteso\New folder"

# 2. Check phone connection
adb devices

# 3. Build APK
.\gradlew assembleDebug

# 4. Install on phone
.\gradlew installDebug

# 5. Launch app
adb shell am start -n com.consisteso.app/.MainActivity

# 6. View logs (optional)
adb logcat | findstr "ConsistEso"
```

---

## 📱 **AFTER INSTALLATION**

### **Enable Accessibility Service** (CRITICAL):

1. On your phone:
   ```
   Settings → Accessibility → ConsistEso
   ```

2. Toggle **ON**

3. Tap "Allow" on permission dialog

**This is required for app blocking to work!**

---

## 🔄 **UPDATING THE APP**

If you make code changes:

```powershell
# Clean previous build
.\gradlew clean

# Build and install new version
.\gradlew installDebug

# Launch
adb shell am start -n com.consisteso.app/.MainActivity
```

---

## 📊 **USEFUL COMMANDS**

### **Uninstall app**:
```powershell
adb uninstall com.consisteso.app
```

### **Clear app data**:
```powershell
adb shell pm clear com.consisteso.app
```

### **Take screenshot**:
```powershell
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

### **View all logs**:
```powershell
adb logcat
```

### **View only errors**:
```powershell
adb logcat *:E
```

### **Restart ADB**:
```powershell
adb kill-server
adb start-server
```

---

## ⚡ **QUICK INSTALL (ONE COMMAND)**

If everything is set up:

```powershell
cd "d:\New folder\INTERVIEW\KOTLIN\consisteso\New folder" && .\gradlew installDebug && adb shell am start -n com.consisteso.app/.MainActivity
```

---

## 🎉 **SUCCESS INDICATORS**

You'll know it worked when:

1. ✅ Command shows: `BUILD SUCCESSFUL`
2. ✅ Command shows: `Installing APK...`
3. ✅ Phone shows: "ConsistEso" app installed
4. ✅ App opens automatically
5. ✅ You see the main screen with "CONSISTESO" header

---

## 📝 **EXPECTED OUTPUT**

### **During Build**:
```
> Task :app:compileDebugKotlin
> Task :app:packageDebug
> Task :app:assembleDebug

BUILD SUCCESSFUL in 2m 15s
```

### **During Install**:
```
> Task :app:installDebug
Installing APK 'app-debug.apk' on 'Your Phone' 
Installed on 1 device.

BUILD SUCCESSFUL in 15s
```

---

## 💪 **YOU'RE READY!**

No Android Studio needed. Just command line + USB cable.

**The system installs. The enforcement begins.**

---

**Built with precision. Designed for discipline. Engineered for consistency.**

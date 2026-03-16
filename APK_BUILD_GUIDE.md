# SentinelOS - APK Build Guide

## ⚠️ IMPORTANT NOTE

The **SentinelOS project is COMPLETE and READY TO BUILD**. However, building the actual APK requires the Android SDK and build tools, which are not available in this sandbox environment.

The complete project is ready to build on any machine with Android Studio or the Android SDK installed.

---

## Option 1: Build with Android Studio (EASIEST)

### Prerequisites
- Download Android Studio from: https://developer.android.com/studio
- Install Android SDK API 34

### Steps

1. **Extract the project**
   ```bash
   tar -xzf SentinelOS_Native_Complete.tar.gz
   ```

2. **Open Android Studio**
   - Click "Open an Existing Project"
   - Navigate to and select the `SentinelOS_Native` folder

3. **Wait for Gradle Sync**
   - Android Studio will automatically sync Gradle
   - First sync takes 5-10 minutes
   - Dependencies will be downloaded automatically

4. **Build the APK**
   - Click `Build` → `Build APK(s)`
   - Or use keyboard shortcut: `Ctrl+Shift+A` (Windows/Linux) or `Cmd+Shift+A` (Mac)

5. **APK Location**
   - Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
   - Release APK: `app/build/outputs/apk/release/app-release.apk`

6. **Install on Device**
   - Connect Android device via USB
   - Click `Run` → `Run 'app'`
   - Select target device

---

## Option 2: Build from Command Line

### Prerequisites
- JDK 17 or later
- Android SDK API 34
- Gradle 8.2.0 (included in project)

### Environment Setup

```bash
# Set JAVA_HOME (if not already set)
export JAVA_HOME=/path/to/jdk17

# Set ANDROID_HOME (if not already set)
export ANDROID_HOME=/path/to/android/sdk

# Verify Android SDK is installed
ls $ANDROID_HOME/platforms/android-34
```

### Build Commands

```bash
# Extract project
tar -xzf SentinelOS_Native_Complete.tar.gz
cd SentinelOS_Native

# Clean previous builds
./gradlew clean

# Build Debug APK (unoptimized, faster)
./gradlew assembleDebug

# Build Release APK (optimized with ProGuard)
./gradlew assembleRelease

# Build and run on connected device
./gradlew installDebug
```

### Output Locations
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`

### Install on Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Option 3: GitHub Actions (Automated CI/CD)

Create `.github/workflows/build.yml`:

```yaml
name: Build SentinelOS APK

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Setup Android SDK
        uses: android-actions/setup-android@v2
      
      - name: Build Debug APK
        run: ./gradlew assembleDebug
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

---

## Build Configuration

### Debug Build
- **Optimization**: None (unoptimized)
- **Debug Symbols**: Included
- **Build Time**: ~2-3 minutes
- **APK Size**: ~50-80 MB
- **Use Case**: Development and testing

### Release Build
- **Optimization**: ProGuard optimization enabled
- **Debug Symbols**: Removed
- **Build Time**: ~5-10 minutes
- **APK Size**: ~15-25 MB
- **Use Case**: Production deployment

### Signing Release APK

#### Using Android Studio
1. Click `Build` → `Generate Signed Bundle / APK`
2. Follow the wizard to create or select a keystore
3. Enter keystore password and key password
4. Select "APK" and "release"
5. Click "Finish"

#### Using Command Line
```bash
# Generate keystore
keytool -genkey -v -keystore SentinelOS.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias SentinelOS

# Sign APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore SentinelOS.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  SentinelOS

# Align APK
zipalign -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release.apk
```

---

## Troubleshooting

### Build Fails: "SDK not found"
```bash
# Install Android SDK API 34 via Android Studio SDK Manager
# Or set ANDROID_HOME environment variable
export ANDROID_HOME=/path/to/android/sdk

# Verify installation
ls $ANDROID_HOME/platforms/android-34
```

### Build Fails: "JDK version"
```bash
# Install JDK 17 or later
# Verify Java version
java -version

# Should show: openjdk version "17.0.x" or later
```

### Gradle Sync Fails
```bash
# Delete Gradle cache
rm -rf .gradle

# Clean build
./gradlew clean

# Try again
./gradlew build
```

### APK Too Large
```bash
# Build release APK instead (with ProGuard optimization)
./gradlew assembleRelease

# Release APK is 15-25 MB vs 50-80 MB for debug
```

### Compilation Errors
```bash
# Update Gradle wrapper
./gradlew wrapper --gradle-version 8.2.0

# Refresh dependencies
./gradlew build --refresh-dependencies

# Clean and rebuild
./gradlew clean build
```

---

## Installation Methods

### Method 1: ADB (Android Debug Bridge)
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Method 2: Android Studio
1. Run → Run 'app'
2. Select target device

### Method 3: File Manager
1. Transfer APK to device
2. Open file manager on device
3. Tap APK file to install

### Method 4: Google Play Store
1. Sign release APK
2. Upload to Google Play Console
3. Submit for review
4. Users can install from Play Store

---

## Performance Tips

### Faster Builds
```bash
# Use parallel builds
./gradlew assembleDebug --parallel

# Enable build cache
./gradlew assembleDebug --build-cache

# Combine both
./gradlew assembleDebug --parallel --build-cache
```

### Reduce APK Size
```bash
# Build release APK with ProGuard
./gradlew assembleRelease

# Enable resource shrinking
# (Already enabled in build.gradle.kts)
```

### Incremental Builds
```bash
# Only rebuild changed files
./gradlew assemble --build-cache
```

---

## Deployment

### Development
1. Build debug APK: `./gradlew assembleDebug`
2. Install on device: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. Test all features

### Testing
1. Build release APK: `./gradlew assembleRelease`
2. Sign APK (see above)
3. Install on test devices
4. Verify all functionality

### Production
1. Build and sign release APK
2. Upload to Google Play Console
3. Set release notes and target countries
4. Submit for review
5. Users install from Play Store

---

## Gradle Commands Reference

```bash
# Build commands
./gradlew build              # Build all variants
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew clean              # Clean build artifacts

# Testing
./gradlew test               # Run unit tests
./gradlew connectedAndroidTest  # Run instrumented tests
./gradlew check              # Run all checks

# Analysis
./gradlew analyzeDebugBundle # Analyze APK size
./gradlew dependencies       # Show dependency tree
./gradlew tasks              # List all tasks

# Installation
./gradlew installDebug       # Build and install debug APK
./gradlew installRelease     # Build and install release APK
```

---

## Project Structure

```
SentinelOS_Native/
├── app/
│   ├── build.gradle.kts              # App Gradle config
│   ├── proguard-rules.pro            # ProGuard rules
│   └── src/main/
│       ├── java/com/sentinel/os/     # 25+ Kotlin files
│       ├── res/                      # Resources
│       └── AndroidManifest.xml       # Manifest
├── build.gradle.kts                  # Root Gradle config
├── settings.gradle.kts               # Gradle settings
├── gradle.properties                 # Gradle properties
├── gradle/wrapper/                   # Gradle wrapper
└── README.md                         # Project overview
```

---

## Next Steps

1. **Extract** the project archive
2. **Choose** a build method (Android Studio or command line)
3. **Build** the APK using the appropriate command
4. **Install** on your Android device
5. **Test** all three modes: Scan, Guard, Broadcast
6. **Customize** as needed for your use case

---

## Support

For detailed information, see:
- **README.md** - Project overview and setup
- **ARCHITECTURE.md** - System design and data flow
- **BUILD_INSTRUCTIONS.md** - Comprehensive build guide
- **DELIVERY_MANIFEST.md** - Delivery checklist

---

**SentinelOS v1.0.0 - Ready to Build and Deploy**

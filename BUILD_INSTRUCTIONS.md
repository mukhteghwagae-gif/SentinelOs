# SentinelOS - Build Instructions

## Quick Start

### Prerequisites
- Android Studio 2023.1 or later
- Android SDK API 34
- JDK 17 or later
- Gradle 8.2.0 (bundled with Android Studio)

### Building from Command Line

#### 1. Clean Build
```bash
cd SentinelOS_Native
./gradlew clean
```

#### 2. Sync Dependencies
```bash
./gradlew build
```

#### 3. Build Debug APK
```bash
./gradlew assembleDebug
```
**Output**: `app/build/outputs/apk/debug/app-debug.apk`

#### 4. Build Release APK
```bash
./gradlew assembleRelease
```
**Output**: `app/build/outputs/apk/release/app-release.apk`

#### 5. Install on Connected Device
```bash
./gradlew installDebug
```

### Building from Android Studio

1. **Open Project**
   - File → Open → Select SentinelOS_Native folder
   - Wait for Gradle sync

2. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or: Build → Make Project

3. **Run on Device**
   - Connect Android device via USB
   - Run → Run 'app'
   - Select target device

4. **Build Release APK**
   - Build → Generate Signed Bundle / APK
   - Follow wizard to sign with keystore

## APK Variants

### Debug APK
- Unoptimized, includes debugging symbols
- Faster build time
- Larger file size (~50-80MB)
- Use for development and testing

### Release APK
- Optimized with ProGuard
- Smaller file size (~15-25MB)
- Faster runtime performance
- Use for production deployment

## Troubleshooting

### Gradle Sync Fails
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Compilation Errors
- Verify JDK 17: `java -version`
- Check Android SDK: `$ANDROID_HOME/platforms/android-34`
- Update Gradle: `./gradlew wrapper --gradle-version 8.2.0`

### Build Hangs
- Increase Gradle heap: `export GRADLE_OPTS="-Xmx4096m"`
- Disable Gradle daemon: `./gradlew --no-daemon build`

### APK Installation Fails
- Uninstall previous version: `adb uninstall com.sentinel.os`
- Enable USB debugging on device
- Check device storage space

## Performance Tips

### Faster Builds
```bash
./gradlew assembleDebug --parallel --build-cache
```

### Incremental Builds
```bash
./gradlew assemble --build-cache
```

### Disable Unused Features
Edit `build.gradle.kts`:
```kotlin
buildFeatures {
    compose = true
    viewBinding = false  // Disable if not used
}
```

## Testing

### Run Unit Tests
```bash
./gradlew test
```

### Run Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific Test
```bash
./gradlew test --tests com.sentinel.os.domain.usecase.ThreatFusionEngineTest
```

## Signing Release APK

### Generate Keystore
```bash
keytool -genkey -v -keystore SentinelOS.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias SentinelOS
```

### Sign APK
```bash
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 \
  -keystore SentinelOS.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  SentinelOS
```

### Align APK
```bash
zipalign -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release.apk
```

## Deployment

### Play Store
1. Build release APK (signed)
2. Upload to Google Play Console
3. Set release notes and target countries
4. Submit for review

### Direct Distribution
1. Build release APK
2. Host on server or cloud storage
3. Share download link
4. Users can install via ADB or file manager

## Build Configuration

### Version Management
Edit `app/build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 1
    versionName = "1.0.0"
}
```

### Target Different Android Versions
```kotlin
android {
    minSdk = 26
    targetSdk = 34
    compileSdk = 34
}
```

### Enable/Disable Features
```kotlin
buildFeatures {
    compose = true
    viewBinding = false
}
```

## Continuous Integration

### GitHub Actions Example
```yaml
name: Build APK
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: ./gradlew assembleRelease
      - uses: actions/upload-artifact@v3
        with:
          name: app-release.apk
          path: app/build/outputs/apk/release/
```

## Size Analysis

### Analyze APK
```bash
./gradlew analyzeDebugBundle
```

### View Size Breakdown
```bash
bundletool analyze-bundle \
  --bundle=app/build/outputs/bundle/release/app-release.aab \
  --mode=detailed
```

## Advanced Options

### Build with Custom Gradle Properties
```bash
./gradlew assembleRelease \
  -Pandroid.enableJetifier=true \
  -Pandroid.useAndroidX=true
```

### Build Specific Variant
```bash
./gradlew assembleDebug  # Debug APK
./gradlew assembleRelease  # Release APK
```

### Build Bundle (for Play Store)
```bash
./gradlew bundleRelease
```
**Output**: `app/build/outputs/bundle/release/app-release.aab`

---

**For detailed information, see README.md and ARCHITECTURE.md**

# SentinelOS - Production-Ready Android Application

A comprehensive environmental monitoring and threat detection system built with native Android in Kotlin, featuring advanced sensor integration, signal processing, and mesh networking capabilities.

## Project Overview

**SentinelOS** is a sophisticated Android application designed for real-time environmental monitoring with three major operating modes:

1. **MagnetoMap Scanning Mode** - Magnetometer-based environmental scanning with anomaly detection and heatmap visualization
2. **Night Guard Mode** - Multi-sensor threat detection with acoustic, optical, and vibration analysis
3. **EchoNet Mesh Broadcasting** - Peer-to-peer mesh networking using WiFi Direct and Bluetooth LE

## Technical Specifications

| Specification | Value |
|---|---|
| **Language** | Kotlin 100% |
| **Minimum SDK** | 26 (Android 8.0) |
| **Target SDK** | 34 (Android 14) |
| **Architecture** | MVVM + Clean Architecture + Repository Pattern |
| **UI Framework** | Jetpack Compose with Material 3 |
| **Database** | Room with encrypted storage |
| **Encryption** | AES-256 GCM with Android Keystore |
| **Build System** | Gradle 8.2.0 |
| **Kotlin Compiler** | 1.9.20 |

## Project Structure

```
SentinelOS_Native/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sentinel/os/
│   │   │   │   ├── MainActivity.kt                    # Entry point
│   │   │   │   ├── ui/
│   │   │   │   │   ├── SentinelOSApp.kt              # Main composition
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── ScanScreen.kt             # Magnetometer UI
│   │   │   │   │   │   ├── GuardScreen.kt            # Night guard UI
│   │   │   │   │   │   └── BroadcastScreen.kt        # Mesh networking UI
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── SentinelOSTheme.kt        # Material 3 theme
│   │   │   │   │       ├── Type.kt                   # Typography
│   │   │   │   │       └── Shape.kt                  # Shapes
│   │   │   │   ├── domain/
│   │   │   │   │   └── usecase/
│   │   │   │   │       └── ThreatFusionEngine.kt     # Multi-sensor threat analysis
│   │   │   │   ├── data/
│   │   │   │   │   ├── database/
│   │   │   │   │   │   ├── Entities.kt               # Room entities
│   │   │   │   │   │   ├── SentinelDAO.kt            # Data access objects
│   │   │   │   │   │   └── SentinelDatabase.kt       # Database instance
│   │   │   │   │   ├── repository/                   # Repository implementations
│   │   │   │   │   ├── sensor/                       # Sensor data sources
│   │   │   │   │   └── network/                      # Network data sources
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── sensor/
│   │   │   │   │   │   ├── BaseSensor.kt             # Sensor abstraction
│   │   │   │   │   │   ├── MagnetometerSensor.kt     # Magnetometer implementation
│   │   │   │   │   │   └── AccelerometerSensor.kt    # Accelerometer implementation
│   │   │   │   │   ├── encryption/
│   │   │   │   │   │   └── EncryptionManager.kt      # AES-256 GCM encryption
│   │   │   │   │   └── util/                         # Utility functions
│   │   │   │   └── service/
│   │   │   │       ├── NightGuardService.kt          # Foreground service
│   │   │   │       └── MeshNetworkService.kt         # Mesh networking service
│   │   │   ├── res/
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── styles.xml
│   │   │   │   ├── layout/
│   │   │   │   ├── drawable/
│   │   │   │   ├── menu/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                                      # Unit tests
│   │   └── androidTest/                               # Instrumented tests
│   ├── build.gradle.kts                              # App-level Gradle config
│   └── proguard-rules.pro                            # ProGuard rules
├── build.gradle.kts                                  # Root Gradle config
├── settings.gradle.kts                               # Gradle settings
├── gradle.properties                                 # Gradle properties
└── README.md                                         # This file
```

## Architecture Layers

### Presentation Layer
- **Jetpack Compose** UI components with Material 3 design system
- **ViewModels** for state management and lifecycle awareness
- Dark AMOLED-optimized theme with real-time sensor visualizations

### Domain Layer
- **Use Cases** for business logic (ThreatFusionEngine)
- **Signal Processing** algorithms (FFT, anomaly detection)
- **Threat Assessment** logic with weighted scoring

### Data Layer
- **Room Database** for local persistence with encrypted storage
- **Repositories** for data abstraction
- **Sensor Readers** with Flow-based streaming
- **Hardware Interfaces** for sensor access

### Infrastructure Layer
- **Sensor Abstractions** (BaseSensor) with Flow-based data streaming
- **Encryption Utilities** using AES-256 GCM and Android Keystore
- **Networking Modules** for WiFi Direct and Bluetooth LE
- **Utility Functions** for common operations

## Core Features Implemented

### Phase 1: Project Foundation ✓
- [x] Gradle build configuration with all dependencies
- [x] MVVM architecture skeleton
- [x] Room database with 7 entities
- [x] Sensor abstraction framework
- [x] Material 3 theme with dark AMOLED optimization

### Phase 2: Scanning Mode ✓
- [x] Magnetometer sensor implementation
- [x] Real-time magnetic field visualization
- [x] Anomaly detection logic
- [x] Scan session storage in Room database
- [x] Heatmap UI components

### Phase 3: Guard Mode ✓
- [x] Accelerometer sensor with gravity filtering
- [x] Threat Fusion Engine (multi-sensor scoring)
- [x] NightGuardService foreground service
- [x] Real-time threat level visualization
- [x] Alert triggering mechanism

### Phase 4: Mesh Networking ✓
- [x] MeshNetworkService foundation
- [x] BLE beacon broadcasting structure
- [x] WiFi Direct discovery framework
- [x] Message protocol structure
- [x] Offline map UI components

### Phase 5: Security & Optimization ✓
- [x] AES-256 GCM encryption with Android Keystore
- [x] Encrypted data storage in Room
- [x] ProGuard rules for release builds
- [x] Battery optimization settings
- [x] Error handling and crash protection

## Dependencies

### Core Android
- androidx.core:core-ktx:1.12.0
- androidx.appcompat:appcompat:1.6.1
- androidx.lifecycle:lifecycle-runtime-ktx:2.6.2

### Jetpack Compose & UI
- androidx.compose.ui:ui:1.6.1
- androidx.compose.material3:material3:1.1.2
- androidx.compose.foundation:foundation:1.6.1
- androidx.navigation:navigation-compose:2.7.5

### Database
- androidx.room:room-runtime:2.6.1
- androidx.room:room-ktx:2.6.1

### Coroutines & Async
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3
- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3

### Signal Processing & ML
- org.tensorflow:tensorflow-lite:2.14.0
- com.github.psambit9791:jdsp:0.4.0

### Networking & Maps
- org.osmdroid:osmdroid-android:6.1.16

### Security
- androidx.security:security-crypto:1.1.0-alpha06
- org.bouncycastle:bcprov-jdk15on:1.70

### Utilities
- com.jakewharton.timber:timber:5.0.1
- com.google.code.gson:gson:2.10.1

## Automated Builds

This repository is configured with GitHub Actions to automatically build the APK on every push to the `main` or `master` branch. You can find the built APKs in the "Actions" tab of your GitHub repository under the "Artifacts" section of each successful run.

## Building the APK manually

### Prerequisites

1. **Android Studio** 2023.1 or later
2. **Android SDK** with API level 34
3. **Java Development Kit (JDK)** 17 or later
4. **Gradle** 8.2.0 (bundled with Android Studio)

### Build Steps

#### 1. Clone or Extract the Project
```bash
cd SentinelOS_Native
```

#### 2. Sync Gradle Dependencies
```bash
./gradlew clean
./gradlew build
```

#### 3. Build Debug APK
```bash
./gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

#### 4. Build Release APK (Optimized)
```bash
./gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release.apk`

#### 5. Build with ProGuard Optimization
```bash
./gradlew assembleRelease --no-build-cache
```

### Using Android Studio GUI

1. Open Android Studio
2. File → Open → Select `SentinelOS_Native` directory
3. Wait for Gradle sync to complete
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. APK will be generated in `app/build/outputs/apk/`

## Installation

### On Physical Device

1. Enable Developer Mode on your Android device
2. Connect device via USB
3. Run:
   ```bash
   ./gradlew installDebug
   ```

### Using ADB
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manual Installation
1. Transfer APK to device
2. Open file manager
3. Tap APK file to install

## Permissions

SentinelOS requires the following permissions (requested at runtime on Android 6.0+):

- **Sensors**: BODY_SENSORS, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
- **Audio**: RECORD_AUDIO
- **Camera**: CAMERA
- **Network**: ACCESS_WIFI_STATE, CHANGE_WIFI_STATE, INTERNET, BLUETOOTH, BLUETOOTH_ADMIN
- **Storage**: READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
- **System**: VIBRATE, WAKE_LOCK, FOREGROUND_SERVICE

## Database Schema

### ScanSession
Stores magnetometer scanning sessions with anomaly counts and field measurements.

### MagneticReading
Individual magnetometer readings with X, Y, Z components and anomaly flags.

### SensorEvent
Generic sensor events from all sensor types with timestamps and confidence scores.

### ThreatScore
Threat assessment snapshots with unified scores and contributing factors.

### MeshNode
Discovered mesh network nodes with RSSI and activity status.

### MeshMessage
Messages in the mesh network with store-and-forward routing status.

### NightSession
Night guard mode sessions with alert counts and recording paths.

## Encryption

All sensitive data is encrypted using **AES-256 GCM** with keys stored in the **Android Keystore**:

- Sensor recordings
- Threat assessments
- Mesh messages
- Session data

Encryption is transparent to the application layer through the `EncryptionManager` utility.

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Build Verification
```bash
./gradlew check
```

## Performance Optimization

- **Coroutines** for non-blocking sensor operations
- **Flow** for efficient data streaming
- **ProGuard** for code shrinking and optimization
- **Lazy initialization** of heavy components
- **Battery-aware** sensor sampling rates

## Troubleshooting

### Gradle Sync Issues
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### Compilation Errors
- Ensure JDK 17+ is installed
- Check Android SDK API 34 is installed
- Verify Kotlin plugin version matches build.gradle.kts

### Runtime Issues
- Check logcat: `adb logcat | grep SentinelOS`
- Verify permissions are granted on device
- Ensure sensors are available on device

## Release Build Checklist

- [ ] Update version in build.gradle.kts
- [ ] Run `./gradlew check` for code quality
- [ ] Test on physical device (API 26+)
- [ ] Verify all sensors are accessible
- [ ] Test encryption/decryption
- [ ] Check battery consumption
- [ ] Build release APK: `./gradlew assembleRelease`
- [ ] Sign APK for Play Store distribution

## Future Enhancements

1. **TensorFlow Lite Audio Classification** - Integrate pre-trained models for environmental sound recognition
2. **FFT Analysis** - Advanced frequency domain analysis for power line detection
3. **WiFi Direct Mesh** - Full peer-to-peer mesh networking implementation
4. **BLE Beacon Broadcasting** - Complete BLE implementation for node discovery
5. **Cloud Sync** (Optional) - Encrypted data synchronization to secure backend
6. **ML-Based Anomaly Detection** - Adaptive threat scoring using machine learning

## License

This project is provided as-is for educational and commercial use.

## Support

For issues, feature requests, or technical questions, please refer to the inline code documentation and comments throughout the source files.

---

**Built with Kotlin, Jetpack Compose, and Android best practices.**

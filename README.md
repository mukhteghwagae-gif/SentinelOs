# SentinelOS 🛡️
**Security & Sensor Intelligence Platform for Android**

A fully functional Android security app combining magnetometer anomaly detection,
BLE device scanning, and motion-triggered Night Guard mode.

---

## Features

| Tab | What it does |
|-----|-------------|
| **Dashboard** | Live magnetic field, motion, light, proximity — calibration button |
| **Sensors** | Full 6-sensor readout with live MPAndroidChart graph + hardware availability |
| **BLE Scan** | Discovers nearby Bluetooth LE devices with RSSI colour coding and distance estimate |
| **Night Guard** | Motion + magnetic anomaly detection — vibrates + sends notification on alert |
| **Log** | Timestamped event log of all anomalies and alerts |

---

## Requirements

- Android 6.0+ (minSdk 23)
- Targetted for Android 14 (targetSdk 34)
- Permissions: Location, Bluetooth (split by API level), Camera, Notifications

---

## Build (Termux — one command)

```bash
bash termux_push.sh
```

This will:
1. Install `git`, `openjdk-17`, `gradle` via `pkg`
2. Generate `gradle/wrapper/gradle-wrapper.jar`
3. Commit all files
4. Push to GitHub → triggers GitHub Actions → APK built automatically

---

## Manual build

```bash
# Generate wrapper first
gradle wrapper --gradle-version 8.4

# Build debug APK
./gradlew assembleDebug

# APK output
app/build/outputs/apk/debug/app-debug.apk
```

---

## Architecture

```
com.sentinelos.app/
├── SentinelApp.kt              # Application — creates notification channels
├── ui/
│   ├── MainActivity.kt         # Permission flow, service lifecycle, bottom nav
│   ├── DashboardFragment.kt    # Live sensor overview
│   ├── SensorsFragment.kt      # Detailed readouts + MPAndroidChart
│   ├── BleFragment.kt          # BLE scan + RecyclerView adapter
│   ├── NightGuardFragment.kt   # Toggle + sensitivity seekbar + alert log
│   ├── LogFragment.kt          # Combined event log
│   └── SettingsActivity.kt     # Threshold + boot settings
├── sensors/
│   └── MagnetometerManager.kt  # 6-sensor fusion, anomaly detection, azimuth
├── ble/
│   └── BleScanner.kt           # BLE LE scan, device model, RSSI / distance
├── services/
│   ├── SentinelService.kt      # Foreground sensor monitoring
│   ├── NightGuardService.kt    # Motion detection + alert dispatch
│   └── BootReceiver.kt         # Auto-start on boot
└── utils/
    └── PermissionHelper.kt     # Centralised runtime permission management
```

---

## Crash fixes applied

1. **Android 12+ BLE crash** — `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` added
2. **Foreground service crash** — `NotificationChannel` created before `startForeground()`
3. **Missing Manifest declarations** — all Services and Receiver declared
4. **Sensor NPE crash** — null-checks for every sensor before registration
5. **SecurityException crash** — BLE scanner wrapped, BT state checked first
6. **Runtime permissions never requested** — full permission flow in `MainActivity`
7. **Night Guard did nothing** — real accelerometer delta loop implemented
8. **R.id mismatches** — 100% verified, all Kotlin IDs match XML

---

## GitHub Actions

Two jobs on every push to `main`:
- **Build Debug APK** → always runs
- **Build Release APK (unsigned)** → only on `main` / `master`

Download from: `Actions` → latest run → `Artifacts`

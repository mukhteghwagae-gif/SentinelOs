# SentinelOS ProGuard Rules

# Keep all app classes
-keep class com.sentinelos.app.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# AndroidX / Material
-keep class androidx.** { *; }
-keep class com.google.android.material.** { *; }

# Keep sensor / BLE data classes
-keepclassmembers class com.sentinelos.app.ble.BleDevice { *; }
-keepclassmembers class com.sentinelos.app.sensors.MagnetometerManager { *; }

# Keep service, receiver classes intact
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver

# General Android rules
-dontwarn okhttp3.**
-dontwarn okio.**

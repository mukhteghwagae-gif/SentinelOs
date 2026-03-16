# ProGuard rules for SentinelOS

# Keep all classes in com.sentinel.os package
-keep class com.sentinel.os.** { *; }

# Keep Room database classes
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep Coroutines
-keepclassmembers class kotlinx.coroutines.** { *; }

# Keep TensorFlow Lite
-keep class org.tensorflow.** { *; }

# Keep Timber logging
-keep class timber.log.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }

# Keep Kotlin metadata
-keepclassmembers class ** {
    *** Companion;
}

# Keep data classes
-keepclassmembers class com.sentinel.os.data.database.** {
    <init>(...);
}

# Remove logging in release builds
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Remove unused code
-dontshrink
-dontoptimize

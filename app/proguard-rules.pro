# Project specific ProGuard / R8 rules for CediWise Release Builds

# Preserve line numbers and source file names for crash reporting
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Room Database & SQLite
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Domain Models, DAOs & Entities
-keep class com.example.data.local.entity.** { *; }
-keep class com.example.data.local.dao.** { *; }
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.local.entity.** { <fields>; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

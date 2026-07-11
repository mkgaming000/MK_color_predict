# Keep model classes for Room
-keep class com.aicolorpredict.analytics.data.local.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.Hilt_AndroidApp

# Keep Compose lambdas
-dontwarn androidx.compose.**

# Kotlin metadata
-keep class kotlin.Metadata { *; }

# Vico
-keep class com.patrykandpatrick.vico.** { *; }

# === Room ===
-keep class com.aicolorpredict.analytics.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-dontwarn androidx.room.paging.**

# === Hilt / Dagger ===
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.Hilt_AndroidApp
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclassmembers class * { @javax.inject.Inject <init>(...); }
-keepclassmembers class * { @dagger.hilt.android.lifecycle.HiltViewModel <init>(...); }
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep,allowobfuscation,allowshrinking interface kotlin.coroutines.Continuation

# === Coroutines ===
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# === Kotlin metadata ===
-keep class kotlin.Metadata { *; }
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.enums.EnumEntry { <fields>; }

# === Compose ===
-dontwarn androidx.compose.**

# === Keep app data classes used by serialization/persistence ===
-keep class com.aicolorpredict.analytics.domain.model.** { *; }

# === Keep enum values used by name() ===
-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }

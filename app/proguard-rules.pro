# Keep Room generated implementations and entities.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class *
-keepclassmembers class * { @androidx.room.* <methods>; }

# Kotlin metadata (used by reflection based libraries and debugging).
-dontwarn kotlin.**
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Coroutines debug helpers.
-dontwarn kotlinx.coroutines.debug.**

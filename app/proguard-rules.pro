# Keep app models for Room and Serialization
-keep class com.example.anhpannt.data.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Supabase & Ktor
-dontwarn io.github.jan.supabase.**
-dontwarn io.ktor.**
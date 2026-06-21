# Preserve GSON-related data classes from obfuscation
-keep class com.kaspar.tuhat.GameData { *; }
-keep class com.kaspar.tuhat.Player { *; }
-keep class com.kaspar.tuhat.Round { *; }

# Keep GSON's @SerializedName annotation
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.annotations.SerializedName { *; }


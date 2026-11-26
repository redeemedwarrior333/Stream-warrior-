# Keep kotlinx serialization models
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.streamwarrior.app.**$$serializer { *; }
-keepclassmembers class com.streamwarrior.app.** { *** Companion; }
-keepclasseswithmembers class com.streamwarrior.app.** { kotlinx.serialization.KSerializer serializer(...); }

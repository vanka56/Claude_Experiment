# Keep kotlinx.serialization models used for Nearby Connections payloads.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.swingcricket.game.protocol.**$$serializer { *; }
-keepclassmembers class com.swingcricket.game.protocol.** {
    *** Companion;
}
-keepclasseswithmembers class com.swingcricket.game.protocol.** {
    kotlinx.serialization.KSerializer serializer(...);
}

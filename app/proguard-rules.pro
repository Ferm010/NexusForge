# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ============================================
# ЗАЩИТА ОТ ДЕКОМПИЛЯЦИИ И РЕВЕРС-ИНЖИНИРИНГА
# ============================================

# Агрессивная обфускация
-repackageclasses ''
-allowaccessmodification
-optimizationpasses 5

# Удалить отладочную информацию
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Скрыть имена исходных файлов
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Обфусцировать все классы приложения
-keep class com.example.nexusforge.MainActivity { *; }
-keep class com.example.nexusforge.backend.FirebaseConfig { *; }

# Firebase Authentication
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.example.nexusforge.**$$serializer { *; }
-keepclassmembers class com.example.nexusforge.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.nexusforge.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.compose.**

# Credential Manager
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# Защита от рефлексии
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Удалить метаданные Kotlin (усложняет декомпиляцию)
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

# Google Drive API
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.api.services.drive.**
-dontwarn javax.annotation.**
-dontwarn org.apache.http.**
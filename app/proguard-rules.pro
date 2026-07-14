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

# aboutlibraries does not ship its own consumer ProGuard rules, so keep its
# classes (and the kotlinx.serialization models it uses to parse the license
# JSON) from being optimized/stripped by R8.
-keep class com.mikepenz.aboutlibraries.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontwarn kotlinx.serialization.**
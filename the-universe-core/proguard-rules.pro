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

-keep class com.duplicateapp.theuniverse.** {*; }
-keep class top.niunaijun.jnihook.** {*; }
-keep class mirror.** {*; }
-keep class android.** {*; }
-keep class com.android.** {*; }

-keep class com.duplicateapp.theuniverse.reflection.** {*; }
-keep @com.duplicateapp.theuniverse.reflection.annotation.BClass class * {*;}
-keep @com.duplicateapp.theuniverse.reflection.annotation.BClassName class * {*;}
-keep @com.duplicateapp.theuniverse.reflection.annotation.BClassNameNotProcess class * {*;}
-keepclasseswithmembernames class * {
    @com.duplicateapp.theuniverse.reflection.annotation.BField.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BFieldNotProcess.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BFieldSetNotProcess.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BFieldCheckNotProcess.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BMethod.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BStaticField.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BStaticMethod.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BMethodCheckNotProcess.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BConstructor.* <methods>;
    @com.duplicateapp.theuniverse.reflection.annotation.BConstructorNotProcess.* <methods>;
}
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

-keep class com.dd.the.universe.** {*; }
-keep class mirror.** {*; }
-keep class android.** {*; }
-keep class com.android.** {*; }

-keep class com.dd.the.universe.reflection.** {*; }
-keep @com.dd.the.universe.reflection.annotation.BClass class * {*;}
-keep @com.dd.the.universe.reflection.annotation.BClassName class * {*;}
-keep @com.dd.the.universe.reflection.annotation.BClassNameNotProcess class * {*;}
-keepclasseswithmembernames class * {
    @com.dd.the.universe.reflection.annotation.BField.* <methods>;
    @com.dd.the.universe.reflection.annotation.BFieldNotProcess.* <methods>;
    @com.dd.the.universe.reflection.annotation.BFieldSetNotProcess.* <methods>;
    @com.dd.the.universe.reflection.annotation.BFieldCheckNotProcess.* <methods>;
    @com.dd.the.universe.reflection.annotation.BMethod.* <methods>;
    @com.dd.the.universe.reflection.annotation.BStaticField.* <methods>;
    @com.dd.the.universe.reflection.annotation.BStaticMethod.* <methods>;
    @com.dd.the.universe.reflection.annotation.BMethodCheckNotProcess.* <methods>;
    @com.dd.the.universe.reflection.annotation.BConstructor.* <methods>;
    @com.dd.the.universe.reflection.annotation.BConstructorNotProcess.* <methods>;
}
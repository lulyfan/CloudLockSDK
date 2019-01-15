# Add project specific ProGuard rules here.
# You can control the set1 of applied configuration files using the
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
-keep class com.ut.unilink.UnilinkManager { *; }
-keep class com.ut.unilink.cloudLock.CloudLock { *; }
-keep class com.ut.unilink.cloudLock.ScanDevice { *; }
-keep class com.ut.unilink.cloudLock.LockStateListener { *; }
-keep class com.ut.unilink.cloudLock.protocol.data.** { *; }
-keep class com.ut.unilink.util.Log { *; }

-keep interface com.ut.unilink.cloudLock.ScanListener { *; }
-keep interface com.ut.unilink.cloudLock.CallBack { *; }
-keep interface com.ut.unilink.cloudLock.ConnectListener { *; }

# ProGuard rules for Eyefind Browser

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application

# WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep Tor related classes
-keep class com.nxvus.eyefind.TorManager { *; }
-keep class com.nxvus.eyefind.model.** { *; }

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/leege100/software/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes *Annotation*
-keep public class cn.com.goluk.ipcsdk.bean.DownloadInfo
-keep public class cn.com.goluk.ipcsdk.bean.FileInfo
-keep public class cn.com.goluk.ipcsdk.bean.RecordStorageState
-keep public class cn.com.goluk.ipcsdk.bean.VideoInfo
-keep public class cn.com.goluk.ipcsdk.command.BaseIPCCommand
-keep public class cn.com.goluk.ipcsdk.command.IPCConfigCommand
-keep public class cn.com.goluk.ipcsdk.command.IPCConnCommand
-keep public class cn.com.goluk.ipcsdk.command.IPCFileCommand
-keep public class cn.com.goluk.ipcsdk.listener.IPCConfigListener
-keep public class cn.com.goluk.ipcsdk.listener.IPCConnListener
-keep public class cn.com.goluk.ipcsdk.listener.IPCFileListener
-keep public class cn.com.goluk.ipcsdk.listener.IPCInitListener
-keep public class cn.com.goluk.ipcsdk.main.GolukIPCSdk
-keep public class cn.com.goluk.ipcsdk.utils.GolukUtils
-keep public class cn.com.goluk.ipcsdk.utils.IpcDataParser

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

# keep setters in Views so that animations can still work.
# see http://proguard.sourceforge.net/manual/examples.html#beans
-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# We want to keep methods in Activity that could be used in the XML attribute onClick
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}
#
## The support library contains references to newer platform versions.
## Don't warn about those in case this app is linking against an older
## platform version.  We know about them, and they are safe.
#-dontwarn android.support.**//?????????????????????????????????

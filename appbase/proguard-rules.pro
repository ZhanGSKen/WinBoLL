# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
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

# ============================== 基础通用规则 ==============================
# 保留系统组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# 保留 WinBoLL 核心包及子类（适配你的两个包名）
#-keep public class * extends com.winboll.WinBoLLActivity
#-keep public class * extends com.winboll.WinBoLLFragment
# 主包名
-keep class cc.winboll.studio.*.** { *; }
# beta包名
-keep class cc.winboll.studio.*.beta.** { *; }
-keepclassmembers class cc.winboll.studio.*.** { *; }
-keepclassmembers class cc.winboll.studio.*.beta.** { *; }

# 保留所有类中的 public static final String TAG 字段
-keepclassmembers class * {
    public static final java.lang.String TAG;
}

# 保留序列化类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 保留 R 文件
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留 native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留注解和泛型
-keepattributes *Annotation*
-keepattributes Signature

# 屏蔽 Java 8+ 警告（适配 Java 7）
-dontwarn java.lang.invoke.*
-dontwarn android.support.v8.renderscript.*
-dontwarn java.util.function.**

# ============================== 第三方框架规则 ==============================
# Retrofit + OkHttp
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Glide 4.x
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$ImageType {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# GreenDAO 3.x
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
    public static java.lang.String TABLENAME;
}
-keep class **$Properties
# 实体类包名（按实际调整）
#-keep class cc.winboll.studio.appbase.model.** { *; }

# ButterKnife 8.x
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.BindView <fields>;
    @butterknife.OnClick <methods>;
}

# EventBus 3.x
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ============================== 优化与调试 ==============================
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-verbose
-dontpreverify
-dontusemixedcaseclassnames
# 保留行号（便于崩溃定位）
-keepattributes SourceFile,LineNumberTable


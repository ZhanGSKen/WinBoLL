# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# ============================== 基础通用规则 ==============================
# 保留系统组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

# 保留 WinBoLL 核心包及子类（合并简化规则）
-keep class cc.winboll.studio.** { *; }
-keepclassmembers class cc.winboll.studio.** { *; }

# 保留所有类中的 public static final String TAG 字段（便于日志定位）
-keepclassmembers class * {
    public static final java.lang.String TAG;
}

# 保留序列化类（避免Parcelable/Gson解析异常）
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

# 保留 R 文件（避免资源ID混淆）
-keepclassmembers class **.R$* {
    public static <fields>;
}

# 保留 native 方法（避免JNI调用失败）
-keepclasseswithmembernames class * {
    native <methods>;
}

# 保留注解和泛型（避免反射/序列化异常）
-keepattributes *Annotation*
-keepattributes Signature

# 屏蔽 Java 8+ 警告（适配 Java 7 语法）
-dontwarn java.lang.invoke.*
-dontwarn android.support.v8.renderscript.*
-dontwarn java.util.function.**

# ============================== 第三方框架专项规则 ==============================
# OkHttp 4.4.1（米盟广告请求依赖，完善Lambda兼容）
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okhttp3.internal.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.internal.platform.**
-dontwarn okio.**

# Glide 4.9.0（米盟广告图片加载依赖）
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$ImageType {
    **[] $VALUES;
    public *;
}
-keepclassmembers class * implements com.bumptech.glide.module.AppGlideModule {
    <init>();
}
-dontwarn com.bumptech.glide.**

# Gson 2.8.5（米盟广告数据序列化依赖）
-keep class com.google.gson.** { *; }
-keep interface com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 米盟 SDK（核心广告组件，完整保留避免加载失败）
-keep class com.miui.zeus.** { *; }
-keep interface com.miui.zeus.** { *; }
# 保留米盟日志字段（便于广告加载失败排查）
-keepclassmembers class com.miui.zeus.mimo.sdk.** {
    public static final java.lang.String TAG;
}

# RecyclerView 1.0.0（米盟广告布局渲染依赖）
-keep class androidx.recyclerview.** { *; }
-keep interface androidx.recyclerview.** { *; }
-keepclassmembers class androidx.recyclerview.widget.RecyclerView$Adapter {
    public *;
}

# 其他第三方框架（按引入依赖保留，无则可删除）
# XXPermissions 18.63
-keep class com.hjq.permissions.** { *; }
-keep interface com.hjq.permissions.** { *; }

# ZXing 二维码（核心解析组件）
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.zxing.** { *; }

# Jsoup HTML解析
-keep class org.jsoup.** { *; }

# Pinyin4j 拼音搜索
-keep class net.sourceforge.pinyin4j.** { *; }

# JSch SSH组件
-keep class com.jcraft.jsch.** { *; }

# AndroidX 基础组件
-keep class androidx.appcompat.** { *; }
-keep interface androidx.appcompat.** { *; }

# ============================== 优化与调试配置 ==============================
# 优化级别（平衡混淆效果与性能）
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# 调试辅助（保留行号便于崩溃定位）
-verbose
-dontpreverify
-dontusemixedcaseclassnames
-keepattributes SourceFile,LineNumberTable


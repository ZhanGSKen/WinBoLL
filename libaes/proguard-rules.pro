# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:/tools/adt-bundle-windows-x86_64-20131030/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# JavaMail SMTP（邮件发送核心库，Release编译必须保留）
-dontwarn javax.mail.**
-keep class javax.mail.** { *; }
-keep class javax.mail.internet.** { *; }
-dontwarn com.sun.mail.**
-keep class com.sun.mail.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

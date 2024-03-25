# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\PADMAVATHI\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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
#VMAX SDK RELATED PROGAURD
-keepattributes *Annotation*,JavascriptInterface,Exceptions,InnerClasses,Signature,*Annotation*,EnclosingMethod,*Annotation*,Signature

-dontwarn com.google.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

-keep public class com.vmax.android.ads.api.VmaxAdView {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxSdk {
    public <fields>;
    public <methods>;
}


-keep public class com.vmax.android.ads.api.VmaxAdSize {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.BitmapSampler {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.AdContainer {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.ImageLoader {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.NativeImageDownload {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.NativeImageDownloadListener {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxAdPartner {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxAdReward {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.common.VmaxAdListener {
    <fields>;
    <methods>;
}

-keep public class com.vmax.android.ads.common.User {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.exception.** {
    public <fields>;
    public <methods>;
}


-keep public class com.vmax.android.ads.exception.VmaxAdError {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.mediation.** {
    public <fields>;
    public <methods>;
    }
-keep class com.vmax.android.ads.mediation.partners.** {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.mediation.partners.VmaxAdPlayer {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.nativeads.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.nativeHelper.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.nativeview.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.CountryAttributes {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.CountryNames {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.Utility {
    <fields>;
    <methods>;
}

-keep public class com.vmax.android.ads.vast.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.webview.** {
    public <fields>;
    public <methods>;
}

 -keep public class com.google.android.gms.** {
    <fields>;
   <methods>;
}

-keep public class com.google.ads.** {
    public <fields>;
    public <methods>;
}

#-keep public class com.google.android.gms.common.GooglePlayServicesUtil {
#    public <fields>;
#   public <methods>;
#}
#
#-keep public class com.google.android.gms.common.ConnectionResult {
#    public <fields>;
#    public <methods>;
#}
#
#-keep public class com.google.android.gms.ads.identifier.AdvertisingIdClient {
#    public <fields>;
#    public <methods>;
#}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}


# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers class * {
    native <methods>;
}

-keep class com.google.firebase.** {
    public <fields>;
    public <methods>;
}

-keep public final class com.google.firebase.FirebaseOptions {
    public <fields>;
    public <methods>;
}

-keep public enum  com.vmax.android.ads.api.VmaxAdView$AdState {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep public enum  com.vmax.android.ads.api.VmaxSdk$Gender {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep public enum  com.vmax.android.ads.api.VmaxSdk$UserAge {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep public class com.vmax.android.ads.api.Section {
    public <fields>;
    public <methods>;
}

-keep public enum  com.vmax.android.ads.api.Section$** {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep,allowshrinking @com.google.android.gms.common.annotation.KeepName class *


#VMAX SDK RELATED PROGAURD
-keepattributes *Annotation*,JavascriptInterface,Exceptions,InnerClasses,Signature,*Annotation*,EnclosingMethod,*Annotation*,Signature

-dontwarn com.google.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

-keep public class com.vmax.android.ads.api.VmaxAdView {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxSdk {
    public <fields>;
    public <methods>;
}


-keep public class com.vmax.android.ads.api.VmaxAdSize {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.BitmapSampler {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.AdContainer {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.ImageLoader {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.NativeImageDownload {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.api.NativeImageDownloadListener {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxAdPartner {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxAdReward {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.common.VmaxAdListener {
    <fields>;
    <methods>;
}

-keep public class com.vmax.android.ads.common.User {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.exception.** {
    public <fields>;
    public <methods>;
}


-keep public class com.vmax.android.ads.exception.VmaxAdError {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.mediation.** {
    public <fields>;
    public <methods>;
    }
-keep class com.vmax.android.ads.mediation.partners.** {
    public <fields>;
    public <methods>;
}
-keep public class com.vmax.android.ads.mediation.partners.VmaxAdPlayer {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.nativeads.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.nativeHelper.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.nativeview.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.CountryAttributes {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.CountryNames {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.util.Utility {
    <fields>;
    <methods>;
}

-keep public class com.vmax.android.ads.vast.** {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.webview.** {
    public <fields>;
    public <methods>;
}

 -keep public class com.google.android.gms.** {
    <fields>;
   <methods>;
}

-keep public class com.google.ads.** {
    public <fields>;
    public <methods>;
}

#-keep public class com.google.android.gms.common.GooglePlayServicesUtil {
#    public <fields>;
#   public <methods>;
#}
#
#-keep public class com.google.android.gms.common.ConnectionResult {
#    public <fields>;
#    public <methods>;
#}
#
#-keep public class com.google.android.gms.ads.identifier.AdvertisingIdClient {
#    public <fields>;
#    public <methods>;
#}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}


# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers class * {
    native <methods>;
}

-keep class com.google.firebase.** {
    public <fields>;
    public <methods>;
}

-keep public final class com.google.firebase.FirebaseOptions {
    public <fields>;
    public <methods>;
}

-keep public enum  com.vmax.android.ads.api.VmaxAdView$AdState {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep public enum  com.vmax.android.ads.api.VmaxSdk$Gender {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep public enum  com.vmax.android.ads.api.VmaxSdk$UserAge {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep public class com.vmax.android.ads.api.Section {
    public <fields>;
    public <methods>;
}

-keep public enum  com.vmax.android.ads.api.Section$** {
   <fields>;
   public static **[] values();
   public static ** valueOf(java.lang.String);
}

-keep,allowshrinking @com.google.android.gms.common.annotation.KeepName class *


#Flurry Ad SDK Settings
-keep class com.flurry.**{ *; }
-dontwarn com.flurry**
-keepattributes *Annotation*,EnclosingMethod
-keepclasseswithmembers class * {
 public <init>(android.content.Context, android.util.AttributeSet, int);
}
#Facebook:
-dontwarn com.facebook.ads.internal.**
-keep class com.facebook.ads.**
#inmobi
-keepattributes SourceFile,LineNumberTable
-keep class com.inmobi.** { *; }
-dontwarn com.inmobi.**
-dontwarn com.squareup.picasso.**
 
# skip the Picasso library classes
-keep class com.squareup.picasso.** {*;}
-dontwarn com.squareup.picasso.**
-dontwarn com.squareup.okhttp.**
# skip Moat classes
-keep class com.moat.** {*;}
-dontwarn com.moat.**
-keep class com.moat.** {*;}
-dontwarn com.moat.**
# skip comscore classes
-keep class com.comscore.** { *; }
-dontwarn com.comscore.**

 # Google Play Services library
  -keep class * extends java.util.ListResourceBundle {
   protected Object[][] getContents();
}

 -keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
  public static final *** NULL;
 }

 -keepnames @com.google.android.gms.common.annotation.KeepName class *
 -keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
  }

 -keepnames class * implements android.os.Parcelable {
  public static final ** CREATOR;
 }
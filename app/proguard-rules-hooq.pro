# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# Generic Settings
-ignorewarnings
-dontwarn sun.**
-dontwarn javax.**
-dontwarn java.awt.**
-dontwarn org.apache.**
-dontwarn java.lang.invoke**
-dontpreverify


#############################################################################################
## Generic Global Settings for Android

-keepattributes *Annotation*,Signature,InnerClasses,SourceFile,LineNumberTable,EnclosingMethod

# com.example.android.apis.animation.ShapeHolder,...
-keepclassmembers class **animation**Holder {
    public *** get*();
    public void set*(***);
}

-keepclassmembers !abstract class !com.google.ads.** extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclassmembers !abstract class * {
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-dontnote android.webkit.JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Renderscript support library.
-dontwarn android.os.SystemProperties
-dontwarn android.renderscript.RenderScript


# Ignore references to removed R classes.
-dontwarn android.support.**.R
-dontwarn android.support.**.R$*

# Ignore dynamic references and descriptor classes in support classes.
-dontnote android.support.**

# Ignore references from compatibility support classes to missing classes.
-dontwarn
    android.support.**Compat*,
    android.support.**Honeycomb*,
    android.support.**Jellybean*,
    android.support.**JB*,
    android.support.**Kitkat*,
    android.support.**19,
    android.support.**21,
    android.support.v7.internal.**,
    android.support.v7.widget.Toolbar,
    android.app.Notification$Builder
    
# Avoid merging and inlining compatibility classes.
-keep,allowshrinking,allowobfuscation class
    android.support.**Compat*,
    android.support.**Honeycomb*,
    android.support.**Jellybean*,
    android.support.**JB*,
    android.support.**Kitkat*,
    android.support.**19,
    android.support.**21
        { *; }


# Design support library.
-keepnames class android.support.design.widget.CoordinatorLayout
-keep !abstract class android.support.design.widget.* implements android.support.design.widget.CoordinatorLayout$Behavior {
    <init>(android.content.Context, android.util.AttributeSet);
}

-keepclassmembers,allowshrinking,allowobfuscation class android.support.design.widget.NavigationView {
    android.support.design.internal.NavigationMenuPresenter mPresenter;
}

-keepclassmembers,allowshrinking,allowobfuscation class android.support.design.widget.FloatingActionButton {
    android.support.design.widget.FloatingActionButtonImpl mImpl;
}

# Signature optimized with class from API level 19 or higher.
-keep,allowshrinking,allowobfuscation class android.support.v4.app.FragmentState$InstantiationException {
    <init>(...);
}

# Fields accessed before initialization.
-keepclassmembers,allowshrinking,allowobfuscation class android.support.v7.widget.GridLayout {
    ** *;
}

#############################################################################################
## Third Party Libraries that we need to filter 

######
# Google Play Services.
-dontwarn com.google.android.gms.**
-dontnote com.google.android.gms.**
-keep,allowshrinking class com.google.android.gms.location.ActivityRecognitionResult
-keep,allowshrinking class com.google.android.gms.maps.GoogleMapOptions

-keepclassmembers class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final java.lang.String NULL;
}

-keep,allowobfuscation @interface com.google.android.gms.common.annotation.KeepName
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Google Play market License Verification Library.
-dontnote com.android.vending.licensing.ILicensingService
-keep,allowobfuscation public class com.android.vending.licensing.ILicensingService


######
# GSON.
-keep,allowobfuscation @interface com.google.gson.annotations.*

-dontnote com.google.gson.annotations.Expose
-keepclassmembers class * {
    @com.google.gson.annotations.Expose <fields>;
}

-keepclasseswithmembers,allowobfuscation,includedescriptorclasses class * {
    @com.google.gson.annotations.Expose <fields>;
}

-dontnote com.google.gson.annotations.SerializedName
-keepclasseswithmembers,allowobfuscation,includedescriptorclasses class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

-keepclassmembers enum * {
    @com.google.gson.annotations.SerializedName <fields>;
}


#############################################################################################
## Language Settings 

# Enumerations.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Maintain java native methods / JNI Info
-keepclasseswithmembernames class * {
    native <methods>;
}

# Serializable classes.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


# When not preverifing in a case-insensitive filing system, such as Windows.
# Because this tool unpacks your processed jars, you should then use:
-dontusemixedcaseclassnames

# Some package required for the manifest file.
-allowaccessmodification

#############################################################################################
## VSTB Specific Settings
# NOTE: VSTB is shipped in two versions, a Debug build and a Release build.
# Both builds are already obfuscated. If desired you can skip obfuscating the VSTB Library
# The Exposed, and Plugin packages are not obfuscated, but core internal stuff is. If you wish
# you may obfuscate the entire app.

# All Core Implementations requires a serialVersionUID today, but they are NOT serializable.
-keepclassmembers class * implements com.quickplay.core.config.exposed.Core {
    static final long serialVersionUID;
}

# Keep Anything that is annotated with JNICallback or JNIVariable
-keepclasseswithmembers class * {
  @com.quickplay.cpp.exposed.annotation.JNICallback <methods>;
  @com.quickplay.cpp.exposed.annotation.JNIVariable <fields>; 
}

# Keep following classes unobfuscated as we use JNI to find them when using
-keep class com.quickplay.cpp.exposed.error.cpp.CPPNativeError {
	*;
}

-keep class com.quickplay.cpp.exposed.error.cpp.CPPNativeNetworkError {
	*;
}

# Maintain Native Handles - some of of our internal JNI ties together with 'native handle'.
# Deprecated - will not be required with CPPCore 1.1.0 (Will be using above annotations instead)
-keepclasseswithmembernames class * {
    private long nativeHandle;
}
-keepclasseswithmembernames class * {
    *** nativeCallback*(...);
}

# Keep all Event Reporting Classes - Event Reporting today uses reflection and we do not have
# annotations on all events at this time. So for now its best to avoid obfuscating events.
-keep class com.quickplay.vstb.eventlogger.exposed.client.events.** { *; }
-keep class com.quickplay.vstb.eventlogger.hidden.events.** { *; }

# Depending upon Third-Party Plugin being used, we might be unable to obfuscate for various reasons.
# See Below.
-keep class com.visualon.** { *; }
-keep class com.insidesecure.** { *; }
-keep class com.nexstreaming.nexplayerengine.** { *; }
-dontwarn com.insidesecure.**
-dontwarn  com.quickplay.**
-keep class com.quickplay.** {*;}
################################################################################
## VSTB Sample App Rules
#
# The rules below are strictly for the VSTB Sample Application and
# should not be used in your application.
#
# For flexibility with our alternative plugins, the VSTB Sample application
# makes heavy use of reflection via strings configured in a JSON file. As a
# result, we need to prevent obfuscation of many classes. Our obfuscation of
# the sample app is meant to demonstrate how to obfuscate with VSTB, if desired.
# It is not suitable for your application without modification.
#
# NOTE: VSTB itself comes pre-obfuscated.

-keep class com.quickplay.vstb.authentication.** { *; }
-keep class com.quickplay.vstb.ref.plugin.chromecast.** { *; }


#VMAX



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
#skip Comscore classes
-keep class com.comscore.** { *; }
-dontwarn com.comscore.**

#Base Sdk

-keepattributes *Annotation*,JavascriptInterface,Exceptions,InnerClasses,Signature,*Annotation*,EnclosingMethod,*Annotation*,Signature

#If Google IMA partner not integrated
-dontwarn com.google.ads.**
#If Rewarded Interstitial Ad Format not integrated
-dontwarn com.google.firebase.**
#If AdMob not integrated
-dontwarn com.google.android.gms.**

-keep public class com.vmax.android.ads.api.VmaxAdView {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxSdk {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxAdSettings {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.VmaxAdSize {
    public <fields>;
    public <methods>;
}

-keep public class com.vmax.android.ads.api.ViewMandatoryListener {
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

-keep public enum com.vmax.android.ads.api.VmaxAdView$AdState {
  <fields>;
  public static **[] values();
  public static ** valueOf(java.lang.String);
}

-keep public class com.google.ads.** {
    public <fields>;
    public <methods>;
}

-keep public class com.google.android.gms.** {
    <fields>;
    <methods>;
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keep,allowshrinking @com.google.android.gms.common.annotation.KeepName class *
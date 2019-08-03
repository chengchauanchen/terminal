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

#---------------------------------基本指令区----------------------------------
-ignorewarnings
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志
-printmapping proguardMapping.txt
-optimizationpasses 5   # 指定代码的压缩级别
-dontskipnonpubliclibraryclassmembers


-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes *Annotation*,InnerClasses
-keepattributes Signature # 避免混淆泛型
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable #运行抛出异常时保留代码行号
-keepattributes Exceptions # 解决AGPBI警告

#继承自activity,application,service,broadcastReceiver,contentprovider....不进行混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.support.multidex.MultiDexApplication
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService
-keep class android.support.** {*;}

# 所有View的子类及其子类的get、set方法都不进行混淆
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 这个主要是在layout 中写的onclick方法android:onclick="onClick"，不进行混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}


# 对于带有回调函数onXXEvent的，不能被混淆
-keepclassmembers class * {
    void *(*Event);
}

# 枚举类不能被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# natvie 方法不混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}


# 不混淆R类里及其所有内部static类中的所有static变量字段，$是用来分割内嵌类与其母体的标志
-keep public class **.R$*{
   public static final int *;
}


#（可选）避免Log打印输出
-assumenosideeffects class android.util.Log {
   public static *** v(...);
   public static *** d(...);
   public static *** i(...);
   public static *** w(...);
 }

#---------------------------------webview------------------------------------
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}

#---------------------------------业务组件实体类---------------------------------
# 确保所有实体类都继承于Serializable、Parcelable则不用配置
# 保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


#不混淆Serializable接口的子类中指定的某些成员变量和方法
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}


#---------------------------------第三方库及jar包-------------------------------

#rxJava
-keep class io.reactivex.* {*;}
-dontwarn io.reactivex.*

#OkHttp3
-dontwarn okhttp3.logging.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature-keepattributes Exceptions

# Gson
-keep class com.google.gson.stream.** { *; }
-keepattributes EnclosingMethod

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# for DexGuard only
-keepresourcexmlelements manifest/application/meta-data@value=GlideModule

#AndroidEventBus
-keep class org.simple.** { *; }
-keep interface org.simple.** { *; }
-keepclassmembers class * {
    @org.simple.eventbus.Subscriber <methods>;
}
-keepattributes *Annotation*

# butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }

-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}

-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#https://github.com/gyf-dev/ImmersionBar
#android 4.4以上沉浸式状态栏和沉浸式导航栏管理
-keep class com.gyf.barlibrary.* {*;}

# okdownload
-keep class com.liulishuo.okdownload.** { *; }
-dontwarn com.liulishuo.okdownload.**

#----------------------------Common-Res--------------------------------------
# 圆形图片
-keep class com.makeramen.** { *; }
-dontwarn com.makeramen.**

# 验证码
-keep class com.jyn.verificationcodeview.** { *; }
-dontwarn com.jyn.verificationcodeview.**

# 渠道id获取
-keep class com.leon.channel.** { *; }
-dontwarn com.leon.channel.**

# 轮播图
-keep class com.alibaba.android.** { *; }
-dontwarn com.alibaba.android.**

# 轮播图
-keep class com.alibaba.android.** { *; }
-dontwarn com.alibaba.android.**

# 极光推送
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.service.JPushMessageReceiver { *; }

-dontwarn cn.jpush.im.**
-keep class cn.jpush.im.** { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

-dontwarn cn.jiguang.analytics.**
-keep class cn.jiguang.analytics.** { *; }

-dontwarn cn.jiguang.share.**
-keep class cn.jiguang.share.** { * ;}



# tab+viewPage FlycoTabLayout_Lib
-keep class com.flyco.tablayout.** { *; }
-dontwarn com.flyco.tablayout.**


# tab+viewPage FlycoTabLayout_Lib
-keep class cn.aigestudio.wheelpicker.** { *; }
-dontwarn cn.aigestudio.wheelpicker.**

# greendao
-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

# 抽屉滑动效果
-keep class com.sothree.slidinguppanel.** { *; }
-dontwarn com.sothree.slidinguppanel.**

# 时间选择器
-keep class com.contrarywind.** { *; }
-dontwarn com.contrarywind.**

# okgo
-keep class com.lzy.net.** { *; }
-dontwarn com.lzy.net.**

# 拍照、相册
-keep class com.github.wildma.** { *; }
-dontwarn com.github.wildma.**

#------------------------------------Module-Home-----------------------------------------

# expandablelayout
-keep class net.cachapa.expandablelayout.** { *; }
-dontwarn net.cachapa.expandablelayout.**


#------------------------------------Module-Pay-----------------------------------------
# zxing
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**


#-------------------------------------SDK-Code-Ali-----------------------------------------
#支付宝安全支付
-dontwarn com.alipay.android.app.**
-keep class com.alipay.android.app.** { *; }

-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}

-dontwarn com.alipay.sdk.**
-keep class com.alipay.sdk.** { *; }

# alipay Inside
-dontwarn com.ali.user.mobile.**
-keep class com.ali.user.mobile.** { *; }

-dontwarn com.alibaba.wireless.security.**
-keep class com.alibaba.wireless.security.** { *; }

-dontwarn com.alipay.**
-keep class com.alipay.** { *; }

-dontwarn com.squareup.wire.**
-keep class com.squareup.wire.** { *; }

-dontwarn com.ta.utdid2.**
-keep class com.ta.utdid2.** { *; }

-dontwarn com.ut.device.**
-keep class com.ut.device.** { *; }

-dontwarn org.json.alipay.inside.**
-keep class org.json.alipay.inside.** { *; }


#-------------------------------------SDK-Code-Gold-----------------------------------------
#金码sdk
-dontwarn com.goldencode.lib.**
-keep class com.goldencode.lib.** {*;}

#-------------------------------------SDK-Code-Self-----------------------------------------

#自发码SDK
-dontwarn com.xiaoma.TQR.ridingcodelib.**
-keep class com.xiaoma.TQR.ridingcodelib.** { *; }

-dontwarn com.xiaoma.TQR.accountcodelib.**
-keep class com.xiaoma.TQR.accountcodelib.** { *; }

-dontwarn com.xiaoma.TQR.couponlib.**
-keep class com.xiaoma.TQR.couponlib.** { *; }

#-------------------------------------SDK-GaodeMap-----------------------------------------
#高德地图
-dontwarn com.amap.api.**
-keep class com.amap.api.** { *; }

#滑动点
-dontwarn com.github.LinweiJ.**
-keep class com.github.LinweiJ.** { *; }


#-------------------------------------SDK-Pay-----------------------------------------
#银联
-dontwarn com.unionpay.**
-keep class com.unionpay.** { *; }

#微信支付
-dontwarn com.tencent.**
-keep class com.tencent.** { *; }
-dontwarn com.tencent.smtt.sdk.**
-keep class com.tencent.smtt.sdk.** { *; }

#招行
-dontwarn com.ccb.**
-keep class com.ccb.** { *; }












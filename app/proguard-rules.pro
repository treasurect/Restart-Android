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

# 保留泛型签名及注解
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod

# 保留 Retrofit API 接口及其所有方法
-keep interface com.treasure.restart.network.api.** { *; }

# 保留响应类及其泛型信息
-keep class com.treasure.basic.network.BaseResponse { *; }

# 保留业务数据模型
-keep class com.treasure.restart.bean.** { *; }
-keep class com.treasure.restart.bean.**$* { *; }
-keep class com.treasure.basic.bean.** { *; }
-keep class com.treasure.basic.bean.**$* { *; }

# R8 full mode 会删除未 keep 类型的泛型签名。
# Retrofit 解析 suspend 函数返回值时需要读取 Continuation 上的泛型参数。
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# 保留 Gson 自定义适配器
-keep class com.treasure.basic.network.BaseResponseTypeAdapterFactory { *; }
-keep class com.treasure.basic.network.BaseResponseAdapter { *; }

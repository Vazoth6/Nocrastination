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

# Retrofit e Gson
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Retrofit usa reflection em métodos
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Mantenha classes do Retrofit
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Para classes de modelo (Response/Request)
-keep class pt.ipt.dam2025.nocrastination.data.datasource.remote.models.** { *; }

# Se estiver usando Kotlin, adicione:
-keep class kotlin.** { *; }
-dontwarn kotlin.**

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Mantenha suas classes de API
-keep class pt.ipt.dam2025.nocrastination.data.datasource.remote.api.** { *; }

# Mantenha classes de resposta/login
-keep class pt.ipt.dam2025.nocrastination.data.datasource.remote.models.responses.** { *; }
-keep class pt.ipt.dam2025.nocrastination.data.datasource.remote.models.requests.** { *; }

# Mantenha a classe de erro específica
-keep class * implements retrofit2.Call { *; }

# Para problemas com reflection em tipos genéricos
-keepattributes Signature

# Mantenha suas classes de domínio
-keep class pt.ipt.dam2025.nocrastination.domain.models.** { *; }

# Mantenha classes com anotações
-keep @interface pt.ipt.dam2025.nocrastination.** { *; }

# Se estiver usando Koin/Injeção de Dependência
-keep class org.koin.** { *; }
-dontwarn org.koin.**
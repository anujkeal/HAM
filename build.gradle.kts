plugins {
 id("com.android.application") version "8.13.2" apply false
 id("com.android.library") version "8.13.2" apply false
 id("org.jetbrains.kotlin.android") version "2.0.21" apply false
 id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
 id("org.jetbrains.kotlin.kapt") version "2.0.21" apply false
}
extra["compileSdkVersion"] = 36
extra["minSdk"] = 26
extra["targetSdkVersion"] = 28
extra["versionCode"] = 1
extra["versionName"] = "0.1.0"

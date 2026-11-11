plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.android")
 id("org.jetbrains.kotlin.plugin.compose")
 id("org.jetbrains.kotlin.kapt")
}
android {
 namespace = "com.keal.ham"
 compileSdk = 36
 defaultConfig { applicationId = "com.keal.ham"; minSdk = 26; targetSdk = 28; versionCode = 1; versionName = "0.1.0" }
 buildFeatures { compose = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 packaging { jniLibs { useLegacyPackaging = true } }
}
kapt { correctErrorTypes = true }
dependencies {
 implementation(project(":Bcore"))
 implementation("androidx.core:core-ktx:1.15.0")
 implementation("androidx.activity:activity-compose:1.10.0")
 implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.compose.ui:ui:1.7.6")
 implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
 implementation("androidx.compose.material3:material3:1.3.1")
 implementation("androidx.compose.material:material-icons-extended:1.7.6")
 implementation("androidx.compose.foundation:foundation:1.7.6")
 implementation("androidx.room:room-runtime:2.6.1")
 implementation("androidx.room:room-ktx:2.6.1")
 kapt("androidx.room:room-compiler:2.6.1")
 debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
 testImplementation("junit:junit:4.13.2")
}

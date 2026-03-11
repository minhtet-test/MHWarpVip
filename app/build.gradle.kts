plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mhwarp.vip"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mhwarp.vip"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // ဤစာကြောင်းသည် app/libs/ ထဲက aar အားလုံးကို ဖတ်ခိုင်းခြင်းဖြစ်သည်
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
    
    // ကျန်တာတွေက အရင်အတိုင်း...
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

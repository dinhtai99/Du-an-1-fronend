plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "fpoly.haideptrai.duan1"
    compileSdk = 34

    defaultConfig {
        applicationId = "fpoly.haideptrai.duan1"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation(libs.cardview)
    
    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    
    // Glide
    implementation(libs.glide)
    
    // MPAndroidChart
    implementation(libs.mpandroidchart)
    
    // Apache POI
    implementation(libs.poi)
    implementation(libs.poi.ooxml)
    
    // Retrofit & OkHttp for API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)


    // ZaloPay SDK từ libs folder
    implementation(files("libs/zpdk-release-v3.1.aar"))


    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // ZaloPay SDK - COMMENT TẠM THỜI VÌ KHÔNG TẢI ĐƯỢC TỪ REPOSITORY
    // TODO: Tải SDK thủ công hoặc tìm repository khác
    // implementation("vn.zalopay.sdk:zp-sdk:3.1.0")
    
    // MoMo Payment SDK
    // Thử version 1.0.6 trước, nếu không được thì tải SDK thủ công
    // Exclude Support Library để tránh xung đột với AndroidX
    implementation("com.github.momo-wallet:mobile-sdk:1.0.6") {
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-v4")
    }
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
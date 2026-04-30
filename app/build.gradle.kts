plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinx.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "hf.inner.notebook"
    compileSdk = 36

    defaultConfig {
        applicationId = "hf.inner.notebook"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // hilt
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)

    // hilt navigation
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.splashscreen)
    // navigation component
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.material.icons.extended)
    // salt
    implementation(libs.salt.ui)
    // room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.serialization.json)

    // coil
    implementation(libs.coil.compose)

    // markdown
    implementation(libs.markdown.core)
    implementation(libs.markdown.tables)
    implementation(libs.markdown.strikethrough)
    implementation(libs.markdown.html)
    implementation(libs.markdown.linkify)
    implementation(libs.narkdown.jeziellago)

    implementation(libs.adnroidx.appcompat)

    // calendar
    implementation(libs.calendar.compose)

    implementation(libs.google.material)
    // zip
    implementation(libs.zt.zip)

    // sardine-android
    implementation(libs.sardine.android)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // biometric
    implementation(libs.androidx.biometric)

    // jsoup
    implementation(libs.jsoup)
}
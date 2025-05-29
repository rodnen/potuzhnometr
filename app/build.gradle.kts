import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.potuzhnometr"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.potuzhnometr"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

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

    applicationVariants.configureEach {
        val appName = "Potuzhnometr"
        val vName = versionName
        val bType = buildType.name

        outputs.configureEach {
            val apkOutput = this as BaseVariantOutputImpl
            if (bType == "release") {
                apkOutput.outputFileName = "${appName}App_v${vName}.apk"
            }
            if (bType == "debug") {
                apkOutput.outputFileName = "${appName}AppDev_v${vName}.apk"
            }
        }
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.lottie)
    implementation (libs.gson)
    implementation (libs.okhttp)
    implementation(libs.androidx.lifecycle.process)
    implementation (libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.cronet.embedded)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
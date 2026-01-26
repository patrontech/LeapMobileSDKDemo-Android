plugins {
    alias(libs.plugins.conventions.kiba.app)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "com.example.kibasdkpoc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.kibasdkpoc"
        minSdk = 29
        targetSdk = 36
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
        create("prod") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.serializationCore)
    implementation(libs.google.servicesLocation)
    implementation(libs.androidx.fragmentKtx)
    implementation(libs.material)

    // Compose UI dependencies
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.compose.material3)

    // Leap Mobile SDK
    implementation(libs.leapmobilesdk)
    implementation(libs.qrScanner)

    // SDKs needed to make the app properly run
    implementation(libs.retrofit)
    implementation(libs.retrofitSerializationConverter)
    implementation(libs.okHttp)
    implementation(libs.okHttpLoggingInterceptor)
    implementation(libs.androidx.security)
    implementation(libs.androidx.datastore)
    implementation(libs.kotlinReflect)
    implementation(libs.androidx.work)
    implementation(libs.zip4j)
    implementation(libs.kiba.parsimonious)
    implementation(libs.komposable)
}

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
    kotlinOptions {
        jvmTarget = "17"
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

    implementation(project(":core"))
    implementation(project(":event"))
    implementation(project(":interfacekit"))
    implementation(project(":maps"))
    implementation(project(":thuzi"))
    implementation(project(":ticketing"))
    implementation(project(":toolkit"))
}

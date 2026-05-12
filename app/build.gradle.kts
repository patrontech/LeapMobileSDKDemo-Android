import com.android.build.gradle.internal.api.BaseVariantOutputImpl

plugins {
    alias(libs.plugins.conventions.kiba.app)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinCompose)
}

val leapSdkVersion: String = libs.versions.leapmobilesdk.get()

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
		debug {
			isMinifyEnabled = false

			signingConfig = signingConfigs.getByName("debug")
			matchingFallbacks += listOf("debug")
		}
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            matchingFallbacks += listOf("release")
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
		buildConfig = true
        compose = true
        viewBinding = true
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            (this as BaseVariantOutputImpl).outputFileName =
                "LeapDemo-sdk${leapSdkVersion}-${name}.apk"
        }
    }
}

dependencies {
    implementation(libs.serializationCore)
    implementation(libs.google.servicesLocation)
    implementation(libs.androidx.fragmentKtx)
    implementation(libs.material)

    implementation(libs.leapmobilesdk.core)
    // Fanatics bundle (same version as mobile-sdk-core). Remove if the app provides assets under content/ itself.
    implementation(libs.leapmobilesdk.fanaticsBundle)
    implementation(libs.qrScanner)

    // Compose UI dependencies
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.compose.fragment)
    implementation(libs.compose.navigation)
    implementation(libs.compose.lifecycleViewModel)
    implementation(libs.androidx.appCompat)

    // Webkit
    implementation(libs.androidx.webkit)

	// Compose UI testing
	androidTestImplementation(platform(libs.compose.bom))
	androidTestImplementation(libs.test.composeUiJunit4)
	debugImplementation(libs.test.composeManifest)
}

configurations.all {
	resolutionStrategy {
		force("androidx.webkit:webkit:1.11.0")
	}
}

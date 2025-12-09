plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
}

android {
    buildFeatures.buildConfig = true

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro", "coroutines.pro")
    }

    buildTypes {
        getByName("release") {
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    namespace = "com.greencopper.toolkit"
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))

    implementation(libs.coroutinesAndroid)
    implementation(libs.kotlinReflect)

    // Http Client
    implementation(libs.okHttp)
    implementation(libs.okHttpLoggingInterceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofitSerializationConverter)

    // Zip
    implementation(libs.zip4j)

    // Local storage
    implementation(libs.androidx.security)

    // Unit testing
    testImplementation(project(LibraryTestLibs.kibaTestMocks))

    // Instrumentation testing
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
}

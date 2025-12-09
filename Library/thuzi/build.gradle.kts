plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
    alias(libs.plugins.conventions.kiba.library.uibasics)
    alias(libs.plugins.conventions.compose)
    alias(libs.plugins.kotlinCompose)
}

android {
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

    namespace = "com.greencopper.thuzi"
}

dependencies {
    implementation(libs.zxing)
    implementation(libs.retrofit)
    implementation(libs.okHttpLoggingInterceptor)
    implementation(libs.komposable)

    // KIBA
    implementation(project(LibraryLibs.kibaToolkit))
    implementation(project(LibraryLibs.kibaCore))
    implementation(project(LibraryLibs.kibaInterfaceKit))

    // Third Party
    implementation(libs.qrScanner)

    // Unit testing
    testImplementation(project(LibraryTestLibs.kibaTestMocks))
    testImplementation(project(LibraryTestLibs.coreTestMocks))
    testImplementation(libs.test.komposable)

    // Instrumentation testing
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
    androidTestImplementation(project(LibraryTestLibs.coreTestMocks))
}

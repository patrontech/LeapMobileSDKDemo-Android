plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
    alias(libs.plugins.conventions.kiba.library.uibasics)
    alias(libs.plugins.conventions.compose)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.kotlinParcelize.get().pluginId)
    alias(libs.plugins.kotlinCompose)
}

android {
    namespace = "com.greencopper.interfacekit"

    defaultConfig {
        testApplicationId = "com.greencopper.interfacekittest"
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

}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(libs.google.appReview)
    implementation(libs.google.appReviewKtx)
    implementation(libs.androidx.datastore)
    implementation(libs.komposable)
    implementation(libs.retrofit)
    implementation(libs.retrofitSerializationConverter)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)

    // Encoding
    implementation(libs.zxing)

    // KIBA
    implementation(project(LibraryLibs.kibaToolkit))
    implementation(project(LibraryLibs.kibaCore))
    implementation(libs.kiba.parsimonious)

    // Image loading
    implementation(libs.sharpSVG)

    //Fuzzy Search
    implementation(libs.fuzzywuzzy)

    // Unit testing
    testImplementation(project(LibraryTestLibs.kibaTestMocks))
    testImplementation(project(LibraryTestLibs.coreTestMocks))
    testImplementation(libs.test.komposable)

    // Instrumentation testing
    // This is due to a bug: https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-test-doubles#7
    implementation(libs.test.androidxCore)
    implementation(libs.test.androidFragment)

    androidTestImplementation(libs.test.androidxKtxCore)
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
    androidTestImplementation(project(LibraryTestLibs.coreTestMocks))
    androidTestImplementation(libs.test.composeJunit5)
    debugImplementation(libs.test.composeManifest)
}

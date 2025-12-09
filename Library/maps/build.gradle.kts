plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
    alias(libs.plugins.conventions.kiba.library.uibasics)
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

    namespace = "com.greencopper.kiba_maps"
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))

    // KIBA
    implementation(project(LibraryLibs.kibaToolkit))
    implementation(project(LibraryLibs.kibaCore))
    implementation(project(LibraryLibs.kibaInterfaceKit))

    // UI
    implementation(libs.google.maps)
    implementation(libs.google.mapsUtil)
    implementation(libs.google.mapsKt)
    implementation(libs.google.mapsUtilKtx)

    // Unit testing
    testImplementation(project(LibraryTestLibs.kibaTestMocks))
    testImplementation(project(LibraryTestLibs.eventTestMocks))
    testImplementation(project(LibraryTestLibs.mapsTestMocks))

    // Instrumentation testing
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
    androidTestImplementation(project(LibraryTestLibs.mapsTestMocks))
}

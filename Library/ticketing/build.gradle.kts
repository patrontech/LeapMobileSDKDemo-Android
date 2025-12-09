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

    namespace = "com.greencopper.ticketing"
}

dependencies {

implementation(libs.retrofit)
    implementation(libs.retrofitSerializationConverter)

    // KIBA
    implementation(project(LibraryLibs.kibaToolkit))
    implementation(project(LibraryLibs.kibaCore))
    implementation(project(LibraryLibs.kibaInterfaceKit))

    // Unit testing
    testImplementation(project(LibraryTestLibs.kibaTestMocks))
    testImplementation(project(LibraryTestLibs.coreTestMocks))
    testImplementation(project(LibraryTestLibs.ticketingTestMocks))

    // Instrumentation testing
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
    androidTestImplementation(project(LibraryTestLibs.coreTestMocks))
    androidTestImplementation(project(LibraryTestLibs.ticketingTestMocks))
}

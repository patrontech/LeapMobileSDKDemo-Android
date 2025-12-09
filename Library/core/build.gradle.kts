plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro", "coroutines.pro")
    }


    namespace = "com.greencopper.core"
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.hamcrest:hamcrest-core:1.3")).using(module("junit:junit:4.10"))
        }
    }
}

dependencies {
    implementation(project(":toolkit"))
    implementation(libs.kiba.parsimonious)
    implementation(libs.kotlinReflect)
    implementation(libs.androidx.security)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(libs.retrofit)
    implementation(libs.retrofitSerializationConverter)

    // Unit testing
    testImplementation(project(LibraryTestLibs.kibaTestMocks))
    testImplementation(project(LibraryTestLibs.coreTestMocks))

    // Instrumentation testing
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
    androidTestImplementation(project(LibraryTestLibs.coreTestMocks))
}

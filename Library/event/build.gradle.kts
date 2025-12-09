plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
    alias(libs.plugins.conventions.kiba.library.uibasics)
    alias(libs.plugins.conventions.compose)
    id(libs.plugins.ksp.get().pluginId)
    id(libs.plugins.kotlinParcelize.get().pluginId)
    alias(libs.plugins.kotlinCompose)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
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

    testOptions {
        unitTests.all {
            // https://github.com/mockk/mockk/issues/681
            it.jvmArgs("--add-opens", "java.base/java.time=ALL-UNNAMED")
        }
    }

    namespace = "com.greencopper.event"
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to listOf("*.jar")))
    implementation(libs.androidx.work)
    implementation(libs.komposable)

    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)

    // KIBA
    implementation(project(LibraryLibs.kibaToolkit))
    implementation(project(LibraryLibs.kibaCore))
    implementation(project(LibraryLibs.kibaInterfaceKit))

    // Unit testing
    testImplementation(libs.test.komposable)
    testImplementation(project(LibraryTestLibs.eventTestMocks))
    testImplementation(project(LibraryTestLibs.kibaTestMocks))
    testImplementation(project(LibraryTestLibs.coreTestMocks))

    // Instrumentation testing
    androidTestImplementation(libs.test.androidx.workTesting)
    androidTestImplementation(project(LibraryTestLibs.kibaTestMocks))
    androidTestImplementation(project(LibraryTestLibs.eventTestMocks))
    androidTestImplementation(project(LibraryTestLibs.coreTestMocks))
}

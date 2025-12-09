plugins {
    alias(libs.plugins.conventions.kiba.library.basics)
    alias(libs.plugins.conventions.kiba.library.uibasics)
    alias(libs.plugins.conventions.mock.module)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro", "coroutines.pro")
    }
    namespace = "com.greencopper.mapsmocks"
}

dependencies {
    implementation(project(LibraryLibs.kibaCore))
    implementation(project(LibraryLibs.kibaInterfaceKit))
    implementation(project(LibraryLibs.kibaMaps))
}

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named("clean") {
    mustRunAfter(tasks.matching { it.name != "clean" })
}

dependencies {
    implementation(libs.gradle)
    implementation(libs.kotlinGradle)
    implementation(libs.kotlin.serialization)

    implementation(gradleApi())
}

gradlePlugin {
    plugins {
        register("kibaApp") {
            id = "conventions.kiba.app"
            implementationClass = "KIBAAppConventionPlugin"
        }
        register("kibaLibraryBasics") {
            id = "conventions.kiba.library.basics"
            implementationClass = "KIBALibraryBasicsConventionPlugin"
        }
        register("kibaLibraryUIBasics") {
            id = "conventions.kiba.library.uibasics"
            implementationClass = "KIBALibraryUIBasicsConventionPlugin"
        }
        register("mockModule") {
            id = "conventions.mock.module"
            implementationClass = "MockModuleConventionPlugin"
        }
        register("composePlugin") {
            id = "conventions.compose"
            implementationClass = "ComposeConventionPlugin"
        }
    }
}

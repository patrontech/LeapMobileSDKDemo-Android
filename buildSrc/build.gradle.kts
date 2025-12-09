import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}
repositories {
    mavenLocal()
    google()
    mavenCentral()
}

dependencies {
    // When updating, must also update the serialization version in libs.versions.toml
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

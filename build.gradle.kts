plugins {
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
}

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlinGradle)

        classpath(libs.google.services)
        classpath(libs.firebase.crashlyticsPlugin)
        classpath(libs.firebase.performancePlugin)

        // Tests
        classpath(libs.test.junitAndroid)
        classpath(libs.test.jacoco)
    }

    // Force everything to use the same jacoco version
    configurations.all {
        resolutionStrategy {
            eachDependency {
                if ("org.jacoco" == requested.group) {
                    useVersion("0.8.12")
                }
            }
        }
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/patrontech/LeapMobileSDK-Android/")
            credentials {
                username = rootProject.githubUser
                password = rootProject.githubToken
            }
        }
    }
}

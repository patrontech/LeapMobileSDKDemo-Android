pluginManagement {
    includeBuild("build-logic")

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "KIBA SDK POC"
rootProject.buildFileName = "build.gradle.kts"

include(":app")

val dependencies: Sequence<String>
    get() = File("Library")
        .walk()
        .maxDepth(1)
        .map { ":${it.name}" }
dependencies.forEach {
    include(it)
}

rootProject.children.forEach { project ->
    if (project.name != "app") {
        project.projectDir = File(settingsDir, "Library/${project.name}")
    }
}

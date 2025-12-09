import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

/**
 * Also add 'alias(libs.plugins.kotlinCompose)' to the module using this plugin
 */
class ComposeConventionPlugin : Plugin<Project> {

    private val implementations = listOf(
        "compose-material3",
        "compose-ui",
        "compose-uiGraphics",
        "compose-uiViewBinding",
        "compose-uiToolingPreview",
        "compose-runtime",
        "compose-compiler",
        "compose-lifecycleViewModel",
        "compose-lifecycleRuntime",
        "compose-foundation",
    )

    override fun apply(project: Project) {
        with(project) {
            val libs = this@with.the<VersionCatalogsExtension>().named("libs")

            extensions.configure<LibraryExtension> {
                configureAndroid(this, libs)
            }
        }
    }

    private fun Project.configureAndroid(
        libraryExtension: LibraryExtension,
        libs: VersionCatalog,
    ) {
        libraryExtension.apply {
            buildFeatures {
                compose = true
            }

            composeOptions {
                kotlinCompilerExtensionVersion = libs.findVersion("compose-compiler").get().requiredVersion
            }

            dependencies {
                add("implementation", platform(libs.findLibrary("compose-bom").get()))
                implementations.forEach {
                    add("implementation", libs.findLibrary(it).get())
                }
                add("debugImplementation", libs.findLibrary("compose-uiTooling").get())
            }
        }
    }
}

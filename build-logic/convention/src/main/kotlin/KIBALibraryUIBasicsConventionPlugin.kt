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
class KIBALibraryUIBasicsConventionPlugin : Plugin<Project> {

    private val implementations = listOf(
        "material",
        "androidx.appCompat",
        "androidx-constraintLayout",
        "androidx-fragmentKtx",
        "androidx-lifecycleViewModel",
        "androidx-lifecycleCompiler",
        "androidx-lifecycleProcess",
        "androidx-recyclerView",
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
        extension: LibraryExtension,
        libs: VersionCatalog,
    ) {
        extension.apply {
            buildFeatures {
                viewBinding = true
            }

            dependencies {
                implementations.forEach {
                    add("implementation", libs.findLibrary(it).get())
                }
            }
        }
    }
}

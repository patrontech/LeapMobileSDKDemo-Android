import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the

class MockModuleConventionPlugin : Plugin<Project> {

    private val implementations = listOf(
        "test-androidxCore",
        "test-androidxKtxCore",
        "test-mockkAndroid",
        "test-junit",
        "test-coroutines",
        "test-assertJ",
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
            dependencies {
                implementations.forEach {
                    add("implementation", libs.findLibrary(it).get())
                }
            }
        }
    }
}

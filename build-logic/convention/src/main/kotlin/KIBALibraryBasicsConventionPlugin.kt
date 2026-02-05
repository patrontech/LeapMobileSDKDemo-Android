import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KIBALibraryBasicsConventionPlugin : Plugin<Project> {

    private val plugins = listOf(
        "androidLibrary",
        "kotlinAndroid",
        "junit",
        "serialization",
    )

    private val implementations = listOf(
        "androidx-coreKtx",
        "serializationJson",
        "serializationCore",
        "coroutinesCore",
        "androidx-appCompat",
        "androidx-fragmentKtx",
        "androidx-lifecycleRuntime",
        "androidx-lifecycleExtensions",
    )

    private val testImplementations = listOf(
        "test-junit",
        "test-assertJ",
        "test-mockk",
        "test-coroutines",
    )

    private val androidTestImplementations = listOf(
        "test-androidxCore",
        "test-kotlin-common",
        "test-coroutines",
        "test-androidxRunner",
        "test-junit",
        "test-mockkAndroid",
        "test-assertJ",
    )

    override fun apply(project: Project) {
        with(project) {
            val libs = this@with.the<VersionCatalogsExtension>().named("libs")
            this@KIBALibraryBasicsConventionPlugin.plugins.forEach {
                pluginManager.apply(libs.findPlugin(it).get().get().pluginId)
            }
            pluginManager.apply("jacoco")

            extensions.findByType(org.gradle.testing.jacoco.plugins.JacocoPluginExtension::class.java)?.apply {
                toolVersion = libs.findVersion("jacoco").get().requiredVersion // set the version you want
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this, libs)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
            }
        }
    }

    private fun Project.configureKotlinAndroid(
        commonExtension: CommonExtension<*, *, *, *, *, *>,
        libs: VersionCatalog,
    ) {
        commonExtension.apply {
            compileSdk = libs.versionAsInt("compileSdk")

            dependencies {
                implementations.forEach {
                    add("implementation", libs.findLibrary(it).get())
                }
                testImplementations.forEach {
                    add("testImplementation", libs.findLibrary(it).get())
                }
                androidTestImplementations.forEach {
                    add("androidTestImplementation", libs.findLibrary(it).get())
                }

                add("coreLibraryDesugaring", libs.findLibrary("desugaringJdk").get())
                add("testRuntimeOnly", libs.findLibrary("test-junitRuntime").get())
            }

            defaultConfig {
                minSdk = libs.versionAsInt("minSdk")

                // Junit 5 configuration
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                testInstrumentationRunnerArguments["runnerBuilder"] = "de.mannodermaus.junit5.AndroidJUnit5Builder"
            }

            buildTypes {
                all {
                    isMinifyEnabled = false
                }

                getByName("debug") {
                    enableUnitTestCoverage = true
                    enableAndroidTestCoverage = true
                }
            }

            // Prevent conflicts on packages with similar licences
            packaging {
                resources.excludes.add("META-INF/LICENSE*")
                resources.excludes.add("META-INF/AL2.0")
                resources.excludes.add("META-INF/LGPL2.1")
                resources.excludes.add("**/*MANIFEST.MF")
            }

            compileOptions {
                isCoreLibraryDesugaringEnabled = true

                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            // Configure the global Kotlin extension
            configure<KotlinAndroidProjectExtension>() {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }

            // Configure specific compilation tasks. This overrides global settings.
            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    // This argument will be added to ALL Kotlin compilation tasks
                    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.FlowPreview")
                    freeCompilerArgs.add("-opt-in=kotlinx.serialization.ExperimentalSerializationApi")
                    freeCompilerArgs.add("-Xexplicit-api=warning")
                }
            }

            lint {
                lintConfig = file("${project.rootDir}/lint-config.xml")
            }
        }

    }
}

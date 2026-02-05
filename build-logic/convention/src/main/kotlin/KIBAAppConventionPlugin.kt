import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KIBAAppConventionPlugin : Plugin<Project> {

    private val plugins = listOf(
        "androidApplication",
        "kotlinAndroid",
        "junit",
        "serialization",
        "firebaseCrashlytics",
        "googleServices",
        "ksp",
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
        "androidx-constraintLayout",
        "firebase-crashlytics",
        "firebase-cloudMessaging",
        "firebase-performance",
        "google-servicesLocation",
        "coroutinesPlayServices",
        "compose-runtime",
        "compose-ui",
    )

    private val testImplementations = listOf(
        "test-junit",
        "test-assertJ",
        "test-mockk",
        "test-coroutines",
    )

    private val androidTestImplementations = listOf(
        "test-androidxCore",
        "test-androidxKtxCore",
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
            this@KIBAAppConventionPlugin.plugins.forEach {
                pluginManager.apply(libs.findPlugin(it).get().get().pluginId)
            }
            pluginManager.apply("jacoco")

            extensions.findByType(JacocoPluginExtension::class.java)?.apply {
                toolVersion = libs.findVersion("jacoco").get().requiredVersion // set the version you want
            }

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this, libs)
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().requiredVersion.toInt()
            }
        }
    }

    private fun Project.configureKotlinAndroid(
        extension: ApplicationExtension,
        libs: VersionCatalog,
    ) {
        extension.apply {
            compileSdk = libs.versionAsInt("compileSdk")

            dependencies {
                add("implementation", platform(libs.findLibrary("firebase-bom").get()))
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
                getByName("release") {
                    isMinifyEnabled = true
                }

                getByName("debug") {
                    isMinifyEnabled = false
                }
            }

            // Prevent conflicts on packages with similar licences
            packaging {
                resources.excludes.add("META-INF/LICENSE*")
                resources.excludes.add("META-INF/AL2.0")
                resources.excludes.add("META-INF/LGPL2.1")
                resources.excludes.add("**/*MANIFEST.MF")
                resources.excludes.add("META-INF/*")
                resources.excludes.add("META-INF/library_release.kotlin_module")
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

            buildFeatures {
                viewBinding = true
                dataBinding = true
                compose = true
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

            configurations.all {
                resolutionStrategy {
                    // Known issues with 4.27.0 included with Ticketmaster, force downgrade to 4.26.1
                    // https://github.com/firebase/firebase-android-sdk/issues/5997
                    force("com.google.protobuf:protobuf-javalite:4.26.1")
                }
            }
        }

    }

}

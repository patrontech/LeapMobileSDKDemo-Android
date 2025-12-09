import org.gradle.api.Project
import java.io.FileInputStream
import java.util.Properties

fun Project.getBuildProperty(name: String, propertiesFileName: String = "local.properties"): String? {
    val properties = this
        .file(propertiesFileName)
        .takeIf { it.exists() }
        ?.let { file -> Properties().apply { load(FileInputStream(file)) } }
    return properties?.get(name) as String?
}

val Project.githubUser: String
    get() = this.getBuildProperty("gpr.user") ?: System.getenv("GITHUB_USERNAME") ?: throw Exception("Github access not configured. Please see README.md.")

val Project.githubToken: String
    get() = this.getBuildProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN") ?: throw Exception("Github access not configured. Please see README.md.")

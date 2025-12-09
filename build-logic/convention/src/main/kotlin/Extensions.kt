import org.gradle.api.artifacts.VersionCatalog

fun VersionCatalog.versionAsInt(versionName: String): Int {
    return this.findVersion(versionName).get().requiredVersion.toInt()
}

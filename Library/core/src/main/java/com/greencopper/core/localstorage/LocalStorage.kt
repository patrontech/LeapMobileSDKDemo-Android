package com.greencopper.core.localstorage

import com.greencopper.parsimonious.parse
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.net.URLEncoder

public class LocalStorage(
    project: String,
    public override val localStorageContainer: LocalStorageContainer
        = TestLocalStorageContainer() // This default is useful in tests.
) : LocalStorageProvider {
    public val app: AppLocalStorageDomain =
        AppLocalStorageDomain(localStorageContainer)

    /**
     * Gets the project assigned when this instance
     * of `LocalStorage` was created. This value is
     * stable. If the current project changes, such
     * as in multi-event, this value does not change.
     *
     * Most of the time this is what you want, because
     * UI and other objects are created and destroyed
     * as needed. In addition, you may be running in
     * a service and not ready to change your current
     * project yet.
     *
     * To always use the latest project, use the
     * `current` method, which returns a new `LocalStorage`
     * pointing to the current project.
     */
    public val project: ProjectLocalStorageDomain =
        ProjectLocalStorageDomain(project, localStorageContainer)

    public operator fun get(project: String): LocalStorage =
        LocalStorage(project, localStorageContainer)

    @Suppress("NAME_SHADOWING")
    public fun replaceUrlParameters(url: String): String {
        // Technically we should never have a blank url, but it assists with tests
        // and the previous code allowed it implicitly.
        if (url.isBlank()) return url
        var url = url
        val substitutions = parse(url, URLSubstitutionParser.url)
        val matchSubstitutions = substitutions.map { it to url.substring(it.range) }
        for ((substitution, match) in matchSubstitutions) {
            url = replaceUrlParameter(substitution, match, url)
        }
        return url
    }

    private fun replaceUrlParameter(substitution: URLSubstitutionParser.Substitution, match: String, url: String): String {
        val currentProject = project.localStorageDomainName.toString()
        val key = substitution.key.inProject(currentProject)
        val error = "The key $key was not present in LocalStorage" +
                " but is required for parameter substitution."
        val container = localStorageContainer
        if (!substitution.optional && !container.keyExists(key)) {
            throw IllegalArgumentException(error)
        }
        return try {
            val subscript = substitution.subscript
            val primitive = if (subscript != null) {
                val obj = container.get(key, JsonObject(emptyMap()))
                val elem = obj[subscript] ?: JsonNull
                if (elem is JsonPrimitive) {
                    elem
                } else if (substitution.optional) {
                    JsonNull
                } else {
                    throw IllegalArgumentException(
                        "The key $key[$subscript] was present in LocalStorage" +
                                " but is not of the correct type."
                    )
                }
            } else {
                container.get(key, JsonPrimitive(null as String?))
            }
            val value = if (primitive is JsonNull) "" else primitive.content.trim()
            if (!substitution.optional && value.isEmpty()) {
                throw IllegalArgumentException(error)
            }
            url.replace(match, URLEncoder.encode(value, "utf-8"))
        } catch (e: SerializationException) {
            if (!substitution.optional) {
                throw IllegalArgumentException(
                    "The key $key was present in LocalStorage" +
                            " but is not of the correct type."
                )
            }
            url.replace(match, "")
        }
    }
}

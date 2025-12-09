package com.greencopper.toolkit.serialization

import kotlinx.serialization.json.*


private const val paramSymbol = "#/"
private val pattern = "\\{${Regex.escape(paramSymbol)}(?<key>[0-9a-z_-]+)(?<optional>\\?)?\\}"
private val regex = Regex(pattern, RegexOption.IGNORE_CASE)

/**
 * Replace parameters in a JsonElement. Parameters are of format '"{#/key?}"'
 * The '?' represent if the parameter is optional or not
 * If a parameter found can't be replaced, an exception is thrown (except if said parameter is
 * optional, in that case it'll be replaced by 'null')
 */
public fun JsonElement.substituteJsonParams(withParams: Map<String, JsonElement>): JsonElement {
    return when (this) {
        is JsonObject -> buildJsonObject {
            jsonObject.keys.forEach { key ->
                when (val value = jsonObject[key]) {
                    is JsonPrimitive -> put(key, value.substitutePrimitiveJsonParams(withParams))
                    is JsonObject -> put(key, value.substituteJsonParams(withParams))
                    is JsonArray -> put(key, buildJsonArray {
                        value.forEach { arrayObject ->
                            when (arrayObject) {
                                is JsonPrimitive -> add(arrayObject.substitutePrimitiveJsonParams(withParams))
                                else -> add(arrayObject.substituteJsonParams(withParams))
                            }
                        }
                    })

                    null -> put(key, JsonNull)
                }
            }
        }
        else -> this
    }
}

private fun JsonPrimitive.substitutePrimitiveJsonParams(withParams: Map<String, JsonElement>): JsonElement {
    return if (isString && content.contains(paramSymbol)) {
        var resultString = content
        var match: MatchResult? = regex.find(resultString)

        while (match != null) {
            val key = match.groups["key"]?.value ?: break // Can't be null, or there would be no match.
            val optional = match.groups["optional"] != null
            val value = withParams[key]

            if (!optional && value == null) {
                throw JsonSubstitutionException.MissingKey(key)
            }

            if (match.range.first == 0 && match.range.last == content.lastIndex) {
                return value?.toSafeElement() ?: JsonNull
            } else {
                var stringValue = value.toString()
                if (stringValue.startsWith('"') && stringValue.endsWith('"')) {
                   stringValue = stringValue.substring(1, stringValue.lastIndex)
                }
                resultString = resultString.replaceRange(match.range, stringValue)
            }

            match = regex.find(resultString)
        }

        JsonPrimitive(resultString)
    } else {
        this
    }
}

public sealed class JsonSubstitutionException(errorMessage: String) : Throwable() {
    override val message: String = errorMessage

    public class MissingKey(key: String) : JsonSubstitutionException(
        "Missing substitute for key \"$key\""
    )

    public class MalformedMatch(match: String) : JsonSubstitutionException(
        "Malformed match \"$match\""
    )
}

/**
 * Parsed string without quote is considered Primitive but not String. We need to add those manually
 * by recreating a JsonPrimitive that will include them
 */
private fun JsonElement.toSafeElement(): JsonElement {
    return (this as? JsonPrimitive)
        ?.takeIf { booleanOrNull == null && intOrNull == null && longOrNull == null && floatOrNull == null && doubleOrNull == null }
        ?.let { JsonPrimitive(this.content) }
        ?: this
}

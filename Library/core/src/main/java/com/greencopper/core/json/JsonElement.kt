package com.greencopper.core.json

import kotlinx.serialization.json.*
import java.math.BigDecimal

internal val JsonElement.truthValue: Boolean
    get() = when (this) {
        is JsonNull -> false
        is JsonPrimitive -> {
            if (isString) {
                content.isNotEmpty()
            } else {
                when (content) {
                    "true" -> true
                    "false" -> false
                    "null" -> false
                    "0" -> false
                    "0.0" -> false
                    else -> {
                        val decimal = BigDecimal(content)
                        decimal != BigDecimal::ZERO
                    }
                }
            }
        }
        is JsonArray -> isNotEmpty()
        is JsonObject -> isNotEmpty()
    }

internal val JsonElement.sortDescriptor: JsonSortDescriptor
    get() = JsonSortDescriptor(this)

internal val JsonElement.sortOrder: Int
    get() = when (this) {
        is JsonNull -> 0
        is JsonPrimitive ->
            if (isString) 3 else
            when (content) {
                "true", "false" -> 1
                "null" -> 0
                else -> 2
            }
        is JsonArray -> 4
        is JsonObject -> 5
    }

/**
 * Internally (and sadly), `JsonPrimitive` stores numbers as strings.
 * `==` is implemented for `JsonPrimitive` via `JsonLiteral`, but it
 * does a simple string comparison. This means that `0` and `0.0` and
 * `00.000` are **not** equal.
 *
 * This won't work for our purposes, so we have to roll our own. Also,
 * the notion of "equality" must be identical to the one used in the
 * Swift implementation (which uses `Decimal`) or we could get different
 * results.
 */
internal infix fun JsonElement.jsonEquals(other: JsonElement): Boolean =
    when (this) {
        is JsonPrimitive -> other is JsonPrimitive && this jsonPrimitiveEquals other
        is JsonArray -> other is JsonArray && this jsonArrayEquals other
        is JsonObject -> other is JsonObject && this jsonObjectEquals other
    }

private infix fun JsonPrimitive.jsonPrimitiveEquals(other: JsonPrimitive): Boolean {
    if (!isNumeric || !other.isNumeric) return this == other
    return BigDecimal(content) == BigDecimal(other.content)
}

private infix fun JsonArray.jsonArrayEquals(other: JsonArray): Boolean =
    isEmpty() && other.isEmpty() ||
            (size == other.size && zip(other).all { it.first jsonEquals it.second })

private infix fun JsonObject.jsonObjectEquals(other: JsonObject): Boolean {
    if (keys != other.keys) return false
    for (key in keys) {
        val thisValue = this[key] ?: return false
        val otherValue = other[key] ?: return false
        if (!(thisValue jsonEquals otherValue)) return false
    }
    return true
}

/**
 * This is used by the `TYPEQ` operator `=@`. It is true if both
 * sides of the expression have the same type, e.g.,
 *
 * ```
 * .languages =@ {}
 * ```
 *
 * This is a shallow comparison.
 */
internal infix fun JsonElement.jsonTypeEquals(other: JsonElement): Boolean =
    when (this) {
        is JsonNull -> other is JsonNull
        is JsonPrimitive -> other is JsonPrimitive && this jsonPrimitiveTypeEquals other
        is JsonObject -> other is JsonObject
        is JsonArray -> other is JsonArray
    }

private infix fun JsonPrimitive.jsonPrimitiveTypeEquals(other: JsonPrimitive): Boolean =
    (isString && other.isString) ||
        (isNumeric && other.isNumeric) ||
        (isBoolean && other.isBoolean)
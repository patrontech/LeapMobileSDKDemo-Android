package com.greencopper.core.json

import kotlinx.serialization.json.*
import java.math.BigDecimal

/**
 * The point of this is to exactly match the sorting used on iOS.
 */
internal class JsonSortDescriptor(private val json: JsonElement): Comparable<JsonSortDescriptor> {
    override fun compareTo(other: JsonSortDescriptor): Int =
        if (json is JsonPrimitive && other.json is JsonPrimitive) {
            if (json.isString && other.json.isString)
               json.content.compareTo(other.json.content)
            else if (json.isNumeric && other.json.isNumeric)
                BigDecimal(json.content).compareTo(BigDecimal(other.json.content))
            else if (json.isBoolean && other.json.isBoolean)
                json.boolean.compareTo(other.json.boolean)
            else
                json.sortOrder.compareTo(other.json.sortOrder)
        } else json.sortOrder.compareTo(other.json.sortOrder)
}
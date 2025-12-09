package com.greencopper.core.json

import kotlinx.serialization.json.*

internal val JsonPrimitive.isNumeric: Boolean
    get() = if (isString) {
        false
    } else when (content) {
        "true", "false", "null" -> false
        else -> true
    }

internal val JsonPrimitive.isBoolean: Boolean
    get() = if (isString) {
        false
    } else when (content) {
        "true", "false" -> true
        else -> false
    }

internal val JsonPrimitive.isNull: Boolean
    get() = !isString && content == "null"
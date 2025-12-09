package com.greencopper.core.json

import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JsonPrimitiveTest {
    @Test
    internal fun `truth value for all JSON types`() {
       val values = listOf(
           // null, truth value: false
           JsonNull,
           // Empty object, truth value: false
           JsonObject(mapOf()),
           // Non-empty object, truth value: true
           JsonObject(mapOf("key" to JsonPrimitive("value"))),
           // Empty array, truth value: false
           JsonArray(listOf()),
           // Non-empty array, truth value: true
           JsonArray(listOf(JsonPrimitive("value"))),
           // Empty string, truth value: false
           JsonPrimitive(""),
           // Non-empty string, truth value: true
           JsonPrimitive("value"),
           // Zero, truth value: false
           JsonPrimitive(0),
           // Non-zero, truth value: true
           JsonPrimitive(99),
           // False, truth value: false
           JsonPrimitive(false),
           // True, truth value: true
           JsonPrimitive(true)
       )
       assertThat(values.map { it.truthValue })
           .isEqualTo(listOf(
               false, // null
               false, // empty object
               true, // non-empty object
               false, // empty array
               true, // non-empty array
               false, // empty string
               true, // non-empty string
               false, // zero
               true, // non-zero
               false, // false
               true // true
           ))
    }

    @Test
    internal fun `booleans are detected properly`() {
        val booleans = listOf(true, false).map(::JsonPrimitive)
        assertThat(booleans.map { it.isBoolean } ).isEqualTo(listOf(true, true))
    }
}
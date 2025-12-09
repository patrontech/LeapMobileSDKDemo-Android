package com.greencopper.core.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test

internal class JsonElementTest {
    @Test
    fun `test sortOrder`() {
        val values = listOf(
            JsonNull,
            JsonPrimitive(true),
            JsonPrimitive(10),
            JsonPrimitive("string"),
            JsonArray(listOf()),
            JsonObject(mapOf())
        )
        assertThat(values.map { it.sortOrder })
            .isEqualTo(listOf(0, 1, 2, 3, 4, 5))
    }

    @Test
    fun `test array equality`() {
        val numbers0 = JsonArray(emptyList())
        val numbers1 = JsonArray(listOf(1, 2, 3).map(::JsonPrimitive))
        val numbers2 = JsonArray(listOf(7, 9, 14).map(::JsonPrimitive))
        val numbers3 = JsonArray(listOf(2, 4).map(::JsonPrimitive))

        assertThat(numbers0 jsonEquals numbers0).isTrue
        assertThat(numbers1 jsonEquals numbers1).isTrue
        assertThat(!(numbers0 jsonEquals numbers2)).isTrue
        assertThat(!(numbers1 jsonEquals numbers3)).isTrue
    }

    @Test
    fun `test object equality`() {
        val object0 = JsonObject(emptyMap())
        val object1 = JsonObject(mapOf("key" to JsonPrimitive("value")))
        val object2 = JsonObject(
            mapOf("key" to JsonPrimitive("value"), "key2" to JsonPrimitive(2))
        )

        assertThat(object0 jsonEquals object0).isTrue
        assertThat(object1 jsonEquals object1).isTrue
        assertThat(!(object1 jsonEquals object2)).isTrue
    }

    @Test
    fun `test general equality`() {
        val none = JsonNull
        val int = JsonPrimitive(0)
        val bool = JsonPrimitive(true)
        val string = JsonPrimitive("string")
        val array = JsonArray(listOf(none, int, bool))
        val obj = JsonObject(
            mapOf(
                "none" to none,
                "int" to int,
                "bool" to bool,
                "string" to string,
                "array" to array
            )
        )

        val map = mapOf(
            "none" to none,
            "int" to int,
            "bool" to bool,
            "string" to string,
            "array" to array,
            "obj" to obj
        )

        for (key1 in map.keys) {
            for (key2 in map.keys) {
                if (key1 == key2) {
                    assertThat(map[key1]!! jsonEquals map[key2]!!).isTrue
                } else {
                    assertThat(!(map[key1]!! jsonEquals map[key2]!!)).isTrue
                }
            }
        }
    }

    @Test
    fun `test isNull`() {
        assertThat(!JsonPrimitive(3).isNull).isTrue
        assertThat(JsonNull.isNull).isTrue
    }

    @Test
    fun `test sorting`() {
        val list = listOf(
            JsonObject(emptyMap()),
            JsonArray(emptyList()),
            JsonPrimitive("string2"),
            JsonPrimitive("string1"),
            JsonPrimitive(0),
            JsonPrimitive(3),
            JsonPrimitive(false),
            JsonPrimitive(true),
            JsonNull
        )

        assertThat(list.sortedBy { it.sortDescriptor })
            .isEqualTo(
                listOf(
                    JsonNull,
                    JsonPrimitive(false),
                    JsonPrimitive(true),
                    JsonPrimitive(0),
                    JsonPrimitive(3),
                    JsonPrimitive("string1"),
                    JsonPrimitive("string2"),
                    JsonArray(emptyList()),
                    JsonObject(emptyMap())
                )
            )
    }
}
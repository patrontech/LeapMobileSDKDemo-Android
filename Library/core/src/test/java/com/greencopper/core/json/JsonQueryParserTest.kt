package com.greencopper.core.json

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/*
 * These tests (including their names) are functionally identical to the iOS tests.
 *
 * This is important because the language must function identically on both platforms.
 */
internal class JsonQueryParserTest {

    init {
        Toolkit.setupTest()
    }

    private val rawJSON = """
        {
            "languages": [
                {"name": "Kotlin", "paradigms": ["OOP", "functional", "imperative", "static"], "year": 2014},
                {"name": "Haskell", "paradigms": ["functional", "static"], "year": 1990},
                {"name": "Ruby", "paradigms": ["OOP", "functional", "imperative", "dynamic"], "year": 1995},
                {"name": "Rust", "paradigms": ["imperative", "static"], "year": 2010},
                {"name": "J", "paradigms": ["function-level", "dynamic"], "year": 1990}
            ]
        }
    """.trimIndent()

    val json: JsonElement = App.resolve<Json>().decodeFromString(rawJSON)

    @Test
    fun testNot() {
        val query = ".languages[!((.year < 2010) && .paradigms[. == \"OOP\"])].name"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonArray.toList())
            .isEqualTo(listOf("Kotlin", "Haskell", "Rust", "J").map(::JsonPrimitive))
    }

    @Test
    fun testContains() {
        // What are the sorted names of the functional languages?
        val query = ".languages[.paradigms[. == \"functional\"]].name$"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonArray.toList())
            .isEqualTo(listOf("Haskell", "Kotlin", "Ruby").map(::JsonPrimitive))
    }

    @Test
    fun testIndex() {
        // What's the name of the first static language after sorting?
        val query = ".languages[.paradigms[. == \"static\"]].name$.0"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.content).isEqualTo("Haskell")
    }

    @Test
    fun testFilterWithObject() {
        // Is the number of the paradigms of the first named language greater than
        // or equal to 4?
        val query = ".languages.0.[.name].paradigms.# >= 4"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.boolean).isTrue
    }

    @Test
    fun testMapWithArray() {
        // What's the list of the first paradigm of each language?
        val query = ".languages.paradigms @ .0"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonArray.map { it.jsonPrimitive.content })
            .isEqualTo(listOf("OOP", "functional", "OOP", "imperative", "function-level"))
    }

    @Test
    fun testMapWithNonArray() {
        // This one doesn't make much sense, but satisfies the test.
        val query = ".languages.0 @ [1, 2, 3]"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonArray.map { it.jsonPrimitive.int })
            .isEqualTo(listOf(1, 2, 3))
    }

    @Test
    fun testCountWithObject() {
        // This returns 3, because the first object has three fields.
        val query = ".languages.0#"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.int).isEqualTo(3)
    }

    @Test
    fun testCountWithTruePrimitive() {
        // This returns 1, because the truthValue of a non-empty
        // string is true.
        val query = ".languages.0.name#"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.int).isEqualTo(1)
    }

    @Test
    fun testCountWithFalsePrimitive() {
        val query = "0#"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.int).isEqualTo(0)
    }

    @Test
    fun testUniqueWithoutArray() {
        val query = ".languages.0.name~"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.content).isEqualTo("Kotlin")
    }

    @Test
    fun testInequality() {
        // Are there any non-OOP languages? Return their names sorted.
        // Literally: Are there any languages for which the count of paradigms
        // without OOP is equal to the count of paradigms?
        val query = ".languages[.paradigms | .[. != \"OOP\"]# == #].name$"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonArray.map { it.jsonPrimitive.content })
            .isEqualTo(listOf("Haskell", "J", "Rust"))
    }

    @Test
    fun testFlattenWithMixedElements() {
        // Flatten the JSON array using the ^ operator.
        val query = "[1, [2, 3], [4, 5], 6]^"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result.jsonArray.map { it.jsonPrimitive.int })
            .isEqualTo(listOf(1, 2, 3, 4, 5, 6))
    }

    @Test
    fun testFlattenWithNonArray() {
        val query = "\"Hi\"^"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result.jsonPrimitive.content).isEqualTo("Hi")
    }

    @Test
    fun testSortWithNonArray() {
        val query = "3$"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result.jsonPrimitive.int).isEqualTo(3)
    }

    @Test
    fun testKeyInArrayOfPrimitives() {
        val query = "[2][.x]"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result.jsonArray.toList().size).isEqualTo(0)
    }

    @Test
    fun testKeyWithPrimitive() {
        val query = "\"primitive\".something"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result).isEqualTo(JsonNull)
    }

    @Test
    fun testOr() {
        // How many languages are either OOP or functional?
        val query = ".\"languages\"[.paradigms[(. == \"OOP\") || (. == \"functional\")]]#"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.int).isEqualTo(3)
    }

    @Test
    fun testAnd() {
        // Are there any OOP languages released before 2010?
        val query = ".languages[(.year <= 2010) && .paradigms[. == \"OOP\"]].name"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonArray).isEqualTo(listOf(JsonPrimitive("Ruby")))
    }

    @Test
    fun testGreaterThan() {
        // Are there more than 5 distinct language paradigms?
        // Explanation:
        // .languages.paradigms produces an array of arrays.
        // ^ flattens into a single array.
        // ~ gets the unique values
        // # counts the values
        val query = ".languages.paradigms^~# > 5"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.boolean).isTrue
    }

    @Test
    fun testGreaterThanOrEqual() {
        val query = ".languages | # >= 3"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.boolean).isTrue
    }

    @Test
    fun testLessThan() {
        val query = ".languages.# < 10"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.boolean).isTrue
    }

    @Test
    fun testLessThanOrEqual() {
        val query = ".languages.year^~# <= 10"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.boolean).isTrue
    }

    @Test
    fun testNegativeIndex() {
        // What's the name of the penultimate language?
        val query = ".languages.-2.name"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result.jsonPrimitive.content).isEqualTo("Rust")
    }

    @Test
    fun testIndexOutOfRange() {
        val query = ".languages.99"
        val result = JsonQueryParser.parse(query).eval(json)
        assertThat(result).isEqualTo(JsonNull)
    }

    @Test
    fun testIndexNonArray() {
        val query = "\"fish\".0"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result).isEqualTo(JsonNull)
    }

    @Test
    fun testMissingKey() {
        val query = "{\"key\": \"value\"}.foo"
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result).isEqualTo(JsonNull)
    }

    @Test
    fun testTypeEquality() {
        val examples = listOf("null", "0", "false", "\"\"", "{}", "[]")
        val subqueries = mutableListOf<String>()
        for (example1 in examples) {
            for (example2 in examples) {
                if (example1 == example2) {
                    subqueries.add("($example1=@$example2)")
                } else {
                    subqueries.add("($example1!=@$example2)")
                }
            }
        }
        val query = subqueries.joinToString("&&")
        val result = JsonQueryParser.parse(query).eval(JsonNull)
        assertThat(result.jsonPrimitive.boolean).isTrue
    }

    @Test
    fun testKey() {
        val query = ".a-bC3"
        val result = JsonQueryParser.parse(query)
        assertThat(result.toString()).isEqualTo("Path(Path(Current.Key(\"a-bC3\")))")
    }

    @Test
    fun testToString() {
        val strings = listOf(
            JsonQuery.Path(listOf(JsonQuery.Current)),
            JsonQuery.Logic(JsonQuery.Key("foo"), JsonQuery.Op.EQ, JsonQuery.Key("bar")),
            JsonQuery.Key("key"),
            JsonQuery.Index(99),
            JsonQuery.Current,
            JsonQuery.Count,
            JsonQuery.Flatten,
            JsonQuery.Unique,
            JsonQuery.Sort,
            JsonQuery.Filter(JsonQuery.Count),
            JsonQuery.Json(JsonPrimitive(3))
        ).map { it.toString() }
        assertThat(strings).isEqualTo(
            listOf(
                "Path(Current)",
                "Logic(Key(\"foo\")==Key(\"bar\"))",
                "Key(\"key\")",
                "Index(99)",
                "Current",
                "Count",
                "Flatten",
                "Unique",
                "Sort",
                "Filter(Count)",
                "Json(3)"
            )
        )
    }
}


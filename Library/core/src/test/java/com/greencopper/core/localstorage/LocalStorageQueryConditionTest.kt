package com.greencopper.core.localstorage

import com.greencopper.core.localstorage.LocalStorageQueryCondition.LocalStorageQueryConditionData
import com.greencopper.parsimonious.ParseException
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.LazyResolver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class LocalStorageQueryConditionTest {
    private lateinit var container: LocalStorageContainer
    private lateinit var condition: LocalStorageQueryCondition

    @BeforeEach
    internal fun setUp() {
        Toolkit.setupTest()
        container = TestLocalStorageContainer()
        container.set(LocalStorageKey("@/foo"), JsonPrimitive("bar"))
        container.set(
            LocalStorageKey("UT_test/custom"),
            JsonObject(
                mapOf(
                    "foo" to JsonPrimitive("bar")
                )
            )
        )
        condition =
            LocalStorageQueryCondition(LazyResolver.adhoc(LocalStorage("UT_test", container)))
    }


    @Test
    fun `checkWith succeeds`() {
        val data = LocalStorageQueryConditionData("~/custom", ".foo == \"bar\"")
        assertThat(condition.checkWith(data)).isTrue
    }

    @Test
    fun `checkWith throws if the query is invalid`() {
        val data = LocalStorageQueryConditionData("~/custom", "%3")
        assertThrows<ParseException> {
            condition.checkWith(data)
        }
    }

    @Test
    fun `checkWith succeeds in app domain`() {
        val data = LocalStorageQueryConditionData("@/foo", ". == \"bar\"")
        assertThat(condition.checkWith(data)).isTrue
    }

    @Test
    fun `checkWith fails if value is missing`() {
        val data = LocalStorageQueryConditionData("~/foo", ". == \"bar\"")
        assertThat(condition.checkWith(data)).isFalse
    }

    @Test
    fun `checkWithFlow succeeds`() {
        val data = LocalStorageQueryConditionData("~/custom", ".foo == \"bar\"")
        runTest {
            val result = condition.checkWithFlow(data).first()
            assertThat(result).isTrue
        }
    }
}
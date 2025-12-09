package com.greencopper.core.conditions

import com.greencopper.parsimonious.ParseException
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testSerializable
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class MetadataQueryConditionTest {

    init {
        Toolkit.setupTest()
    }

    private val condition = MetadataQueryCondition()
    private val json = App.resolve<Json>()

    @Test
    fun matchingMetadata_checkWith_returnsTrue() {
        condition.metadata.value = json.encodeToJsonElement(mapOf("foo" to "bar"))
        val param = MetadataQueryCondition.MetadataQueryData(".foo == \"bar\"")
        assertThat(condition.checkWith(param)).isTrue
    }

    @Test
    fun matchingMetadata_checkWithFlow_returnsTrue() {
        condition.metadata.value = json.encodeToJsonElement(mapOf("foo" to "bar"))
        val param = MetadataQueryCondition.MetadataQueryData(".foo == \"bar\"")

        runTest {
            assertThat(condition.checkWithFlow(param).first()).isTrue
        }
    }

    @Test
    fun unmatchingMetadata_checkWith_returnsFalse() {
        condition.metadata.value = json.encodeToJsonElement(mapOf("foo" to JsonNull))
        val param = MetadataQueryCondition.MetadataQueryData(".foo == \"bar\"")
        assertThat(condition.checkWith(param)).isFalse
    }

    @Test
    fun noMetadata_checkWith_returnsFalse() {
        val param = MetadataQueryCondition.MetadataQueryData(".foo == \"bar\"")
        assertThat(condition.checkWith(param)).isFalse
    }

    @Test
    fun invalidQuery_checkWith_throws() {
        val param = MetadataQueryCondition.MetadataQueryData("##+")
        assertThrows<ParseException> {
            condition.checkWith(param)
        }
    }

    @Test
    fun deserialize_doesNotThrow() {
        val data = MetadataQueryCondition.MetadataQueryData("query")
        assertDoesNotThrow {
            assertThat(condition.deserialize(json.encodeToJsonElement(data)))
        }
    }

    @Test
    fun metadataQueryData_testSerializable() {
        testSerializable(MetadataQueryCondition.MetadataQueryData("query"))
    }
}
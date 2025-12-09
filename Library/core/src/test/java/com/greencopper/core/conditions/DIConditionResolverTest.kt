package com.greencopper.core.conditions

import com.greencopper.core.conditions.conditionchecker.DIConditionResolver
import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DIConditionResolverTest {

    private val conditionCheckerResolver = DIConditionResolver()

    init {
        Toolkit.setupTest()
    }

    @Test
    fun whenResolvingExistingChecker_shouldResolveProperChecker() {
        App.resolve<Registrar>().bindCondition(TestCondition.key, auto(::TestCondition))
        val checker = conditionCheckerResolver.resolve(TestCondition.key)
        assertThat(checker).isNotNull
    }

    @Test
    fun whenResolvingNonExistingChecker_shouldResolveNull() {
        val checker = conditionCheckerResolver.resolve(ConditionInfo.Key("Null", -1))
        assertThat(checker).isNull()
    }

    @Test
    fun whenResolvingMetadataQueryCondition_shouldPassMetadata() {
        App.resolve<Registrar>().bindCondition(MetadataQueryCondition.key, auto(::MetadataQueryCondition))
        val json = App.resolve<Json>()
        val metadata = json.encodeToJsonElement(mapOf("test" to "value"))

        val condition = conditionCheckerResolver.resolve(MetadataQueryCondition.key, MutableStateFlow(metadata))
        assertThat(condition).isInstanceOf(MetadataQueryCondition::class.java)
        assertThat((condition as MetadataQueryCondition).metadata.value).isEqualTo(metadata)
    }
}

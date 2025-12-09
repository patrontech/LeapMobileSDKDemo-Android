package com.greencopper.interfacekit.search.logic

import com.greencopper.testmocks.interfacekit.MockParameterizedSearchProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SearchProviderTest {

    private val searchProvider: MockParameterizedSearchProvider = MockParameterizedSearchProvider()

    init {
        Toolkit.setupTest()
    }

    @Test
    fun getEntries_withNullParams_shouldFail() {
        assertThrows<ParameterizedSearchProviderException.NoParametersProvidedException> {
            searchProvider.entries(null)
        }
    }

    @Test
    fun getEntries_withWrongParams_shouldFail() {
        assertThrows<ParameterizedSearchProviderException.ParametersDecodeFailed> {
            val params = buildJsonObject { put("testKey", "testValue") }
            searchProvider.entries(params)
        }
    }

    @Test
    fun getEntries_withCorrectParams_shouldReturn() {
        //given
        val data = MockParameterizedSearchProvider.Params(listOf("test1", "test2"))

        //when
        val result = searchProvider.entries(data.encodeToJsonElement())

        //then
        assertThat(result.size).isEqualTo(2)
        assertThat(result.first().matches.first()).isEqualTo("test1")
    }

}

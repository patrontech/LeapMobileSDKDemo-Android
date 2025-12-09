package com.greencopper.interfacekit.search.logic

import com.greencopper.testmocks.interfacekit.MockParameterizedSearchProvider
import com.greencopper.testmocks.setupTest
import com.greencopper.testmocks.testKiboSerializable
import com.greencopper.toolkit.Toolkit
import org.junit.jupiter.api.Test

internal class SearchProviderInfoTest {

    init {
        Toolkit.setupTest()
    }

    @Test
    fun testSerializable() {
        val data = SearchProviderInfo(
            SearchProviderKey("key", 1),
            MockParameterizedSearchProvider.Params(listOf("test1"))
        )

        testKiboSerializable(data)
    }

    @Test
    fun testSerializableWithNullParams() {
        val params: MockParameterizedSearchProvider.Params? = null
        val data = SearchProviderInfo(
            SearchProviderKey("key", 1),
            params
        )

        testKiboSerializable(data)
    }
}

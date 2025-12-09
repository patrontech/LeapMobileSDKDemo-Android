package com.greencopper.interfacekit.search

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.search.logic.SearchProvider
import com.greencopper.interfacekit.search.logic.SearchProviderInfo
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.testmocks.interfacekit.MockParameterizedSearchProvider
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.*

internal class SearchInitializerTest {
    private lateinit var searchInitializer: SearchInitializer
    private val analytics = ScreenNameAnalytics("TestSearch")
    private val data = MockParameterizedSearchProvider.Params(listOf("test1", "test2"))

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
    }

    @BeforeEach
    fun setupEach() {
        searchInitializer = SearchInitializer()
    }

    @Test
    fun getLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            searchInitializer.getLayout(null)
        }
    }

    @Test
    fun getLayout_withWrongParams_shouldThrow() {
        val params = buildJsonObject { put("testKey", "testValue") }
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            searchInitializer.getLayout(params)
        }
    }

    @Test
    fun getLayout_withCorrectParams_shouldSucceed() {
        mockBundleConstructor()
        bindProvider<SearchProvider>(MockParameterizedSearchProvider(), MockParameterizedSearchProvider.key)

        val encodedParams = data.encodeToJsonElement()
        val params = SearchData(
            listOf(SearchProviderInfo(MockParameterizedSearchProvider.key, encodedParams)),
            true,
            analytics,
            ""
        ).encodeToJsonElement()
        Assertions.assertThat(searchInitializer.getLayout(params)).isNotNull
    }

    @Test
    fun getRedirectionHash_withoutParams_shouldReturnDefault() {
        val hash = searchInitializer.redirectionHashFor(null)
        Assertions.assertThat(hash).isEqualTo(RedirectionHash(SearchInitializer.key))
    }

    @Test
    fun getRedirectionHash_withWrongParams_shouldReturnDefault() {
        val params = buildJsonObject { put("testKey", "testValue") }
        val hash = searchInitializer.redirectionHashFor(params)
        Assertions.assertThat(hash).isEqualTo(RedirectionHash(SearchInitializer.key))
    }

    @Test
    fun getRedirectionHash_withCorrectParams_shouldSucceed() {
        val params = SearchData(
            listOf(SearchProviderInfo(MockParameterizedSearchProvider.key, data.encodeToJsonElement())),
            true,
            analytics,
            ""
        ).encodeToJsonElement()
        val hash = searchInitializer.redirectionHashFor(params)
        Assertions.assertThat(hash).isNotNull
    }

    @Test
    fun testSerializable() {
        val data = SearchLayoutData(
            listOf(),
            false,
            ScreenNameAnalytics("screenName"),
            RedirectionHash(FeatureKey("name", 1)),
            ""
        )
        testKiboSerializable(data)
    }
}

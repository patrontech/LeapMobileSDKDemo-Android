package com.greencopper.ticketing.ticketsscan

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.ticketing.providers.ProviderInfo
import com.greencopper.ticketingmocks.providers.MockProvider
import com.greencopper.ticketingmocks.providers.MockProviderResolver
import com.greencopper.toolkit.Toolkit
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class TicketsScanInitializerTest {

    private val initializer: TicketsScanInitializer
    private val providerResolver: MockProviderResolver

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        providerResolver = MockProviderResolver()
        initializer = TicketsScanInitializer(providerResolver)
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withWrongParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(buildJsonObject { put("testKey", "testValue") })
        }
    }

    @Test
    fun whenGettingLayout_withoutProvider_shouldThrow() {
        val parameters = TicketsScanData(
            ProviderInfo(MockProvider.key, MockProvider.Data("apiUrl").encodeToJsonElement()),
            "featureLink",
            ScreenNameAnalytics("test")
        )

        assertThrows<NoSuchElementException> {
            initializer.getLayout(
                parameters.encodeToJsonElement()
            )
        }

    }

    @Test
    fun whenGettingLayout_withCorrectParams_shouldGetLayout() {
        mockBundleConstructor()
        providerResolver.providers = mapOf(
            MockProvider.key to MockProvider()
        )

        val parameters = TicketsScanData(
            ProviderInfo(MockProvider.key, MockProvider.Data("apiUrl").encodeToJsonElement()),
            "featureLink",
            ScreenNameAnalytics("test")
        )
        val layout = initializer.getLayout(
            parameters.encodeToJsonElement()
        )
        Assertions.assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetHash() {
        val redirectionHash = initializer.redirectionHashFor(null)
        Assertions.assertThat(redirectionHash).isNotNull
    }
}

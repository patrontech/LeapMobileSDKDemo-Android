package com.greencopper.interfacekit.links

import android.content.Context
import com.greencopper.interfacekit.links.resolver.ConcreteLinkResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.core.MockLocalizationService
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ConcreteLinkResolverTest {

    private val linksConfigHolder = LinksConfigurationHolder()
    private lateinit var testLinksConfig: LinksConfiguration
    private lateinit var resolver: ConcreteLinkResolver

    @BeforeEach
    fun beforeEach() {
        Toolkit.setupTest()
        val json: Json = App.resolve()

        val paramString = "paramString"
        testLinksConfig = LinksConfiguration(
            mapOf(
                "1" to Route.Present(
                    feature = FeatureInfo(key = FeatureKey("", 1))
                ),
                "2" to Route.Push(
                    feature = FeatureInfo(key = FeatureKey("", 1))
                ),
                "3" to Route.Present(
                    FeatureInfo(
                        FeatureKey("mockFeature", 1),
                        json.parseToJsonElement(
                            """
                                { "myParamString": "{#/$paramString}" }
                            """.trimIndent()
                        )
                    )
                )
            ),
            mapOf(
                "3" to FeatureInfo(key = FeatureKey("feature3", 1)),
                "4" to FeatureInfo(key = FeatureKey("feature4", 1)),
                "5" to FeatureInfo(
                    FeatureKey("mockFeature", 1),
                    json.parseToJsonElement(
                        """
                            { "myParamString": "{#/$paramString}" }
                        """.trimIndent()
                    )
                )
            )
        )
        linksConfigHolder.currentConfiguration.value = testLinksConfig

        val mockContext = mockk<Context>()
        every { mockContext.getString(any()) } returns "scheme"

        resolver = ConcreteLinkResolver(linksConfigHolder, MockLocalizationService(), App.resolve(), mockContext)
    }

    @Test
    fun doesNotHaveConfig_shouldResolveNull() {
        linksConfigHolder.currentConfiguration.value = null
        assertThat(resolver.route("scheme://1")).isNull()
        assertThat(resolver.featureInfo("scheme://3")).isNull()
    }

    @Test
    fun doesNotHaveRoute_shouldResolveNull() {
        assertThat(resolver.route("scheme://a")).isNull()
    }

    @Test
    fun hasRoute_shouldResolve() {
        val route = resolver.route("scheme://1")
        assertThat(route).isNotNull
        assertThat(route).isEqualTo(testLinksConfig.routeLinks["1"])
    }

    @Test
    fun givenParamsAsNumberStrings_route_appendsParameters() {
        val testParam = "paramString"
        val testValue = "\"123\""
        val uri = resolver.route("scheme://3", mapOf(testParam to testValue)) as Route.Present
        assertThat(uri.feature.params.toString()).contains("\"myParamString\":\"123\"")
    }

    @Test
    fun doesNotHaveFeature_shouldResolveNull() {
        assertThat(resolver.featureInfo("scheme://a")).isNull()
    }

    @Test
    fun hasFeature_shouldResolve() {
        val featureInfo = resolver.featureInfo("scheme://3")
        assertThat(featureInfo).isNotNull
        assertThat(featureInfo).isEqualTo(testLinksConfig.featureLinks["3"])
    }

    @Test
    fun route_wrongScheme_returnsNull() {
        val route = resolver.route("wrongScheme://1")
        assertThat(route).isNull()
    }

    @Test
    fun featureInfo_wrongScheme_returnsNull() {
        val route = resolver.featureInfo("wrongScheme://3")
        assertThat(route).isNull()
    }

    @Test
    fun route_withParams_replacesParams() {
        val testValue = "test"
        val route = resolver.route("scheme://3?paramString=$testValue")
        assertThat(route).isNotNull
        assertThat(route?.getParams()?.toString()).contains(testValue)
    }

    @Test
    fun featureInfo_withParams_replacesParams() {
        val testValue = "test2"
        val featureInfo = resolver.featureInfo("scheme://5?paramString=$testValue")
        assertThat(featureInfo).isNotNull
        assertThat(featureInfo?.params?.toString()).contains(testValue)
    }

    @Test
    fun givenParams_routeUri_appendsParameters() {
        val testParam = "param"
        val testValue = "value"
        val uri = resolver.routeUri("scheme://1", mapOf(testParam to testValue))
        assertThat(uri.toString()).contains(testParam)
        assertThat(uri.toString()).contains(testValue)
    }
}

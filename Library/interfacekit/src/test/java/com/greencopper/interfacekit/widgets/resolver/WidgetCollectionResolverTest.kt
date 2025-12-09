package com.greencopper.interfacekit.widgets.resolver

import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.WidgetCollectionConfigurationHolder
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageWidgetParameters
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.container.Key
import com.greencopper.toolkit.serialization.JsonFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class WidgetCollectionResolverTest {

    private val configHolder = WidgetCollectionConfigurationHolder()
    private val widgetCollectionResolver = WidgetCollectionResolver(configHolder)

    @BeforeEach
    fun beforeEach() {
        App = mockk()
        every {
            App.resolve(Json::class, any(), any())
        } returns Pair(
            Key(Json::class, Unit),
            JsonFactory.create()
        )
    }

    @Test
    fun resolveWithoutConfig_returnsNull() {
        assertThat(widgetCollectionResolver.resolve("test")).isNull()
    }

    @Test
    fun resolveWithEmptyInstance_returnsNull() {
        //given
        configHolder.currentConfiguration.value = WidgetCollectionConfiguration(mapOf())

        //then
        assertThat(widgetCollectionResolver.resolve("test")).isNull()
    }

    @Test
    fun resolveWithWrongKey_returnsNull() {
        //given
        configHolder.currentConfiguration.value = createConfig()

        //then
        assertThat(widgetCollectionResolver.resolve("test")).isNull()
    }

    @Test
    fun resolveWithCorrectKey_returnsInstance() {
        //given
        configHolder.currentConfiguration.value = createConfig()

        //when
        val instance = widgetCollectionResolver.resolve("instance")

        //then
        assertThat(instance).isNotNull
        assertThat(instance?.widgets?.get(0)?.key?.name).isEqualTo("widget")
    }

    private fun createConfig(): WidgetCollectionConfiguration {
        return WidgetCollectionConfiguration(
            mapOf(
                "instance" to WidgetCollectionConfiguration.Instance(
                    null, listOf(
                        WidgetCollectionConfiguration.Instance.WidgetInfo(
                            WidgetCollectionConfiguration.Instance.WidgetKey("widget", 1),
                            FullWidthImageWidgetParameters(
                                FullWidthImageWidgetParameters.Image("image_light", "image_dark"),
                                null,
                                ItemNameAnalytics("analytics"),
                                null
                            ).encodeToJsonElement()
                        )
                    )
                )
            )
        )
    }

}

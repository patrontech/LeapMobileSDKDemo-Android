package com.greencopper.maps.geomap

import android.graphics.Color as AndroidColor
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.filtering.FilteringInfo
import com.greencopper.interfacekit.filtering.FilteringPredicate
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.layout.LayoutDataProvider
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.maps.geomap.data.MapData
import com.greencopper.testmocks.*
import com.greencopper.testmocks.interfacekit.MockLayoutDataProvider
import com.greencopper.toolkit.Toolkit
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.serialization.json.JsonNull
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*

internal class GeoMapInitializerTest {

    init {
        Toolkit.setupTest()
        bindProvider<LayoutDataProvider>(MockLayoutDataProvider())
        bindProvider(mockk<RouteController>())
        bindSingleton(mockk<AggregateMetricsService>())
    }

    private val initializer = GeoMapInitializer()

    @Test
    fun checkKey() {
        assertThat(GeoMapInitializer.key.name).isEqualTo("Maps.GeoMap")
    }

    @Test
    fun whenGettingLayout_withoutParams_shouldThrow() {
        assertThrows<FeatureInitializerException.NoParametersProvidedException> {
            initializer.getLayout(null)
        }
    }

    @Test
    fun whenGettingLayout_withEmptyParams_shouldThrow() {
        assertThrows<FeatureInitializerException.ParametersDecodeFailed> {
            initializer.getLayout(JsonNull)
        }
    }

    @Test
    fun whenGettingLayout_withProperParams_shouldGetLayout() {
        mockBundleConstructor()
        val params = geoMapTestData.encodeToJsonElement()

        val layout = initializer.getLayout(params)
        assertThat(layout).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withoutParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(null)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(GeoMapInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withWrongParams_shouldGetDefault() {
        val redirectionHash = initializer.redirectionHashFor(JsonNull)
        assertThat(redirectionHash).isEqualTo(RedirectionHash(GeoMapInitializer.key))
    }

    @Test
    fun whenGettingRedirectionHash_withProperParams_shouldGetHash() {
        val params = geoMapTestData.encodeToJsonElement()
        val redirectionHash = initializer.redirectionHashFor(params)
        assertThat(redirectionHash).isNotNull
    }

    @Test
    fun whenGettingRedirectionHash_withPreselectedLocationId_containsId() {
        val preselectedLocationId = "1234"
        val data = GeoMapData(
            geoJson = MapData(listOf()),
            filtering = FilteringInfo(FilteringPredicate.Tag("tag")),
            preSelectedLocationId = preselectedLocationId,
            showUserLocation = true,
            hideMapBackgroundColor = Color(1, 2),
            search = GeoMapData.Search("routeLink"),
            analytics = ScreenNameAnalytics("screenName"),
            title = "title",
            camera = GeoMapData.Camera(
                isRotateEnabled = false,
                initial = GeoMapData.Camera.Initial(center = listOf(1.0, 2.0), radius = 1f, bearing = 1f),
                restrictedArea = listOf(listOf(1.0, 2.0))
            ),
        )

        mockkStatic(AndroidColor::class)
        every { AndroidColor.red(any()) } returns 1
        every { AndroidColor.blue(any()) } returns 1
        every { AndroidColor.green(any()) } returns 1
        every { AndroidColor.alpha(any()) } returns 1
        every { AndroidColor.parseColor(any()) } returns 1

        val hash = initializer.redirectionHashFor(data.encodeToJsonElement())
        assertThat(hash.identifier).contains(preselectedLocationId)
    }
}

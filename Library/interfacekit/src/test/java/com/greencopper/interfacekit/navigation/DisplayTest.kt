package com.greencopper.interfacekit.navigation

import com.greencopper.interfacekit.mocks.TestParams
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.tabBar.TabBarData
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DisplayTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun serialize_withFeatureInfo_asDisplay() {
        val featureKey =
            FeatureKey(
                "Test.Route",
                1
            )
        val featureParams = TestParams("routingInfo" to "testing")
        val featureInfo =
            FeatureInfo(
                featureKey,
                featureParams.toJsonObject()
            )
        val displayInfo = TabBarData.Display.Embedded(feature = featureInfo)
        val displayInfoString = json.encodeToString(TabBarData.Display.serializer(), displayInfo)
        val displayInfoRestored = json.decodeFromString(TabBarData.Display.serializer(), displayInfoString)
        assertThat(displayInfoRestored).isEqualTo(displayInfo)
        assertThat(displayInfoRestored.mode).isEqualTo(TabBarData.NavigationMode.EMBEDDED)
    }

    @Test
    fun serialize_withFeatureInfo() {
        val featureKey =
            FeatureKey(
                "Test.Route",
                1
            )
        val featureParams = TestParams("routingInfo" to "testing")
        val featureInfo =
            FeatureInfo(
                featureKey,
                featureParams.toJsonObject()
            )
        val displayInfo = TabBarData.Display.Embedded(feature = featureInfo)
        val displayInfoString =
            json.encodeToString(TabBarData.Display.Embedded.serializer(), displayInfo)
        val displayInfoRestored =
            json.decodeFromString(TabBarData.Display.Embedded.serializer(), displayInfoString)
        assertThat(displayInfoRestored).isEqualTo(displayInfo)
    }

    @Test
    fun serialize_withRoutingInfo_asDisplay() {
        val featureKey =
            FeatureKey(
                "Test.Route",
                1
            )
        val featureParams = TestParams("routingInfo" to "testing")
        val featureInfo =
            FeatureInfo(
                featureKey,
                featureParams.toJsonObject()
            )
        val routingInfo =
            Route.Present(
                featureInfo
            )
        val displayInfo = TabBarData.Display.Routing(route = routingInfo)
        val displayInfoString =
            json.encodeToString(TabBarData.Display.serializer(), displayInfo)
        val displayInfoRestored =
            json.decodeFromString(TabBarData.Display.serializer(), displayInfoString)
        assertThat(displayInfoRestored).isEqualTo(displayInfo)
        assertThat(displayInfoRestored.mode).isEqualTo(TabBarData.NavigationMode.ROUTING)
    }

    @Test
    fun serialize_withRoutingInfo() {
        val featureKey =
            FeatureKey(
                "Test.Route",
                1
            )
        val featureParams = TestParams("routingInfo" to "testing")
        val featureInfo =
            FeatureInfo(
                featureKey,
                featureParams.toJsonObject()
            )
        val routingInfo =
            Route.Present(
                featureInfo
            )
        val displayInfo = TabBarData.Display.Routing(route = routingInfo)
        val displayInfoString =
            json.encodeToString(TabBarData.Display.Routing.serializer(), displayInfo)
        val displayInfoRestored =
            json.decodeFromString(TabBarData.Display.Routing.serializer(), displayInfoString)
        assertThat(displayInfoRestored).isEqualTo(displayInfo)
        assertThat(displayInfoRestored.route).usingRecursiveComparison().isEqualTo(routingInfo)
    }
}

package com.greencopper.interfacekit.navigation.feature.info

import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.commands.system.CommandInfo
import com.greencopper.interfacekit.mocks.TestParams
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RouteTest {

    init {
        Toolkit.setupTest()
    }

    private val json: Json = App.resolve()

    @Test
    fun whenSerializing_presentRoute_shouldDeserializeProperly() {
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
        val routingInfoString = json.encodeToString(Route.serializer(), routingInfo)
        val routingInfoRestored = json.decodeFromString(Route.serializer(), routingInfoString)
        assertThat(routingInfoRestored).isEqualTo(routingInfo)
    }

    @Test
    fun whenSerializing_pushRoute_shouldDeserializeProperly() {
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
            Route.Push(
                featureInfo
            )
        val routingInfoString = json.encodeToString(Route.serializer(), routingInfo)
        val routingInfoRestored = json.decodeFromString(Route.serializer(), routingInfoString)
        assertThat(routingInfoRestored).isEqualTo(routingInfo)
    }

    @Test
    fun whenSerializing_externalRoute_shouldDeserializeProperly() {
        val routingInfo = Route.External("https://greencopper.com", ScreenNameAnalytics("Greencopper"))
        val routingInfoString = json.encodeToString(Route.serializer(), routingInfo)
        val routingInfoRestored = json.decodeFromString(Route.serializer(), routingInfoString)
        assertThat(routingInfoRestored).isEqualTo(routingInfo)
    }

    @Test
    fun whenDeserializing_externalRoute_shouldWork() {
        val jsonValue =
            """{ "mode": "external",  "url": "url_key_xxx_to_be_translated",  "analytics" : { "screenName" : "GC" } } """
        val externalRouteRestored = json.decodeFromString(Route.serializer(), jsonValue)
        assertThat(externalRouteRestored).isInstanceOf(Route.External::class.java)
        assertThat((externalRouteRestored as Route.External).url).isEqualTo("url_key_xxx_to_be_translated")
    }

    @Test
    fun whenDeserializing_pushRoute_shouldWork() {
        val jsonValue =
            """{"mode":"push","feature":{"key":{"name":"Test.Route","version":1},"params":{"pairs":{"routingInfo":"testing"}}}}"""
        val pushRouteRestored = json.decodeFromString(Route.serializer(), jsonValue)
        assertThat(pushRouteRestored).isInstanceOf(Route.Push::class.java)
    }

    @Test
    fun whenDeserializing_presentRoute_shouldWork() {
        val jsonValue =
            """{"mode":"present","feature":{"key":{"name":"Test.Route","version":1},"params":{"pairs":{"routingInfo":"testing"}}}}"""
        val presentRouteRestored = json.decodeFromString(Route.serializer(), jsonValue)
        assertThat(presentRouteRestored).isInstanceOf(Route.Present::class.java)
    }

    @Test
    fun givenPushPresentRoute_getParams_returnsParams() {
        val featureParams = JsonObject(
            mapOf(
                "newParam" to json.encodeToJsonElement("newValue")
            )
        )
        val featureInfo = FeatureInfo(FeatureKey("", 1), featureParams)
        val pushRoute = Route.Push(featureInfo)
        val presentRoute = Route.Present(featureInfo)

        assertThat(pushRoute.getParams()).isEqualTo(featureParams)
        assertThat(presentRoute.getParams()).isEqualTo(featureParams)
    }

    @Test
    fun givenExternalRoute_getParams_returnsNull() {
        val externalRoute = Route.External("https://www.google.com", ScreenNameAnalytics("screeName"))
        assertThat(externalRoute.getParams()).isNull()
    }

    @Test
    fun givenPushRoute_getFeatureInfo_returnsNotNull() {
        val featureInfo = FeatureInfo(FeatureKey("", 1))
        assertThat(Route.Push(featureInfo).getFeatureInfo()).isEqualTo(featureInfo)
    }

    @Test
    fun givenPresentRoute_getFeatureInfo_returnsNotNull() {
        val featureInfo = FeatureInfo(FeatureKey("", 1))
        assertThat(Route.Present(featureInfo).getFeatureInfo()).isEqualTo(featureInfo)
    }

    @Test
    fun givenExternalRoute_getFeatureInfo_returnsNotNull() {
        assertThat(Route.External("url").getFeatureInfo()).isNull()
    }

    @Test
    fun givenExecuteRoute_getFeatureInfo_returnsNotNull() {
        assertThat(Route.Execute(CommandInfo(CommandInfo.Key("", 1))).getFeatureInfo()).isNull()
    }
}

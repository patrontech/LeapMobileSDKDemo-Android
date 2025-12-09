package com.greencopper.interfacekit.notification

import androidx.core.os.bundleOf
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.Route.Push
import com.greencopper.interfacekit.navigation.route.getRoute
import com.greencopper.interfacekit.navigation.route.putRoute
import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.Toolkit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RouteTest {
    private val bundleKey = "bundle_key_route"

    init {
        Toolkit.setupTest()
    }

    @Test
    fun writeReadRouteToBundle_success() {
        val route: Route = Push(
            feature = FeatureInfo(
                key = FeatureKey(
                    name = "Event.Schedule",
                    version = 1
                ),
                params = null
            ),
            addToBackStack = false
        )
        val bundle = bundleOf()
        bundle.putRoute(bundleKey, route)
        assertThat(bundle.getRoute(bundleKey)).isEqualTo(route)
    }

    @Test
    fun readRouteFromBundle_failSafe() {
        val bundle = bundleOf()
        bundle.putBoolean(bundleKey, false)
        assertThat(bundle.getRoute(bundleKey)).isNull()
    }
}

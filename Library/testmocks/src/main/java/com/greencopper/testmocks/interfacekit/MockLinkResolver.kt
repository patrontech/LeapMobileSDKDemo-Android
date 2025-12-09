package com.greencopper.testmocks.interfacekit

import android.net.Uri
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route
import io.mockk.mockk

public class MockLinkResolver(
    public var mockRoutes: Map<String, Route> = mutableMapOf(),
    private var mockFeatures: Map<String, FeatureInfo> = mutableMapOf(),
    private var mockUri: Uri = mockk(),
) : LinkResolver {

    override fun route(link: String, params: Map<String, String>?): Route? {
        return mockRoutes[link]
    }

    override fun routeUri(link: String, params: Map<String, String>?): Uri {
        return mockUri
    }

    override fun featureInfo(link: String, params: Map<String, String>?): FeatureInfo? {
        return mockFeatures[link]
    }
}

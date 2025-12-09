package com.greencopper.interfacekit.links.resolver

import android.net.Uri
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route

public interface LinkResolver {
    public fun route(link: String, params: Map<String, String>? = null): Route?
    public fun routeUri(link: String, params: Map<String, String>?): Uri
    public fun featureInfo(link: String, params: Map<String, String>? = null): FeatureInfo?
}
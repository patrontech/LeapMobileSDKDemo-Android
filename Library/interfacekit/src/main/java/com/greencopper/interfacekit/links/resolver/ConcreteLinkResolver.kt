package com.greencopper.interfacekit.links.resolver

import android.content.Context
import android.net.Uri
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.links.LinksConfigurationHolder
import com.greencopper.interfacekit.navigation.feature.info.FeatureInfo
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.toolkit.App
import com.greencopper.toolkit.logging.e
import com.greencopper.toolkit.serialization.substituteJsonParams
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

internal class ConcreteLinkResolver(
    private val linksConfigurationHolder: LinksConfigurationHolder,
    private val localizationService: LocalizationService,
    private val json: Json,
    context: Context,
) : LinkResolver {

    private val deeplinkScheme: String = context.getString(R.string.deeplink_scheme)

    override fun route(link: String, params: Map<String, String>?): Route? {
        val uri = routeUri(link, params)

        if (uri.scheme != deeplinkScheme) {
            return null
        }

        val route = linksConfigurationHolder.currentConfiguration.value?.let {
            it.routeLinks[uri.host]
        } ?: return null

        return try {
            KiboSerializable.decodeFromJsonElement<Route>(
                json.encodeToJsonElement(route)
                    .substituteJsonParams(uri.toParamMap(json))
            )
        } catch (throwable: Throwable) {
            App.log.e("Failed to encode $deeplinkScheme:${uri.host}", throwable = throwable)
            null
        }
    }

    override fun routeUri(link: String, params: Map<String, String>?): Uri {
        val builder = Uri.parse(localizationService.getString(link))
            .buildUpon()

        if (params != null) {
            for (key in params.keys) {
                builder.appendQueryParameter(key, params[key])
            }
        }

        return builder.build()
    }

    override fun featureInfo(link: String, params: Map<String, String>?): FeatureInfo? {
        val uri = routeUri(link, params)

        if (uri.scheme != deeplinkScheme) {
            return null
        }

        val featureInfo = linksConfigurationHolder.currentConfiguration.value?.let {
            it.featureLinks[uri.host]
        } ?: return null

        return try {
            KiboSerializable.decodeFromJsonElement<FeatureInfo>(
                json.encodeToJsonElement(featureInfo)
                    .substituteJsonParams(uri.toParamMap(json))
            )
        } catch (throwable: Throwable) {
            App.log.e("Failed to encode $deeplinkScheme://${uri.host}", throwable = throwable)
            null
        }
    }
}

private fun Uri.toParamMap(json: Json): Map<String, JsonElement> {
    return queryParameterNames.mapNotNull {
        getQueryParameter(it)?.let { value ->
            try {
                it to json.parseToJsonElement(value)
            } catch (throwable: Throwable) {
                it to json.parseToJsonElement("\"$value\"")
            }
        }
    }.toMap()
}

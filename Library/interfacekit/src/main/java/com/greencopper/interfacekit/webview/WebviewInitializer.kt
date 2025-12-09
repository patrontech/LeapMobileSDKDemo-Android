package com.greencopper.interfacekit.webview

import androidx.core.util.PatternsCompat
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.webview.data.WebViewBaseData
import com.greencopper.interfacekit.webview.data.WebViewData
import com.greencopper.interfacekit.webview.data.WebViewNewWindowMode
import com.greencopper.interfacekit.webview.ui.WebViewFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class WebviewInitializer internal constructor(
    private val localStorage: LocalStorage,
    private val localizationService: LocalizationService
) : ParameterizedFeatureInitializer<WebViewData>() {

    public companion object {
        public val key: FeatureKey = FeatureKey("InterfaceKit.WebView", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): WebViewData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: WebViewData): Layout {
        var localizedUrl = localizationService.getString(params.url)
        localizedUrl = localStorage.replaceUrlParameters(localizedUrl)

        val layoutDataUrl = if (PatternsCompat.WEB_URL.matcher(localizedUrl).matches()) {
            localizedUrl
        } else {
            params.url
        }

        return WebViewFragment(
            WebViewLayoutData(
                url = layoutDataUrl,
                newWindowMode = params.newWindowMode ?: WebViewNewWindowMode.External,
                analytics = params.analytics,
                redirectionHash = redirectionHashForParams(params)
            )
        )
    }

    override fun redirectionHashForParams(params: WebViewData): RedirectionHash =
        RedirectionHash(key, params.url)
}

@Serializable
public data class WebViewLayoutData(
    override val url: String,
    val newWindowMode: WebViewNewWindowMode,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash
) : WebViewBaseData<WebViewLayoutData> {
    override fun getSerializer(): KSerializer<WebViewLayoutData> = serializer()
}

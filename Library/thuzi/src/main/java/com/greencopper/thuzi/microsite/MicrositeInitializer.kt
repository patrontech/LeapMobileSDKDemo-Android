package com.greencopper.thuzi.microsite

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
import com.greencopper.thuzi.microsite.ui.MicrositeFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class MicrositeInitializer(
    private val localStorage: LocalStorage,
    private val localizationService: LocalizationService,
) : ParameterizedFeatureInitializer<MicrositeData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Thuzi.Microsite", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): MicrositeData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: MicrositeData): Layout {
        var localizedUrl = localizationService.getString(params.url)
        localizedUrl = localStorage.replaceUrlParameters(localizedUrl)

        val layoutDataUrl = if (PatternsCompat.WEB_URL.matcher(localizedUrl).matches()) {
            localizedUrl
        } else {
            params.url
        }

        return MicrositeFragment(
            MicrositeLayoutData(
                url = layoutDataUrl,
                analytics = params.analytics,
                redirectionHash = redirectionHashForParams(params),
            )
        )
    }

    override fun redirectionHashForParams(params: MicrositeData): RedirectionHash =
        RedirectionHash(key, params.url)
}

@Serializable
internal class MicrositeData(
    val url: String,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<MicrositeData> {
    override fun getSerializer(): KSerializer<MicrositeData> = serializer()
}

@Serializable
internal class MicrositeLayoutData(
    override var url: String,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : WebViewBaseData<MicrositeLayoutData> {
    override fun getSerializer(): KSerializer<MicrositeLayoutData> = serializer()
}

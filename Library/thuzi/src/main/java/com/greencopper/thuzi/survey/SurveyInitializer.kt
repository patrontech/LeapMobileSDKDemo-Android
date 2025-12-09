package com.greencopper.thuzi.survey

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.webview.data.WebViewBaseData
import com.greencopper.thuzi.survey.ui.SurveyFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class SurveyInitializer(
    private val localizationService: LocalizationService,
    private val localStorage: LocalStorage,
) : ParameterizedFeatureInitializer<SurveyData>() {

    companion object {
        val key: FeatureKey = FeatureKey("Thuzi.Survey", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): SurveyData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: SurveyData): Layout {
        val localizedUrl = localizationService.getString(params.url)
        return SurveyFragment(
            SurveyWebViewLayoutData(
                url = localStorage.replaceUrlParameters(localizedUrl),
                analytics = params.analytics,
                redirectionHash = redirectionHashForParams(params)
            )
        )
    }

    override fun redirectionHashForParams(params: SurveyData): RedirectionHash =
        RedirectionHash(key, params.url)
}

@Serializable
internal class SurveyData(
    val url: String, val analytics: Analytics
) : KiboSerializable<SurveyData> {

    override fun getSerializer(): KSerializer<SurveyData> = serializer()

    @Serializable
    class Analytics(val screenName: String, val itemId: String)
}

@Serializable
internal class SurveyWebViewLayoutData(
    override var url: String,
    val analytics: SurveyData.Analytics,
    val redirectionHash: RedirectionHash
) : WebViewBaseData<SurveyWebViewLayoutData> {

    override fun getSerializer(): KSerializer<SurveyWebViewLayoutData> = serializer()
}

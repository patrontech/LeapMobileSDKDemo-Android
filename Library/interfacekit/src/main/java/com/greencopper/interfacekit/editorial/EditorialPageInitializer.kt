package com.greencopper.interfacekit.editorial

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.editorial.repository.EditorialPageRepository
import com.greencopper.interfacekit.editorial.ui.EditorialPageFragment
import com.greencopper.interfacekit.navigation.feature.FeatureInitializerException
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.webview.data.WebViewBaseData
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class EditorialPageInitializer(
    private val repository: EditorialPageRepository
) : ParameterizedFeatureInitializer<EditorialPageData>() {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.EditorialPage", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): EditorialPageData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: EditorialPageData): Layout {
        val fileUri = repository.getFileUri(params.fileName)
            ?: throw FeatureInitializerException.ParametersNotValid(params.encodeToJsonElement())

        return EditorialPageFragment(
            EditorialPageLayoutData(
                url = fileUri.toString(),
                analytics = params.analytics,
                redirectionHash = redirectionHashForParams(params)
            )
        )
    }

    override fun redirectionHashForParams(params: EditorialPageData): RedirectionHash =
        RedirectionHash(key, params.fileName)
}

@Serializable
internal class EditorialPageData(
    val fileName: String,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<EditorialPageData> {

    override fun getSerializer(): KSerializer<EditorialPageData> = serializer()
}

@Serializable
internal class EditorialPageLayoutData(
    override var url: String,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash
) : WebViewBaseData<EditorialPageLayoutData> {
    override fun getSerializer(): KSerializer<EditorialPageLayoutData> = serializer()
}

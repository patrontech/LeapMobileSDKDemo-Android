package com.greencopper.interfacekit.widgets

import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ScreenNameAnalytics
import com.greencopper.interfacekit.color.DefaultColors
import com.greencopper.interfacekit.navigation.feature.ParameterizedFeatureInitializer
import com.greencopper.interfacekit.navigation.feature.info.FeatureKey
import com.greencopper.interfacekit.navigation.feature.info.FeatureParams
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.layout.RedirectionHash
import com.greencopper.interfacekit.topbar.TopBarData
import com.greencopper.interfacekit.widgets.resolver.WidgetCollectionResolver
import com.greencopper.interfacekit.widgets.ui.widgetcollection.WidgetCollectionFragment
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class WidgetCollectionInitializer(
    private val widgetCollectionResolver: WidgetCollectionResolver,
) : ParameterizedFeatureInitializer<WidgetCollectionData>() {

    companion object {
        val key: FeatureKey = FeatureKey("InterfaceKit.WidgetCollection", 1)
    }

    override val featureKey: FeatureKey = key

    override fun decodeParams(params: FeatureParams): WidgetCollectionData =
        KiboSerializable.decodeFromJsonElement(params)

    override fun layoutForParams(params: WidgetCollectionData): Layout {
        val instance = widgetCollectionResolver.resolve(params.name)
        val widgetCollection = instance?.widgets
            ?: throw IllegalArgumentException("Couldn't find Widget Collection with name ${params.name}")

        val widgetCollectionLayoutData = WidgetCollectionLayoutData(
            header = instance.header,
            widgets = widgetCollection,
            analytics = params.analytics,
            redirectionHash = redirectionHashForParams(params),
            statusBarColor = params.statusBarColor,
            topBar = params.topBar,
        )

        return WidgetCollectionFragment(widgetCollectionLayoutData)
    }

    override fun redirectionHashForParams(params: WidgetCollectionData): RedirectionHash =
        RedirectionHash(key, params.name)
}

@Serializable
internal data class WidgetCollectionData(
    val name: String,
    val statusBarColor: DefaultColors.StatusBar.Style? = null,
    val topBar: TopBarData? = null,
    val analytics: ScreenNameAnalytics,
) : KiboSerializable<WidgetCollectionData> {

    override fun getSerializer(): KSerializer<WidgetCollectionData> = serializer()
}

@Serializable
internal data class WidgetCollectionLayoutData(
    val header: WidgetCollectionConfiguration.Instance.HeaderInfo?,
    val widgets: List<WidgetCollectionConfiguration.Instance.WidgetInfo>,
    val statusBarColor: DefaultColors.StatusBar.Style?,
    val topBar: TopBarData?,
    val analytics: ScreenNameAnalytics,
    val redirectionHash: RedirectionHash,
) : KiboSerializable<WidgetCollectionLayoutData> {
    override fun getSerializer(): KSerializer<WidgetCollectionLayoutData> = serializer()
}

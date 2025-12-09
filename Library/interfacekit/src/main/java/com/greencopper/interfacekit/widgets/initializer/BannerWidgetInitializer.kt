package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.ui.bannerwidget.BannerWidgetGenerator
import com.greencopper.interfacekit.widgets.ui.bannerwidget.BannerWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class BannerWidgetInitializer(
    private val routeController: RouteController,
    private val metrics: AggregateMetricsService,
) : WidgetInitializer {

    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.Banner", version = 1)
        const val widgetCategory = "banner_widget"
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = BannerWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<BannerWidgetParameters>(jsonWidgetParams)
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        val resolvedParams = resolveParams(jsonWidgetParams) as BannerWidgetParameters
        return BannerWidgetGenerator(
            routeController = routeController,
            metrics = metrics,
            screenName = screenName,
            params = resolvedParams,
            origin = origin,
        )
    }
}

@Serializable
internal data class BannerWidgetParameters(
    val title: String,
    val subtitle: String? = null,
    val button: Button? = null,
) : KiboSerializable<BannerWidgetParameters> {

    override fun getSerializer(): KSerializer<BannerWidgetParameters> = serializer()

    @Serializable
    data class Button(val text: String, val icon: String, val onTap: OnTap)

    @Serializable
    data class OnTap(val routeLink: String, val analytics: ItemNameAnalytics)

    @Serializable
    data class Image(val light: String, val dark: String? = null)
}

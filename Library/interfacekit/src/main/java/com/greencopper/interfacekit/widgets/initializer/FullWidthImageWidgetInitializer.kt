package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.ui.fullwidthwidget.FullWidthImageWidgetGenerator
import com.greencopper.interfacekit.widgets.ui.fullwidthwidget.FullWidthImageWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class FullWidthImageWidgetInitializer(
    private val routeController: RouteController,
    private val metrics: AggregateMetricsService,
) : WidgetInitializer {

    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.FullWidthImage", version = 1)
        const val widgetCategory = "image_widget"
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = FullWidthImageWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<FullWidthImageWidgetParameters>(jsonWidgetParams)
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        val resolvedParams = resolveParams(jsonWidgetParams) as FullWidthImageWidgetParameters
        return FullWidthImageWidgetGenerator(
            routeController = routeController,
            metrics = metrics,
            screenName = screenName,
            params = resolvedParams,
            origin = origin,
        )
    }
}

@Serializable
internal data class FullWidthImageWidgetParameters(
    val image: Image,
    val accessibilityLabel: String? = null,
    val analytics: ItemNameAnalytics,
    val onTap: Route? = null,
) : KiboSerializable<FullWidthImageWidgetParameters> {

    override fun getSerializer(): KSerializer<FullWidthImageWidgetParameters> = serializer()

    @Serializable
    data class Image(val light: String, val dark: String? = null)
}

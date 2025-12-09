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
import com.greencopper.interfacekit.widgets.ui.fullwidthimagecarousel.FullWidthImageCarouselWidgetGenerator
import com.greencopper.interfacekit.widgets.ui.fullwidthimagecarousel.FullWidthImageCarouselWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class FullWidthImageCarouselWidgetInitializer(
    private val routeController: RouteController,
    private val metrics: AggregateMetricsService,
) : WidgetInitializer {

    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.FullWidthImageCarousel", version = 1)
        val widgetCategory = "full_width_image_carousel_widget"
    }

    override val key: WidgetKey = Companion.key

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<FullWidthImageCarouselWidgetParameters>(jsonWidgetParams)
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveLayout(context: Context): WidgetLayout<*> = FullWidthImageCarouselWidgetLayout(context)

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout
    ): WidgetGenerator {
        val params = resolveParams(jsonWidgetParams) as FullWidthImageCarouselWidgetParameters
        return FullWidthImageCarouselWidgetGenerator(
            routeController = routeController,
            metrics = metrics,
            screenName = screenName,
            params = params,
            origin = origin,
        )
    }
}

@Serializable
internal data class FullWidthImageCarouselWidgetParameters(
    val accessibilityLabel: String?,
    val images: List<Image>,
    val ratio: Float,
) : KiboSerializable<FullWidthImageCarouselWidgetParameters> {
    @Serializable
    data class Image(
        val imageName: String,
        val accessibilityLabel: String,
        val onTap: OnTap? = null,
    )

    @Serializable
    data class OnTap(
        val routeLink: String,
        val analytics: ItemNameAnalytics,
    )

    override fun getSerializer(): KSerializer<FullWidthImageCarouselWidgetParameters> = serializer()
}

package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.ui.cardcollectionwidget.CardCollectionWidgetGenerator
import com.greencopper.interfacekit.widgets.ui.cardcollectionwidget.CardCollectionWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class CardCollectionWidgetInitializer(
    private val routeController: RouteController,
    private val metrics: AggregateMetricsService,
) : WidgetInitializer {

    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.CardsCollection", version = 1)
        val widgetCategory = "cards_collection_widget"
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = CardCollectionWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<CardCollectionWidgetParameters>(jsonWidgetParams)
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout
    ): WidgetGenerator {
        val resolvedParams = resolveParams(jsonWidgetParams) as CardCollectionWidgetParameters
        return CardCollectionWidgetGenerator(
            routeController = routeController,
            metrics = metrics,
            screenName = screenName,
            params = resolvedParams,
            origin = origin,
        )
    }
}

@Serializable
internal data class CardCollectionWidgetParameters(
    val title: String? = null,
    val items: List<Item>,
) : KiboSerializable<CardCollectionWidgetParameters> {
    override fun getSerializer(): KSerializer<CardCollectionWidgetParameters> = serializer()

    @Serializable
    data class Item(
        val style: Style,
        val label: String? = null,
        val accessibilityLabel: String,
        val onTap: String? = null,
        val analytics: Analytics,
    )

    @Serializable
    data class Style(
        val type: CardCollectionItemStyle,
        val backgroundImage: String? = null,
        val icon: String? = null,
    )

    @Serializable
    data class Analytics(
        val itemName: String,
        val itemId: String? = null,
    )
}

@Serializable
internal enum class CardCollectionItemStyle {
    image, icon
}

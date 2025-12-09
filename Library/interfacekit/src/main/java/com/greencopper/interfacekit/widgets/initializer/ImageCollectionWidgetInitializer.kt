package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.imagecollectionwidget.ImageCollectionWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ImageCollectionWidgetInitializer : WidgetInitializer {
    companion object {
        val key = WidgetKey(name = "InterfaceKit.Widget.ImageCollection", version = 1)
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = ImageCollectionWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            val decodedParams = decodeFromJsonElement<ImageCollectionWidgetParameters>(jsonWidgetParams)
            require(decodedParams.items.isNotEmpty())
            decodedParams
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        unimplemented()
    }
}

@Serializable
public data class ImageCollectionWidgetParameters(
    val title: String? = null,
    val items: List<Item>,
) : KiboSerializable<ImageCollectionWidgetParameters> {

    override fun getSerializer(): KSerializer<ImageCollectionWidgetParameters> = serializer()

    @Serializable
    public data class Item(
        val imageName: String,
        val label: String? = null,
        val accessibilityName: String? = null,
        val onTap: OnTap,
    ) {
        @Serializable
        public data class OnTap(
            val routeLink: String,
            val analytics: ItemNameAnalytics,
        )
    }
}

package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.imagewithlabelwidget.ImageWithLabelWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ImageWithLabelWidgetInitializer : WidgetInitializer {
    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.ImageWithLabel", version = 1)
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = ImageWithLabelWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<ImageWithLabelWidgetParameters>(jsonWidgetParams)
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
internal data class ImageWithLabelWidgetParameters(
    val imageName: String,
    val title: String,
    val body: String? = null,
    val onTap: OnTap? = null,
    val accessibilityLabel: String? = null,
) : KiboSerializable<ImageWithLabelWidgetParameters> {

    override fun getSerializer(): KSerializer<ImageWithLabelWidgetParameters> = serializer()

    @Serializable
    data class OnTap(
        val routeLink: String,
        val analytics: ItemNameAnalytics,
    )
}

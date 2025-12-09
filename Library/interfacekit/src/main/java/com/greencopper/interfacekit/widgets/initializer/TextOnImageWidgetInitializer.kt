package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.textonimagewidget.TextOnImageWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class TextOnImageWidgetInitializer : WidgetInitializer {
    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.TextOnImage", version = 1)
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = TextOnImageWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<TextOnImageWidgetParameters>(jsonWidgetParams)
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
internal data class TextOnImageWidgetParameters(
    val imageName: String,
    val title: Text,
    val body: Text? = null,
    val onTap: OnTap? = null,
    val accessibilityLabel: String? = null,
) : KiboSerializable<TextOnImageWidgetParameters> {

    @Serializable
    data class Text(
        val text: String,
        val color: Color,
    )

    @Serializable
    data class OnTap(
        val routeLink: String,
        val analytics: ItemNameAnalytics,
    )

    override fun getSerializer(): KSerializer<TextOnImageWidgetParameters> = serializer()
}

package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.linkscollectionwidget.LinksCollectionWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class LinksCollectionWidgetInitializer : WidgetInitializer {
    companion object {
        val key = WidgetKey(name = "InterfaceKit.Widget.LinksCollection", version = 1)
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = LinksCollectionWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<LinksCollectionWidgetParameters>(jsonWidgetParams)
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
internal data class LinksCollectionWidgetParameters(
    val title: String? = null,
    val links: List<Link>,
) : KiboSerializable<LinksCollectionWidgetParameters> {

    override fun getSerializer(): KSerializer<LinksCollectionWidgetParameters> = serializer()

    @Serializable
    data class Link(
        val icon: Icon,
        val text: String? = null,
        val accessibilityLabel: String? = null,
        val onTap: String,
        val analytics: ItemNameAnalytics,
    ) {
        @Serializable
        data class Icon(val shouldColor: Boolean, val light: String, val dark: String = light)
    }
}

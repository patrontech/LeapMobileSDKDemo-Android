package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.links.resolver.LinkResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.Route
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.ui.buttonwidget.ButtonWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class ButtonWidgetInitializer : WidgetInitializer {
    override val key: WidgetKey = Companion.key
    override fun resolveLayout(context: Context): WidgetLayout<*> =
        ButtonWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            decodeFromJsonElement<ButtonWidgetParameters>(jsonWidgetParams)
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

    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.Button", version = 1)
    }
}

@Serializable
internal data class ButtonWidgetParameters(
    val iconName: String? = null,
    val text: String,
    val colors: ButtonWidgetColors? = null,
    val analytics: ItemNameAnalytics,
    val onTap: Route,
) : KiboSerializable<ButtonWidgetParameters> {

    override fun getSerializer(): KSerializer<ButtonWidgetParameters> = serializer()
}

@Serializable
internal data class ButtonWidgetV2Parameters(
    val iconName: String? = null,
    val text: String,
    val colors: ButtonWidgetColors? = null,
    val analytics: ItemNameAnalytics,
    val onTap: String,
) : KiboSerializable<ButtonWidgetV2Parameters> {
    override fun getSerializer(): KSerializer<ButtonWidgetV2Parameters> = serializer()
}

@Serializable
internal data class ButtonWidgetColors(
    val background: Color?,
    val border: Color?,
    val title: Color?,
    val image: Color?,
) : KiboSerializable<ButtonWidgetColors> {
    override fun getSerializer(): KSerializer<ButtonWidgetColors> = serializer()
}

internal class ButtonWidgetV2Initializer(
    private val linkResolver: LinkResolver,
) : WidgetInitializer {
    override val key: WidgetKey = ButtonWidgetV2Initializer.key

    override fun resolveLayout(context: Context): WidgetLayout<*> =
        ButtonWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            val params = decodeFromJsonElement<ButtonWidgetV2Parameters>(jsonWidgetParams)
            val onTap = linkResolver.route(params.onTap)
                ?: throw WidgetException.InvalidParametersProvided(jsonWidgetParams)

            ButtonWidgetParameters(
                params.iconName,
                params.text,
                params.colors,
                params.analytics,
                onTap,
            )
        } catch (t: Throwable) {
            when (t) {
                is WidgetException -> throw t
                else -> throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
            }
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        unimplemented()
    }

    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.Button", version = 2)
    }
}

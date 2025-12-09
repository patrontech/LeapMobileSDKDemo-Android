package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.debuginfowidget.DebugInfoWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class DebugInfoWidgetInitializer : WidgetInitializer {
    override val key: WidgetCollectionConfiguration.Instance.WidgetKey = Companion.key
    override fun resolveLayout(context: Context): WidgetLayout<*> =
        DebugInfoWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            KiboSerializable.decodeFromJsonElement<DebugInfoWidgetParameters>(jsonWidgetParams)
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
        val key: WidgetCollectionConfiguration.Instance.WidgetKey =
            WidgetCollectionConfiguration.Instance.WidgetKey(name = "InterfaceKit.Widget.DebugInfo", version = 1)
    }
}

@Serializable
public data class DebugInfoWidgetParameters(
    val morseRoute: MorseRoute? = null,
    val infos: List<Info>,
) : KiboSerializable<DebugInfoWidgetParameters> {

    override fun getSerializer(): KSerializer<DebugInfoWidgetParameters> = serializer()

    @Serializable
    public data class MorseRoute(
        val routeLink: String,
        val sequence: String,
    )

    @Serializable
    public data class Info(val type: String, val label: String? = null, val key: String)
}

package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.color.Color
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.setting.SettingWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class SettingWidgetInitializer : WidgetInitializer {
    override val key: WidgetCollectionConfiguration.Instance.WidgetKey = Companion.key
    override fun resolveLayout(context: Context): WidgetLayout<*> =
        SettingWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            KiboSerializable.decodeFromJsonElement<SettingWidgetParameters>(jsonWidgetParams)
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
            WidgetCollectionConfiguration.Instance.WidgetKey(name = "InterfaceKit.Widget.Setting", version = 1)
    }
}

@Serializable
public data class SettingWidgetParameters(
    val title: Label,
    val subtitle: Label? = null,
    val onTap: OnTap,
) : KiboSerializable<SettingWidgetParameters> {

    override fun getSerializer(): KSerializer<SettingWidgetParameters> = serializer()

    @Serializable
    public data class OnTap(val routeLink: String, val analytics: ItemNameAnalytics)

    @Serializable
    public data class Label(val label: String, val color: Color? = null)

}

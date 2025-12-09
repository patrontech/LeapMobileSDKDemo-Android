package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.di.binding.*

public interface WidgetInitializer {
    public fun resolveLayout(context: Context): WidgetLayout<*>
    public fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters
    public fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator
    public val key: WidgetCollectionConfiguration.Instance.WidgetKey
}

public fun <T: WidgetInitializer> Registrar.bindWidget(
    widgetKey: WidgetCollectionConfiguration.Instance.WidgetKey,
    creator: Creator<T>
) {
    bindProvider<WidgetInitializer>(tag = widgetKey, creator)
}

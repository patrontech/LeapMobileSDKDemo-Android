package com.greencopper.interfacekit.widgets.resolver

import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.WidgetCollectionConfigurationHolder

public class WidgetCollectionResolver(
    private val widgetCollectionConfigurationHolder: WidgetCollectionConfigurationHolder
) {

    public fun resolve(widgetCollectionKey: String): WidgetCollectionConfiguration.Instance? =
        widgetCollectionConfigurationHolder.currentConfiguration.value?.instances?.get(
            widgetCollectionKey
        )
}
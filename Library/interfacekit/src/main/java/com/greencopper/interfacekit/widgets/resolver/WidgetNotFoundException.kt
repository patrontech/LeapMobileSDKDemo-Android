package com.greencopper.interfacekit.widgets.resolver

import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey

public class WidgetNotFoundException(widgetKey: WidgetKey) : Throwable() {
    override val message: String = "Widget couldn't be resolved for key $widgetKey "
}

package com.greencopper.interfacekit.widgets.resolver

import android.content.Context
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetInfo
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.initializer.WidgetInitializer
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.di.resolver.*

public interface WidgetResolver {
    public fun resolveLayout(widgetKey: WidgetKey, context: Context): WidgetLayout<*>
    public fun resolveGenerator(widgetInfo: WidgetInfo, screenName: String, origin: Layout): WidgetGenerator
    public fun resolveParams(widgetInfo: WidgetInfo): WidgetParameters
    public fun resolveWidgets(): List<WidgetKey>
}

internal class ConcreteWidgetResolver(
    private val resolver: Resolver,
) : WidgetResolver {
    override fun resolveLayout(widgetKey: WidgetKey, context: Context): WidgetLayout<*> {
        val initializer =
            resolver.tryResolve<WidgetInitializer>(tag = widgetKey) ?: throw WidgetNotFoundException(widgetKey)
        return initializer.resolveLayout(context)
    }

    override fun resolveParams(widgetInfo: WidgetInfo): WidgetParameters {
        val initializer = resolver.tryResolve<WidgetInitializer>(tag = widgetInfo.key) ?: throw WidgetNotFoundException(
            widgetInfo.key
        )
        return initializer.resolveParams(widgetInfo.params)
    }

    override fun resolveGenerator(widgetInfo: WidgetInfo, screenName: String, origin: Layout): WidgetGenerator {
        val initializer = resolver.tryResolve<WidgetInitializer>(tag = widgetInfo.key) ?: throw WidgetNotFoundException(
            widgetInfo.key
        )
        return initializer.resolveGenerator(widgetInfo.params, screenName, origin)
    }

    override fun resolveWidgets(): List<WidgetKey> {
        return resolver.resolveAll<WidgetInitializer>(allowSubclasses = true).map { it.key }
    }
}

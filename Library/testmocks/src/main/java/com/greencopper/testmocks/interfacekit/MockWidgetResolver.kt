package com.greencopper.testmocks.interfacekit

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetInfo
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.resolver.WidgetResolver
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class MockWidgetResolver(
    public var widgetLayout: WidgetLayout<*>? = null,
    public var widgetParameters: WidgetParameters? = MockWidgetParameters(),
    public var widgetsAvailable: List<WidgetKey> = listOf(),
    public var resolveGenerator: ((
        WidgetInfo,
        screenName: String,
        origin: Layout,
    ) -> WidgetGenerator)? = null,
) : WidgetResolver {
    override fun resolveLayout(
        widgetKey: WidgetKey,
        context: Context,
    ): WidgetLayout<*> {
        return widgetLayout as WidgetLayout
    }

    override fun resolveGenerator(
        widgetInfo: WidgetInfo,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        return resolveGenerator?.invoke(widgetInfo, screenName, origin) ?: unimplemented()
    }

    override fun resolveParams(widgetInfo: WidgetInfo): WidgetParameters =
        widgetParameters ?: throw RuntimeException("widgetParameters can't be null")

    override fun resolveWidgets(): List<WidgetKey> =
        widgetsAvailable

}

@Serializable
public data class MockWidgetParameters(val string: String? = null) : KiboSerializable<MockWidgetParameters> {
    override fun getSerializer(): KSerializer<MockWidgetParameters> = serializer()
}

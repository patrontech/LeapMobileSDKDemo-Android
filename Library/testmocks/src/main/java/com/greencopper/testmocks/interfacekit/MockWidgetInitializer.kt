package com.greencopper.testmocks.interfacekit

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.initializer.WidgetInitializer
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import io.mockk.mockk
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class MockWidgetInitializer(
    public var mockLayout: () -> WidgetLayout<*> = { mockk() },
    public var mockParams: (jsonWidgetParams: JsonWidgetParameters?) -> WidgetParameters = {
        MockWidgetParams("test")
    },
    public var mockGenerator: ((
        jsonWidgetParams: JsonWidgetParameters?,
        screenName: String,
        origin: Layout,
    ) -> WidgetGenerator)? = null,
) : WidgetInitializer {

    public var layoutsResolved: Int = 0

    override val key: WidgetKey = WidgetKey("mockWidget", 1)

    override fun resolveLayout(context: Context): WidgetLayout<*> {
        layoutsResolved += 1
        return mockLayout()
    }

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters = mockParams(jsonWidgetParams)
    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        return mockGenerator?.invoke(jsonWidgetParams, screenName, origin) ?: unimplemented()
    }
}

@Serializable
public data class MockWidgetParams(val name: String) : KiboSerializable<MockWidgetParams> {
    override fun getSerializer(): KSerializer<MockWidgetParams> = serializer()
}

package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.counter.Counter.Key
import com.greencopper.interfacekit.counter.CounterParameters
import com.greencopper.interfacekit.counter.CounterResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.titlecounterwidget.TitleCounterWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class TitleCounterWidgetInitializer(private val counterResolver: CounterResolver) : WidgetInitializer {

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> =
        TitleCounterWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            KiboSerializable.decodeFromJsonElement<TitleCounterWidgetParameters>(jsonWidgetParams).also {
                if (counterResolver.resolve(it.counter.key, it.counter.params) == null) {
                    throw WidgetException.InvalidParametersProvided(jsonWidgetParams)
                }
            }
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

    public companion object {
        public val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.TitleCounter", version = 1)
    }
}

@Serializable
internal data class TitleCounterWidgetParameters(
    val icon: String,
    val title: String,
    val onTap: OnTap,
    val counter: Counter,
) : KiboSerializable<TitleCounterWidgetParameters> {

    override fun getSerializer(): KSerializer<TitleCounterWidgetParameters> = serializer()

    @Serializable
    data class OnTap(val routeLink: String, val analytics: ItemNameAnalytics)

    @Serializable
    data class Counter(val key: Key, val params: CounterParameters)
}

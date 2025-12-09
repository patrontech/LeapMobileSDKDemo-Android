package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.ItemNameAnalytics
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.accountsummarywidget.AccountSummaryWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

internal class AccountSummaryWidgetInitializer : WidgetInitializer {
    override val key: WidgetCollectionConfiguration.Instance.WidgetKey = Companion.key
    override fun resolveLayout(context: Context): WidgetLayout<*> =
        AccountSummaryWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            KiboSerializable.decodeFromJsonElement<AccountSummaryWidgetParameters>(jsonWidgetParams)
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
            WidgetCollectionConfiguration.Instance.WidgetKey(name = "InterfaceKit.Widget.AccountSummary", version = 1)
    }
}

@Serializable
public data class AccountSummaryWidgetParameters(
    val provider: Provider,
    val button: Button,
) : KiboSerializable<AccountSummaryWidgetParameters> {

    override fun getSerializer(): KSerializer<AccountSummaryWidgetParameters> = serializer()

    @Serializable
    public data class Button(val title: String, val onTap: OnTap)

    @Serializable
    public data class OnTap(val routeLink: String, val analytics: ItemNameAnalytics)

    @Serializable
    public data class Provider(val key: AccountProvider.Key, val params: AccountProvider.AccountProviderParams? = null)
}

package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.accountprofilewidget.AccountProfileWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable

public class AccountProfileWidgetInitializer : WidgetInitializer {
    override val key: WidgetCollectionConfiguration.Instance.WidgetKey = Companion.key
    override fun resolveLayout(context: Context): WidgetLayout<*> =
        AccountProfileWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            KiboSerializable.decodeFromJsonElement<AccountProfileWidgetParameters>(jsonWidgetParams)
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
        public val key: WidgetCollectionConfiguration.Instance.WidgetKey =
            WidgetCollectionConfiguration.Instance.WidgetKey(name = "InterfaceKit.Widget.AccountProfile", version = 1)
    }
}

@Serializable
public data class AccountProfileWidgetParameters(
    val provider: Provider,
    val infoToDisplay: List<Info>,
) : KiboSerializable<AccountProfileWidgetParameters> {

    override fun getSerializer(): KSerializer<AccountProfileWidgetParameters> = serializer()

    @Serializable
    public data class Info(val label: String, val key: String)

    @Serializable
    public data class Provider(val key: AccountProvider.Key, val params: AccountProvider.AccountProviderParams? = null)
}

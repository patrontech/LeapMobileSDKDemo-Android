package com.greencopper.interfacekit.widgets.initializer

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.greencopper.core.content.serializers.ZonedDateTimeWithInstantSerializer
import com.greencopper.core.data.KiboSerializable
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration
import com.greencopper.interfacekit.widgets.ui.*
import com.greencopper.interfacekit.widgets.ui.countdownwidget.CountdownWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import com.greencopper.toolkit.extensions.toZonedDateTime
import com.greencopper.toolkit.testing.unimplemented
import kotlinx.serialization.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

public class CountdownWidgetInitializer : WidgetInitializer {
    override val key: WidgetCollectionConfiguration.Instance.WidgetKey = Companion.key
    override fun resolveLayout(context: Context): WidgetLayout<*> =
        CountdownWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            KiboSerializable.decodeFromJsonElement<CountdownWidgetParameters>(jsonWidgetParams)
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
            WidgetCollectionConfiguration.Instance.WidgetKey(name = "InterfaceKit.Widget.Countdown", version = 1)
    }
}

@VisibleForTesting
internal val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

@Serializable
public data class CountdownWidgetParameters(
    @SerialName("endDate")
    private val endDateString: String,
    val showDate: Boolean,
    val title: String,
    val afterCountdown: AfterCountdown? = null,
    val backgroundColor: String? = null,
    val backgroundImage: String? = null,
    val textColor: String,
    val digitBackgroundColor: String? = null,
) : KiboSerializable<CountdownWidgetParameters> {

    @Serializable(with = ZonedDateTimeWithInstantSerializer::class)
    val endDateTime: ZonedDateTime = endDateString.toZonedDateTime() ?: throw WidgetException.ParametersCastFailed(this)

    override fun getSerializer(): KSerializer<CountdownWidgetParameters> = serializer()

    @Serializable
    public data class AfterCountdown(
        val title: String? = null,
        val subtitle: String? = null,
    )
}

package com.greencopper.interfacekit.widgets.initializer

import CardAdWidgetGenerator
import android.content.Context
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.KiboSerializable.Companion.decodeFromJsonElement
import com.greencopper.core.metrics.ItemNameIdAnalytics
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.utils.Weighted
import com.greencopper.interfacekit.utils.randomByWeight
import com.greencopper.interfacekit.widgets.JsonWidgetParameters
import com.greencopper.interfacekit.widgets.WidgetCollectionConfiguration.Instance.WidgetKey
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.interfacekit.widgets.ui.WidgetParameters
import com.greencopper.interfacekit.widgets.ui.cardadwidget.CardAdWidgetLayout
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal class CardAdWidgetInitializer(
    private val routeController: RouteController,
    private val metrics: AggregateMetricsService,
    private val imageService: ImageService,
) : WidgetInitializer {
    companion object {
        val key: WidgetKey = WidgetKey(name = "InterfaceKit.Widget.CardAd", version = 1)
        const val widgetCategory = "card_ad_widget"
    }

    override val key: WidgetKey = Companion.key

    override fun resolveLayout(context: Context): WidgetLayout<*> = CardAdWidgetLayout(context)

    override fun resolveParams(jsonWidgetParams: JsonWidgetParameters?): WidgetParameters {
        jsonWidgetParams ?: throw WidgetException.NoParametersProvided()
        return try {
            val params = decodeFromJsonElement<CardAdWidgetParameters>(jsonWidgetParams)

            getValidAd(params.ads)?.let {
                CardAdWidgetLayoutParameters(
                    it.image,
                    it.onTapRouteLink,
                    it.accessibilityLabel,
                    it.analytics,
                )
            } ?: throw WidgetException.InvalidParametersProvided(jsonWidgetParams)
        } catch (throwable: WidgetException) {
            throw throwable
        } catch (t: Throwable) {
            throw WidgetException.ParametersDecodeFailed(jsonWidgetParams)
        }
    }

    override fun resolveGenerator(
        jsonWidgetParams: JsonWidgetParameters,
        screenName: String,
        origin: Layout,
    ): WidgetGenerator {
        val resolvedParams = resolveParams(jsonWidgetParams) as CardAdWidgetLayoutParameters
        return CardAdWidgetGenerator(
            routeController = routeController,
            metrics = metrics,
            screenName = screenName,
            params = resolvedParams,
            origin = origin,
        )
    }

    private fun getValidAd(ads: List<CardAdWidgetParameters.Ad>): CardAdWidgetParameters.Ad? {
        if (ads.isEmpty()) return null

        val randomAd = randomByWeight(ads) as CardAdWidgetParameters.Ad

        return if (imageService.isImageAvailable(randomAd.image)) {
            randomAd
        } else {
            getValidAd(ads.minus(randomAd))
        }
    }
}

@Serializable
internal data class CardAdWidgetParameters(
    val ads: List<Ad>
) : KiboSerializable<CardAdWidgetParameters> {

    @Serializable
    internal data class Ad(
        val image: String,
        override val weight: Int,
        val accessibilityLabel: String,
        @SerialName("onTap") val onTapRouteLink: String? = null,
        val analytics: ItemNameIdAnalytics,
    ) : Weighted

    override fun getSerializer(): KSerializer<CardAdWidgetParameters> = serializer()
}

@Serializable
internal data class CardAdWidgetLayoutParameters(
    val imageName: String,
    val onTapRouteLink: String?,
    val accessibilityLabel: String,
    val analytics: ItemNameIdAnalytics,
) : KiboSerializable<CardAdWidgetLayoutParameters> {

    override fun getSerializer(): KSerializer<CardAdWidgetLayoutParameters> = serializer()
}

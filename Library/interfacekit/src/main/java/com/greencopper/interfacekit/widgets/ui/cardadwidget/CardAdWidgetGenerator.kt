import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.metrics.adImpression
import com.greencopper.interfacekit.metrics.adTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.CardAdWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.CardAdWidgetLayoutParameters
import com.greencopper.interfacekit.widgets.viewmodel.WidgetGenerator

internal class CardAdWidgetGenerator(
    routeController: RouteController,
    metrics: AggregateMetricsService,
    screenName: String,
    params: CardAdWidgetLayoutParameters,
    origin: Layout,
) : WidgetGenerator {
    override val id: String? = null
    override val topPadding = origin.resources.getInteger(R.integer.ad_widget_min_margin)
    override val bottomPadding = origin.resources.getInteger(R.integer.ad_widget_min_margin)
    override val generateComposable: @Composable ((modifier: Modifier) -> Unit) = { modifier ->
        ImageWidget(
            imageLight = params.imageName,
            imageDark = null,
            accessibilityLabel = params.accessibilityLabel,
            hideIfUnknownImage = true,
            hideIfLoadingImage = true,
            borderColor = InterfaceKitColor.cardAdWidget.card.border,
            shadowColor = InterfaceKitColor.cardAdWidget.card.shadow,
            onTap = params.onTapRouteLink?.let {
                {
                    metrics.track(
                        WidgetEventAnalytics(
                            EventName.adTap(),
                            buildWidgetAnalytics(
                                CardAdWidgetInitializer.widgetCategory,
                                params.analytics.itemName,
                                screenName,
                                params.analytics.itemId
                            )
                        )
                    )

                    routeController.redirectRouteLink(it, origin)
                }
            },
            modifier,
        )

        metrics.track(
            WidgetEventAnalytics(
                EventName.adImpression(),
                buildWidgetAnalytics(
                    CardAdWidgetInitializer.widgetCategory,
                    params.analytics.itemName,
                    screenName,
                    params.analytics.itemId
                )
            )
        )
    }
}

package com.greencopper.interfacekit.widgets.ui.cardadwidget

import ImageWidget
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.ui.Modifier
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.ComposeWidgetBinding
import com.greencopper.interfacekit.metrics.adImpression
import com.greencopper.interfacekit.metrics.adTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.CardAdWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.CardAdWidgetLayoutParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class CardAdWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<CardAdWidgetLayoutParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = CardAdWidgetInitializer.widgetCategory
    override val binding: ComposeWidgetBinding =
        ComposeWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.ad_widget_min_margin)

    private val metricsService: AggregateMetricsService by App.lazy()
    private val viewBuilder: IKViewBuilder by App.lazy()

    @Throws(WidgetException::class)
    override fun bind(
        params: CardAdWidgetLayoutParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.composeWidget.setContent {
            viewBuilder.buildContent {
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
                            metricsService.track(
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
                    Modifier,
                )
            }
        }

        metricsService.track(
            WidgetEventAnalytics(
                EventName.adImpression(),
                buildAnalytics(params, screenName)
            )
        )
    }

    override fun insertAdditionalAnalytics(
        map: MutableMap<EventParameter, String>,
        params: CardAdWidgetLayoutParameters,
    ) {
        super.insertAdditionalAnalytics(map, params)
        map[EventParameter.itemId] = params.analytics.itemId
    }

    override fun getWidgetItemName(params: CardAdWidgetLayoutParameters): String =
        params.analytics.itemName
}

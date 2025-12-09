package com.greencopper.interfacekit.widgets.ui.bannerwidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.ui.Modifier
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.databinding.ComposeWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.BannerWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.BannerWidgetParameters
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class BannerWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<BannerWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = BannerWidgetInitializer.widgetCategory
    override val binding: ComposeWidgetBinding = ComposeWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_full_width_min_margin)

    private val metricsService: AggregateMetricsService by App.lazy()
    private val viewBuilder: IKViewBuilder by App.lazy()

    override fun bind(
        params: BannerWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.composeWidget.setContent {
            viewBuilder.buildContent {
                BannerWidget(
                    state = BannerState(
                        title = params.title,
                        subtitle = params.subtitle,
                        button = params.button?.let {
                            BannerState.Button(
                                text = it.text,
                                icon = it.icon,
                                onTap = {
                                    redirectToRouteLink(it.onTap.routeLink, origin)
                                    metricsService.track(
                                        WidgetEventAnalytics(
                                            EventName.widgetCollectionWidgetTap(),
                                            buildWidgetAnalytics(
                                                widgetCategory = BannerWidgetInitializer.widgetCategory,
                                                widgetName = it.onTap.analytics.itemName,
                                                screenName = screenName,
                                            )
                                        )
                                    )
                                }
                            )
                        }
                    ),
                    Modifier
                )
            }
        }
    }

    override fun getWidgetItemName(params: BannerWidgetParameters): String? = null
}

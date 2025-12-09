package com.greencopper.interfacekit.widgets.ui.fullwidthwidget

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
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageWidgetParameters
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class FullWidthImageWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<FullWidthImageWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = FullWidthImageWidgetInitializer.widgetCategory
    override val binding: ComposeWidgetBinding =
        ComposeWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_full_width_min_margin)

    private val metricsService: AggregateMetricsService by App.lazy()
    private val viewBuilder: IKViewBuilder by App.lazy()

    override fun bind(
        params: FullWidthImageWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.composeWidget.setContent {
            viewBuilder.buildContent {
                FullWidthImageWidget(
                    params = params,
                    onTap = params.onTap?.let {
                        {
                            metricsService.track(
                                WidgetEventAnalytics(
                                    EventName.widgetCollectionWidgetTap(),
                                    buildWidgetAnalytics(
                                        FullWidthImageWidgetInitializer.widgetCategory,
                                        params.analytics.itemName,
                                        screenName
                                    )
                                )
                            )

                            routeController.redirect(it, origin)
                        }
                    },
                    Modifier,
                )
            }
        }
    }

    override fun getWidgetItemName(params: FullWidthImageWidgetParameters): String =
        params.analytics.itemName
}

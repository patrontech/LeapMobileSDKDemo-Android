package com.greencopper.interfacekit.widgets.ui.fullwidthimagecarousel

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.ui.Modifier
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.databinding.ComposeWidgetBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageCarouselWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.FullWidthImageCarouselWidgetParameters
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class FullWidthImageCarouselWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<FullWidthImageCarouselWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = FullWidthImageCarouselWidgetInitializer.widgetCategory
    override val binding: ComposeWidgetBinding = ComposeWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = 0

    private val metricsService: AggregateMetricsService by App.lazy()
    private val viewBuilder: IKViewBuilder by App.lazy()

    override fun bind(
        params: FullWidthImageCarouselWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>
    ) {
        binding.composeWidget.setContent {
            viewBuilder.buildContent {
                FullWidthImageCarouselWidget(
                    metrics = metricsService,
                    routeController = routeController,
                    screenName = screenName,
                    params = params,
                    origin = origin,
                    modifier = Modifier,
                )
            }
        }
    }

    override fun getWidgetItemName(params: FullWidthImageCarouselWidgetParameters): String? = null
}

package com.greencopper.interfacekit.widgets.ui.imagewidget

import ImageWidget
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.ui.Modifier
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.ComposeWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.analytics.buildWidgetAnalytics
import com.greencopper.interfacekit.widgets.initializer.ImageWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.ImageWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal open class ImageWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<ImageWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = ImageWidgetInitializer.widgetCategory
    override val binding: ComposeWidgetBinding =
        ComposeWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val metricsService: AggregateMetricsService by App.lazy()
    private val viewBuilder: IKViewBuilder by App.lazy()

    @SuppressLint("ClickableViewAccessibility")
    @Throws(WidgetException::class)
    override fun bind(
        params: ImageWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.composeWidget.setContent {
            viewBuilder.buildContent {
                ImageWidget(
                    imageLight = params.image.light,
                    imageDark = params.image.dark,
                    accessibilityLabel = params.accessibilityLabel,
                    hideIfUnknownImage = false,
                    hideIfLoadingImage = false,
                    borderColor = InterfaceKitColor.imageWidget.card.border,
                    shadowColor = InterfaceKitColor.imageWidget.card.shadow,
                    onTap = params.onTap?.let {
                        {
                            metricsService.track(
                                WidgetEventAnalytics(
                                    EventName.widgetCollectionWidgetTap(),
                                    buildWidgetAnalytics(
                                        ImageWidgetInitializer.widgetCategory,
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

    override fun getWidgetItemName(params: ImageWidgetParameters): String =
        params.analytics.itemName
}

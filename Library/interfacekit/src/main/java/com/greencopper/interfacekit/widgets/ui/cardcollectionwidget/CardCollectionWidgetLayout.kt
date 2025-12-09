package com.greencopper.interfacekit.widgets.ui.cardcollectionwidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.compose.ui.Modifier
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.databinding.ComposeWidgetBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.ui.compose.IKViewBuilder
import com.greencopper.interfacekit.widgets.initializer.CardCollectionWidgetInitializer
import com.greencopper.interfacekit.widgets.initializer.CardCollectionWidgetParameters
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class CardCollectionWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<CardCollectionWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = CardCollectionWidgetInitializer.widgetCategory
    override val binding: ComposeWidgetBinding = ComposeWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.card_collection_vertical_padding)

    private val metricsService: AggregateMetricsService by App.lazy()
    private val viewBuilder: IKViewBuilder by App.lazy()

    override fun bind(
        params: CardCollectionWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.composeWidget.setContent {
            viewBuilder.buildContent {
                CardCollectionWidget(
                    routeController = routeController,
                    metrics = metricsService,
                    screenName = screenName,
                    params = params,
                    origin = origin,
                    modifier = Modifier
                )
            }
        }
    }

    override fun getWidgetItemName(params: CardCollectionWidgetParameters): String? = null
}

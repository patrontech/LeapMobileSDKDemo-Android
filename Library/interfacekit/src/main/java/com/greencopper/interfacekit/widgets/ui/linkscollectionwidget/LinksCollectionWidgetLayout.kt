package com.greencopper.interfacekit.widgets.ui.linkscollectionwidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.isDarkMode
import com.greencopper.interfacekit.databinding.LinksCollectionWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionLinkTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.HorizontalSpacingItemDecorator
import com.greencopper.interfacekit.ui.setOtaTextOrGone
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.LinksCollectionWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class LinksCollectionWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<LinksCollectionWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "links_collection_widget"
    override val binding = LinksCollectionWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private var decoratorsSet = false
    private val localizationService: LocalizationService by App.lazy()

    init {
        clipChildren = false
        clipToPadding = false
        binding.linksCollectionWidgetTitle.setTextColor(InterfaceKitColor.linksCollectionWidget.title)
        binding.linksCollectionWidgetTitle.setFont(InterfaceKitTextStyle.linksCollectionWidget.title)
        (binding.linksCollectionWidgetRecyclerView.layoutManager as? LinearLayoutManager)?.stackFromEnd =
            layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    @Throws(WidgetException::class)
    override fun bind(
        params: LinksCollectionWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.linksCollectionWidgetTitle.setOtaTextOrGone(localizationService, params.title)
        binding.linksCollectionWidgetRecyclerView.apply {
            (layoutManager as? LinearLayoutManager)?.stackFromEnd =
                layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

            doOnNextLayout {
                setDynamicDecorators()
            }
            adapter = LinksCollectionAdapter(origin.lifecycleScope) {
                redirectToRouteLink(it.onTap, origin)
            }.apply {
                submitList(
                    params.links.toLinkCollectionItemsList(
                        params.links.all { it.text.isNullOrEmpty() },
                        WidgetEventAnalytics(
                            EventName.widgetCollectionLinkTap(),
                            buildAnalytics(params, screenName)
                        )
                    )
                )
            }
        }
    }

    private fun List<LinksCollectionWidgetParameters.Link>.toLinkCollectionItemsList(hideLabel: Boolean, analytics: WidgetEventAnalytics?) =
        map { link ->
            val icon = if (isDarkMode()) {
                link.icon.dark
            } else {
                link.icon.light
            }

            val itemAnalytics = analytics?.let {
                val paramsMap = HashMap(it.analytics)
                paramsMap[EventParameter("item_name")] = link.analytics.itemName
                analytics.copy(analytics = paramsMap)
            }
            LinksCollectionAdapter.LinkItem(
                link.text,
                icon,
                link.icon.shouldColor,
                link.onTap,
                itemAnalytics,
                hideLabel,
                link.accessibilityLabel
            )
        }

    private fun setDynamicDecorators() {
        with(binding.linksCollectionWidgetRecyclerView) {
            getChildAt(0)?.let { firstView ->
                if (!decoratorsSet) {
                    // Figure out optimal spacing to show 4 full items, plus 40% of a 5th item
                    val defaultMinSpace = resources.getDimension(R.dimen.widget_links_collection_min_margin).toInt() * 2
                    val itemWidth = firstView.width
                    val partialItem = (itemWidth * 0.4).toInt()
                    val parentWidth = this.width
                    val padding = this.paddingStart

                    val maxItemsFitting = ((parentWidth - partialItem - padding) / (defaultMinSpace + itemWidth))
                    val optimalSpace = (parentWidth - (maxItemsFitting * itemWidth) - partialItem - padding) / maxItemsFitting
                    val spacingDecorator = HorizontalSpacingItemDecorator(
                        spacing = optimalSpace
                    )
                    addItemDecoration(spacingDecorator)
                    decoratorsSet = true
                }
            }
        }
    }

    override fun getWidgetItemName(params: LinksCollectionWidgetParameters): String? = null
}

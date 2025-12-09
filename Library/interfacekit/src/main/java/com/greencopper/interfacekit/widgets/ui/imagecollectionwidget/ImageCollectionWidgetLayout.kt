package com.greencopper.interfacekit.widgets.ui.imagecollectionwidget

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.ImageCollectionWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.ImageCollectionWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

public class ImageCollectionWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<ImageCollectionWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "image_collection_widget"
    override val binding: ImageCollectionWidgetBinding =
        ImageCollectionWidgetBinding.inflate(LayoutInflater.from(context), this)
    private val localizationService: LocalizationService by App.lazy()

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val itemSpacing: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.image_collection_item_spacing) - context.resources.getDimensionPixelSize(R.dimen.spacing_allowing_shadow)
    }
    private val optimalItemWidth: Int by lazy {
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val padding = context.resources.getDimension(R.dimen.horizontal_margin)
        val minWidth = 80.dpToPx()

        //Calculating how many items would fit whole with the minimal width, then increase width so that it fills the space to match the 30%ish
        val widthToFitFullItemWidthItems = screenWidth - minWidth * 0.3 - padding
        val maxItemsFitting = (widthToFitFullItemWidthItems / (itemSpacing + minWidth)).toInt()
        (widthToFitFullItemWidthItems / maxItemsFitting - itemSpacing).toInt()
    }

    init {
        clipChildren = false
        clipToPadding = false
        with(binding.imageCollectionWidgetTitle) {
            setTextColor(InterfaceKitColor.imageCollectionWidget.title)
            setFont(InterfaceKitTextStyle.imageCollectionWidget.title)
        }

        with(binding.imageCollectionWidgetRecyclerView) {
            addItemDecoration(HorizontalSpacingItemDecorator(itemSpacing))
            (layoutManager as? LinearLayoutManager)?.stackFromEnd =
                layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
        }
    }

    @Throws(WidgetException::class)
    override fun bind(
        params: ImageCollectionWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.imageCollectionWidgetTitle.setOtaTextOrGone(localizationService, params.title)

        binding.imageCollectionWidgetRecyclerView.apply {
            adapter = ImageCollectionAdapter(optimalItemWidth, origin.viewLifecycleOwner.lifecycleScope) {
                redirectToRouteLink(it.onTapRouteLink, origin)
            }.apply {
                submitList(
                    params.items.toImageCollectionItemsList(
                        WidgetEventAnalytics(
                    EventName.widgetCollectionWidgetTap(),
                            buildAnalytics(params, screenName)
                        )
                    )
                )
            }
        }
    }

    private fun List<ImageCollectionWidgetParameters.Item>.toImageCollectionItemsList(
        analytics: WidgetEventAnalytics?,
    ): List<ImageCollectionAdapter.ImageItem> =
        map { imageItem ->
            val itemAnalytics = analytics?.let {
                val paramsMap = HashMap(it.analytics)
                paramsMap[EventParameter("item_name")] = imageItem.onTap.analytics.itemName
                analytics.copy(analytics = paramsMap)
            }
            ImageCollectionAdapter.ImageItem(
                image = imageItem.imageName,
                label = imageItem.label,
                accessibilityName = imageItem.accessibilityName,
                onTapRouteLink = imageItem.onTap.routeLink,
                onTapAnalytics = itemAnalytics,
            )
        }

    override fun getWidgetItemName(params: ImageCollectionWidgetParameters): String? = null

}

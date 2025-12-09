package com.greencopper.interfacekit.widgets.ui.imagewithlabelwidget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.ImageWithLabelWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.ImageWithLabelWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class ImageWithLabelWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RedirectingWidgetLayout<ImageWithLabelWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = "image_with_label_widget"
    override val binding: ImageWithLabelWidgetBinding =
        ImageWithLabelWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val localizationService: LocalizationService by App.lazy()

    init {
        val colors = InterfaceKitColor.imageWithLabelWidget
        with(binding) {
            cardView.strokeColor = colors.card.border
            cardView.setShadowColor(colors.card.shadow)
            title.setTextColor(colors.labels.title)
            title.setFont(InterfaceKitTextStyle.imageWithLabelWidget.title)
            body.setTextColor(colors.labels.body)
            body.setFont(InterfaceKitTextStyle.imageWithLabelWidget.body)
            labelsBackground.setBackgroundColor(colors.labels.background)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Throws(WidgetException::class)
    override fun bind(
        params: ImageWithLabelWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.imageView.setImageResource(android.R.color.transparent)
        jobs.add(
            binding.imageView.setImageFrom(params.imageName, origin.lifecycleScope)
        )
        binding.imageView.contentDescription = localizationService.getString(params.accessibilityLabel)

        params.onTap?.routeLink?.let { onTapRoute ->
            binding.cardView.setOnTouchListener(
                OnTouchClickListener(
                    context,
                    onTouchInternal = { _, event ->
                        playScalingAnimationOnEvent(event, this)
                        false
                    },
                    onClick = {
                        App.track(
                        WidgetEventAnalytics(
                            EventName.widgetCollectionWidgetTap(),
                            buildAnalytics(params, screenName)
                        )
                    )

                    redirectToRouteLink(onTapRoute, origin)
                }
            ))
        } ?: binding.cardView.setOnTouchListener(null)

        binding.title.setOtaTextOrGone(localizationService, params.title)
        binding.body.setOtaTextOrGone(localizationService, params.body)
    }

    override fun getWidgetItemName(params: ImageWithLabelWidgetParameters): String? =
        params.onTap?.analytics?.itemName
}

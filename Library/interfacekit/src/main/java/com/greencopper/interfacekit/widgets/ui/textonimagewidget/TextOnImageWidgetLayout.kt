package com.greencopper.interfacekit.widgets.ui.textonimagewidget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.interfacekit.databinding.TextOnImageWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.TextOnImageWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job

internal class TextOnImageWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<TextOnImageWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = "text_on_image_widget"
    override val binding: TextOnImageWidgetBinding =
        TextOnImageWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val localizationService: LocalizationService by App.lazy()

    init {
        binding.cardView.strokeColor = InterfaceKitColor.textOnImageWidget.card.border
        binding.cardView.setShadowColor(InterfaceKitColor.textOnImageWidget.card.shadow)
    }

    @SuppressLint("ClickableViewAccessibility")
    @Throws(WidgetException::class)
    override fun bind(
        params: TextOnImageWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        jobs.add(
            binding.imageView.setImageFrom(params.imageName, origin.lifecycleScope)
        )

        binding.root.contentDescription = localizationService.getString(params.accessibilityLabel)

        with(binding.title) {
            text = localizationService.getString(params.title.text)
            setTextColor(params.title.color.toColorInt())
            val font = InterfaceKitTextStyle.textOnImageWidget.title
            setFont(font)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                24,
                font.fontSize.toInt(),
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }

        with(binding.body) {
            setOtaTextOrGone(localizationService, params.body?.text)
            setTextColor(params.body?.color?.toColorInt() ?: -1)
            val font = InterfaceKitTextStyle.textOnImageWidget.body
            setFont(font)
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this,
                14,
                font.fontSize.toInt(),
                1,
                TypedValue.COMPLEX_UNIT_SP
            )
        }

        params.onTap?.routeLink?.let { onTapRoute ->
            binding.cardView.setOnTouchListener(OnTouchClickListener(
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
    }

    override fun getWidgetItemName(params: TextOnImageWidgetParameters): String? =
        params.onTap?.analytics?.itemName

}


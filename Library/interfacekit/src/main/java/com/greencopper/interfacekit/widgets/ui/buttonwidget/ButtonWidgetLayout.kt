package com.greencopper.interfacekit.widgets.ui.buttonwidget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.ButtonWidgetBinding
import com.greencopper.interfacekit.imageservice.ImageService
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.ui.utils.scale
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.ButtonWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ButtonWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<ButtonWidgetParameters>(context, attrs, defStyleAttr) {

    override val widgetCategory: String = "button_widget"
    override val binding = ButtonWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_min_margin)

    private val imageService: ImageService by App.lazy()
    private val localizationService: LocalizationService by App.lazy()

    init {
        binding.button.apply {
            iconTint = ColorStateList.valueOf(InterfaceKitColor.buttonWidget.icon)
            setTextColor(InterfaceKitColor.buttonWidget.text)
            background.setTint(InterfaceKitColor.buttonWidget.background)
            setFont(InterfaceKitTextStyle.buttonWidget.text)
        }
    }

    @Throws(WidgetException::class)
    override fun bind(
        params: ButtonWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        binding.button.apply {
            params.colors?.let { colors ->
                colors.background?.let { background.setTint(it.toColorInt()) }
                colors.border?.let { strokeColor = ColorStateList.valueOf(it.toColorInt()) }
                colors.image?.let { iconTint = ColorStateList.valueOf(it.toColorInt()) }
                colors.title?.let { setTextColor(it.toColorInt()) }
            }

            jobs.add(
                imageService.getImageDrawable(
                    params.iconName,
                    hideIfUnknown = true,
                    hideIfLoading = true,
                ).onEach {
                    icon = it.drawable?.scale(context, 20.dpToPx())
                }.launchIn(origin.lifecycleScope)
            )
            text = localizationService.getString(params.text)
            setOnSafeClickListener {
                App.track(
                    WidgetEventAnalytics(
                        EventName.widgetCollectionWidgetTap(),
                        buildAnalytics(params, screenName)
                    )
                )
                redirectTo(params.onTap, origin)
            }
        }
    }

    override fun getWidgetItemName(params: ButtonWidgetParameters): String =
        params.analytics.itemName

}

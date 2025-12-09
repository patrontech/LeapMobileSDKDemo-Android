package com.greencopper.interfacekit.widgets.ui.titlesubtitlewidget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.greencopper.core.services.localizationService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.databinding.TitleSubtitleWidgetBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.setOtaTextOrGone
import com.greencopper.interfacekit.widgets.initializer.TitleSubtitleWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.WidgetLayout
import com.greencopper.toolkit.App
import kotlinx.coroutines.Job

internal class TitleSubtitleWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WidgetLayout<TitleSubtitleWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "title_subtitle_widget"
    override val binding = TitleSubtitleWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_title_subtitle_min_margin)

    private val localizationService by lazy { App.localizationService() }

    init {
        setupWidth()
    }

    @Throws(WidgetException::class)
    override fun bind(
        params: TitleSubtitleWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        with(binding.title) {
            setOtaTextOrGone(localizationService, params.title)
            setTextColor(InterfaceKitColor.titleSubtitleWidget.title)
            setFont(InterfaceKitTextStyle.titleSubtitleWidget.title)
        }
        with(binding.subtitle) {
            setOtaTextOrGone(localizationService, params.subtitle)
            setTextColor(InterfaceKitColor.titleSubtitleWidget.subtitle)
            setFont(InterfaceKitTextStyle.titleSubtitleWidget.subtitle)
        }
    }

    override fun getWidgetItemName(params: TitleSubtitleWidgetParameters): String? = null
}

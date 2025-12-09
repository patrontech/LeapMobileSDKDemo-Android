package com.greencopper.interfacekit.widgets.ui.setting

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.localizationService
import com.greencopper.core.services.track
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.toColorInt
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.SettingWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.SettingWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import kotlinx.coroutines.Job

internal class SettingWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<SettingWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "setting_widget"
    override val binding = SettingWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = 0

    private val localizationService by lazy { App.localizationService() }

    @Throws(WidgetException::class)
    override fun bind(params: SettingWidgetParameters, screenName: String, origin: Layout, jobs: MutableList<Job>) {
        val title = localizationService.getString(params.title.label)
        val subtitle = localizationService.getString(params.subtitle?.label)

        with(binding.settingWidgetTitle) {
            val titleColor = params.title.color?.toColorInt() ?: InterfaceKitColor.settingWidget.title
            setTextColor(titleColor)
            setFont(InterfaceKitTextStyle.settingWidget.title)
            text = title
        }
        with(binding.settingWidgetSubtitle) {
            params.subtitle?.let { subtitleParams ->
                isVisible = true
                val subtitleColor = subtitleParams.color?.toColorInt() ?: InterfaceKitColor.settingWidget.subtitle
                setTextColor(subtitleColor)
                setFont(InterfaceKitTextStyle.settingWidget.subtitle)
                text = subtitle
            } ?: run {
                isVisible = false
            }
        }

        binding.settingWidgetRedirectIcon.imageTintList =
            ColorStateList.valueOf(InterfaceKitColor.settingWidget.chevron)
        setOnSafeClickListener {
            App.track(
                WidgetEventAnalytics(
                    EventName.widgetCollectionWidgetTap(),
                    buildAnalytics(params, screenName)
                )
            )
            redirectToRouteLink(params.onTap.routeLink, origin)
        }
        contentDescription =
            "$title $subtitle"
    }

    override fun getWidgetItemName(params: SettingWidgetParameters): String =
        params.onTap.analytics.itemName
}

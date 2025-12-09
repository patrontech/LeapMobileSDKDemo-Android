package com.greencopper.interfacekit.widgets.ui.unregisteredaccountwidget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.localizationService
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.UnregisteredAccountWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.UnregisteredAccountWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import kotlinx.coroutines.Job

internal class UnregisteredAccountWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<UnregisteredAccountWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "unregistered_account_widget"
    override val binding = UnregisteredAccountWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_double_margin)

    private val localizationService by lazy { App.localizationService() }

    @Throws(WidgetException::class)
    override fun bind(
        params: UnregisteredAccountWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        val colors = InterfaceKitColor.unregisteredAccountWidget
        with(binding.unregisteredAccountIcon) {
            background = computeGradient()
            setImageResource(R.drawable.ic_user_circle)
            imageTintList = ColorStateList.valueOf(colors.icon)
        }
        with(binding.unregisteredAccountDescription) {
            text = localizationService.getString(params.description)
            setTextColor(colors.text)
            setFont(InterfaceKitTextStyle.unregisteredAccountWidget.text)
        }
        with(binding.unregisteredAccountButton) {
            background.setTint(colors.button.background)
            strokeColor = ColorStateList.valueOf(colors.button.border)
            text = localizationService.getString(params.button.title)
            setTextColor(colors.button.label)
            setFont(InterfaceKitTextStyle.unregisteredAccountWidget.button.label)

            setOnSafeClickListener {
                App.track(
                    WidgetEventAnalytics(
                        EventName.widgetCollectionWidgetTap(),
                        buildAnalytics(params, screenName)
                    )
                )
                redirectToRouteLink(params.button.onTap.routeLink, origin)
            }
        }
    }

    private fun computeGradient() =
        GradientDrawable().apply {
            this.shape = GradientDrawable.OVAL
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = 600f
            setGradientCenter(1f, 1f)
            colors =
                intArrayOf(UIColor.default.accent.primary.toColorInt(), UIColor.default.accent.secondary.toColorInt())
        }

    override fun getWidgetItemName(params: UnregisteredAccountWidgetParameters): String =
        params.button.onTap.analytics.itemName
}

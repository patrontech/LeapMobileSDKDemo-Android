package com.greencopper.interfacekit.widgets.ui.accountsummarywidget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.labels.EventName
import com.greencopper.core.services.localizationService
import com.greencopper.core.services.track
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.interfacekit.accountprovider.AccountProviderResolver
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.AccountSummaryWidgetBinding
import com.greencopper.interfacekit.metrics.widgetCollectionWidgetTap
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.widgets.analytics.WidgetEventAnalytics
import com.greencopper.interfacekit.widgets.initializer.AccountSummaryWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AccountSummaryWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<AccountSummaryWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "account_summary_widget"
    override val binding = AccountSummaryWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_double_margin)

    private val localizationService by lazy { App.localizationService() }
    private val accountProviderResolver: AccountProviderResolver by App.lazy()

    @Throws(WidgetException::class)
    override fun bind(
        params: AccountSummaryWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        val colors = InterfaceKitColor.accountSummaryWidget
        binding.accountSummaryIcon.background = computeGradient()
        with(binding.accountSummaryButton) {
            backgroundTintList = ColorStateList.valueOf(colors.button.background)
            text = localizationService.getString(params.button.title)
            setTextColor(colors.button.label)
            setFont(InterfaceKitTextStyle.accountSummaryWidget.button.label)

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
        collectAccountInfo(origin, params) { accountInfo ->
            accountInfo.takeIf { it.info.isNotEmpty() }?.let { data ->
                val firstName = data.info["firstName"]
                val lastName = data.info["lastName"]
                with(binding.accountSummaryTitle) {
                    isVisible = true
                    text = "$firstName $lastName"
                    setTextColor(colors.title)
                    setFont(InterfaceKitTextStyle.accountSummaryWidget.title)
                }

                with(binding.accountSummarySubtitle) {
                    isVisible = true
                    text = "${data.info["email"]}"
                    setTextColor(colors.subtitle)
                    setFont(InterfaceKitTextStyle.accountSummaryWidget.subtitle)
                }

                binding.accountSummaryIcon.setImageDrawable(null)
                with(binding.accountSummaryInitials) {
                    text = "${firstName?.first()?.uppercase()}${lastName?.first()?.uppercase()}"
                    setTextColor(colors.initials)
                }
            } ?: run {
                binding.accountSummaryIcon.setImageResource(R.drawable.ic_user_circle)
                binding.accountSummaryIcon.imageTintList = ColorStateList.valueOf(colors.initials)
                binding.accountSummaryTitle.isVisible = false
                binding.accountSummarySubtitle.isVisible = false
            }
        }
    }

    private fun collectAccountInfo(
        origin: Layout,
        params: AccountSummaryWidgetParameters,
        useAccountInfo: (AccountProvider.AccountInfo) -> Unit,
    ) {
        origin.viewLifecycleOwner.lifecycleScope.launch {
            accountProviderResolver.resolve(params.provider.key)?.getAccount(params.provider.params)?.collect {
                useAccountInfo(it)
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

    override fun getWidgetItemName(params: AccountSummaryWidgetParameters): String =
        params.button.onTap.routeLink
}

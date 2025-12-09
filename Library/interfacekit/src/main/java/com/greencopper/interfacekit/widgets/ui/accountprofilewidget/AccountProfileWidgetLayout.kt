package com.greencopper.interfacekit.widgets.ui.accountprofilewidget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.localization.service.getString
import com.greencopper.core.services.localizationService
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.accountprovider.AccountProvider
import com.greencopper.interfacekit.accountprovider.AccountProviderResolver
import com.greencopper.interfacekit.color.*
import com.greencopper.interfacekit.databinding.AccountProfileWidgetBinding
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.dpToPx
import com.greencopper.interfacekit.widgets.initializer.AccountProfileWidgetParameters
import com.greencopper.interfacekit.widgets.ui.WidgetException
import com.greencopper.interfacekit.widgets.ui.redirectingwidget.RedirectingWidgetLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AccountProfileWidgetLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RedirectingWidgetLayout<AccountProfileWidgetParameters>(context, attrs, defStyleAttr) {
    override val widgetCategory: String = "account_profile_widget"
    override val binding = AccountProfileWidgetBinding.inflate(LayoutInflater.from(context), this)

    override val verticalMargin: Int = resources.getDimensionPixelSize(R.dimen.widget_double_margin)

    private val localizationService by lazy { App.localizationService() }
    private val accountProviderResolver: AccountProviderResolver by App.lazy()

    @Throws(WidgetException::class)
    override fun bind(
        params: AccountProfileWidgetParameters,
        screenName: String,
        origin: Layout,
        jobs: MutableList<Job>,
    ) {
        val colors = InterfaceKitColor.accountProfileWidget
        binding.accountProfileIcon.background = computeGradient()
        collectAccountInfo(origin, params) { accountInfo ->
            accountInfo.takeIf { it.info.isNotEmpty() }?.let { data ->
                val firstName = data.info["firstName"]
                val lastName = data.info["lastName"]

                binding.accountProfileIcon.setImageDrawable(null)
                with(binding.accountProfileInitials) {
                    text = "${firstName?.first()?.uppercase()}${lastName?.first()?.uppercase()}"
                    setTextColor(colors.initials)
                }

                if (params.infoToDisplay.isNotEmpty()) {
                    binding.accountSummaryInfoContainer.updateLayoutParams<LayoutParams> {
                        topMargin = 32.dpToPx()
                    }
                }
                params.infoToDisplay.forEachIndexed { index, info ->
                    val labelTextView = TextView(context)
                    labelTextView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    labelTextView.setTextColor(colors.label)
                    labelTextView.setFont(InterfaceKitTextStyle.accountProfileWidget.label)

                    val infoTextView = TextView(context)
                    infoTextView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 8.dpToPx()
                        if (index != params.infoToDisplay.lastIndex) {
                            bottomMargin = 16.dpToPx()
                        }
                    }
                    infoTextView.setTextColor(colors.text)
                    infoTextView.setFont(InterfaceKitTextStyle.accountProfileWidget.text)

                    computeInfo(info, data, labelTextView, infoTextView)
                    binding.accountSummaryInfoContainer.addView(labelTextView)
                    binding.accountSummaryInfoContainer.addView(infoTextView)
                }
            } ?: run {
                binding.accountProfileIcon.setImageResource(R.drawable.ic_user_circle)
                binding.accountProfileIcon.imageTintList = ColorStateList.valueOf(colors.initials)
            }
        }
        backgroundTintList = ColorStateList.valueOf(colors.background)
    }

    private fun collectAccountInfo(
        origin: Layout,
        params: AccountProfileWidgetParameters,
        useAccountInfo: (AccountProvider.AccountInfo) -> Unit,
    ) {
        origin.viewLifecycleOwner.lifecycleScope.launch {
            accountProviderResolver.resolve(params.provider.key)?.getAccount(params.provider.params)?.collect {
                useAccountInfo(it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun computeInfo(
        info: AccountProfileWidgetParameters.Info,
        accountProviderInfo: AccountProvider.AccountInfo,
        labelTextView: TextView,
        valueTextView: TextView,
    ) {
        labelTextView.text = localizationService.getString(info.label)
        valueTextView.text = accountProviderInfo.info[info.key]
    }

    private fun computeGradient() =
        GradientDrawable().apply {
            this.shape = GradientDrawable.OVAL
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = 600f
            setGradientCenter(1f, 1f)
            colors =
                intArrayOf(
                    UIColor.default.accent.primary.toColorInt(),
                    UIColor.default.accent.secondary.toColorInt()
                )
        }

    override fun getWidgetItemName(params: AccountProfileWidgetParameters): String =
        widgetCategory
}

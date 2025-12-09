package com.greencopper.interfacekit.onboarding.maincard.ui

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.EventParameter
import com.greencopper.core.metrics.labels.itemCategory
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.OnboardingMainCardFragmentBinding
import com.greencopper.interfacekit.metrics.mainActionCard
import com.greencopper.interfacekit.onboarding.maincard.*
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageActionHandler
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.textstyle.InterfaceKitTextStyle
import com.greencopper.interfacekit.textstyle.subsystem.setFont
import com.greencopper.interfacekit.ui.*
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.DefaultBackCloseToolbarNavigationControlsHandler
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.NavigationControlsHandler
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.launch

internal class MainActionCardFragment :
    ParameterizedFragment<MainActionCardLayoutData>, OnboardingPageLayout {

    constructor(constructorData: MainActionCardLayoutData?) : super(constructorData)

    @Deprecated("To conform to system and provide to FragmentFactory an empty constructor accessed by reflection")
    constructor() : super(null)

    private val localizationService: LocalizationService by App.lazy()
    private val actionHandler: OnboardingPageActionHandler by App.lazy()

    override val onboardingScreenViewEvent: ScreenViewEvent? by lazy {
        data.onboardingPageLayoutData.onboardingAnalytics?.let {
            val screen = Screen.mainActionCard(it.screenName)
            val parameters =
                mapOf(EventParameter.itemCategory to it.featureName.plus(" Onboarding"))
            ScreenViewEvent(screen, parameters)
        }
    }

    override val onboardingPageId: String by lazy {
        data.onboardingPageLayoutData.pageId
    }

    override val binding: OnboardingMainCardFragmentBinding by viewBinding(
        OnboardingMainCardFragmentBinding::inflate
    )

    override val navigationBarColor: Int by lazy { InterfaceKitColor.mainActionCardOnboardingPage.card.background }
    override val screenColor: ScreenColor get() = InterfaceKitColor.mainActionCardOnboardingPage

    override fun createNavigationControlsHandler(): NavigationControlsHandler =
        DefaultBackCloseToolbarNavigationControlsHandler(
            this,
            binding.onboardingToolbar,
            InterfaceKitColor.mainActionCardOnboardingPage.topBar,
            InterfaceKitTextStyle.mainActionCardOnboardingPage.topBar,
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view.context)
        setupListeners()
    }

    private fun setupView(context: Context) {
        val onboardingColor = InterfaceKitColor.mainActionCardOnboardingPage
        binding.onboardingMainCardContainer.apply {
            backgroundTintList = ColorStateList.valueOf(onboardingColor.card.background)
            setShadowColor(onboardingColor.card.shadow)
        }
        binding.onboardingMainCardTitleView.apply {
            text = localizationService.getString(data.title)
            setTextColor(onboardingColor.card.title)
            setFont(InterfaceKitTextStyle.mainActionCardOnboardingPage.card.title)
        }
        binding.onboardingMainCardDescriptionView.apply {
            val localizedText = localizationService.getString(data.text)
            val spannedText = HtmlCompat.fromHtml(localizedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
            text = spannedText.trim()
            setTextColor(onboardingColor.card.text)
            setFont(InterfaceKitTextStyle.mainActionCardOnboardingPage.card.text)
            movementMethod = ClickableLinkMovementMethod()
        }
        binding.onboardingMainCardPrimaryActionBtn.apply {
            data.mainButton?.let {
                text = localizationService.getString(it.title)
                setBackgroundColor(onboardingColor.card.button.background)
                setTextColor(onboardingColor.card.button.text)
                setFont(InterfaceKitTextStyle.mainActionCardOnboardingPage.card.button)
                visibility = View.VISIBLE
            } ?: run { visibility = View.GONE }
        }
        binding.onboardingMainCardSecondaryActionButton.apply {
            data.skipButton?.let {
                setTextColor(onboardingColor.card.skip)
                setFont(InterfaceKitTextStyle.mainActionCardOnboardingPage.card.skip)
                text = localizationService.getString(it.title)
                ContextCompat.getDrawable(context, R.drawable.ic_skip)?.mutate()?.let { drawable ->
                    drawable.setTint(onboardingColor.card.skip)
                    binding.onboardingMainCardSecondaryActionButton.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        drawable,
                        null
                    )
                }
                visibility = View.VISIBLE
            } ?: run { visibility = View.GONE }
        }
        binding.onboardingMainCardImageFullScreen.setImageFrom(
            data.backgroundImage,
            lifecycleScope,
            hideIfUnknown = true,
            hideIfLoading = true,
        )

        onboardingPageDelegate?.onboardingController?.onboardingSequence
            ?.takeIf { it.pages.size > 1}
            ?.let { sequence ->
                val sequenceData = sequence.toViewData(onboardingPageId)
                binding.onboardingMainCardDotsIndicatorView.setup(
                    numberOfDots = sequenceData.numberOfPages,
                    currentPosition = sequenceData.selectedPage,
                    selectedDotColor = onboardingColor.card.dots.selected,
                    defaultDotColor = onboardingColor.card.dots.normal
                )
            }
    }

    private fun setupListeners() {
        binding.onboardingMainCardPrimaryActionBtn.setOnSafeClickListener { launchAction(data.mainButton?.action) }
        binding.onboardingMainCardSecondaryActionButton.setOnSafeClickListener { launchAction(data.skipButton?.action) }
    }

    private fun launchAction(action: MainActionCardDataActionButton.Action?) {
        action?.let {
            lifecycleScope.launch {
                val actionResult =
                    actionHandler.executeAction(it.toPageAction(), this@MainActionCardFragment)

                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    onboardingPageDelegate?.pageDidComplete(
                        onboardingPageId,
                        actionResult
                    )
                }
            }
        } ?: onboardingPageDelegate?.pageDidComplete(
            onboardingPageId,
            true
        )
    }

    override fun restoreData(encodedData: String): MainActionCardLayoutData =
        KiboSerializable.decodeFromString(encodedData)
}

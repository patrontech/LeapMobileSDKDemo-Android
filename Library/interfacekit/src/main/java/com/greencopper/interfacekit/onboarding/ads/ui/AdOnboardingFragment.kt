package com.greencopper.interfacekit.onboarding.ads.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localization.service.getString
import com.greencopper.core.metrics.Screen
import com.greencopper.core.metrics.events.ScreenViewEvent
import com.greencopper.core.metrics.labels.*
import com.greencopper.core.metrics.provider.MappedProvider
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.color.InterfaceKitColor
import com.greencopper.interfacekit.color.ScreenColor
import com.greencopper.interfacekit.common.setOnSafeClickListener
import com.greencopper.interfacekit.databinding.OnboardingAdFragmentBinding
import com.greencopper.interfacekit.metrics.*
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.ads.AdOnboardingLayoutData
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.ui.fragment.ParameterizedFragment
import com.greencopper.interfacekit.ui.setImageFrom
import com.greencopper.interfacekit.ui.viewBinding
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import kotlinx.coroutines.*

internal class AdOnboardingFragment : ParameterizedFragment<AdOnboardingLayoutData>, OnboardingPageLayout {

    constructor(params: AdOnboardingLayoutData) : super(params)

    @Deprecated("For system purpose only. Don't use it.")
    constructor() : super(null)

    companion object {
        private const val KEY_AD_TIME_LEFT = "key.adTimeLeft"
    }

    override val binding: OnboardingAdFragmentBinding by viewBinding(OnboardingAdFragmentBinding::inflate)
    override val onboardingPageId: String by lazy { data.onboardingPageLayoutData.pageId }
    override val navigationBarColor: Int by lazy { InterfaceKitColor.mainActionCardOnboardingPage.card.background }
    override val screenColor: ScreenColor get() = InterfaceKitColor.mainActionCardOnboardingPage

    override val onboardingScreenViewEvent: ScreenViewEvent? by lazy {
        val category = data.analytics.featureName ?: "App"
        ScreenViewEvent(Screen.adOnboarding(data.analytics.itemName), mapOf(
            EventParameter.itemId to data.analytics.itemId,
            EventParameter.itemCategory to category.plus(" Onboarding"),
        ))
    }

    private val localizationService: LocalizationService by App.lazy()
    private val routeController: RouteController by App.lazy()
    private val metricsService: AggregateMetricsService by App.lazy()

    private var closeAdOnboardingJob: Job? = null
    private var timeStarted = 0L
    private var timeRemaining = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            ivAdOnboarding.setImageFrom(data.image, viewLifecycleOwner.lifecycleScope)
            ivAdOnboarding.contentDescription = localizationService.getString(data.accessibilityLabel)
            ivAdOnboarding.setOnSafeClickListener {
                metricsService.track(AdTapEvent())
                onAdTap()
            }

            val closeButton = InterfaceKitColor.adOnboardingPage.closeButton
            fabCloseAdOnboarding.backgroundTintList = ColorStateList.valueOf(closeButton.background)
            fabCloseAdOnboarding.setColorFilter(closeButton.icon)
            fabCloseAdOnboarding.setOnSafeClickListener {
                metricsService.track(CloseButtonTapEvent())
                closeAdOnboarding()
            }
        }

        timeRemaining = savedInstanceState?.getLong(KEY_AD_TIME_LEFT) ?: (data.autoCloseTimeout.toLong() * 1000L)
    }

    override fun onResume() {
        super.onResume()
        closeAdOnboardingIn(timeRemaining)
    }

    override fun onPause() {
        super.onPause()
        timeRemaining -= (System.currentTimeMillis() - timeStarted)
        closeAdOnboardingJob?.cancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_AD_TIME_LEFT, timeRemaining)
    }

    override fun restoreData(encodedData: String): AdOnboardingLayoutData =
        KiboSerializable.decodeFromString(encodedData)

    private fun onAdTap() {
        data.onTap?.let {
            routeController.redirectRouteLink(it, null)
            closeAdOnboarding()
        }
    }

    private fun closeAdOnboarding() {
        onboardingPageDelegate?.pageDidComplete(onboardingPageId, true)
    }

    private fun closeAdOnboardingIn(millis: Long) {
        closeAdOnboardingJob = viewLifecycleOwner.lifecycleScope.launch {
            timeStarted = System.currentTimeMillis()
            delay(millis)
            closeAdOnboarding()
        }
    }

    private fun analyticsData() = mapOf(
        EventParameter.itemName to data.analytics.itemName,
        EventParameter.itemId to data.analytics.itemId,
    )

    private inner class CloseButtonTapEvent : MappedMetrics {
        override fun track(provider: MappedProvider) {
            provider.track(EventName.adOnboardingClose(), analyticsData())
        }
    }

    private inner class AdTapEvent : MappedMetrics {
        override fun track(provider: MappedProvider) {
            provider.track(EventName.adOnboardingTap(), analyticsData())
        }
    }
}

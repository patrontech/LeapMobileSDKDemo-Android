package com.greencopper.interfacekit.onboarding.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.metrics.service.AggregateMetricsService
import com.greencopper.interfacekit.navigation.NavigationFragment
import com.greencopper.interfacekit.navigation.layout.*
import com.greencopper.interfacekit.onboarding.*
import com.greencopper.interfacekit.onboarding.initializers.OnboardingFeatureInitializer
import com.greencopper.interfacekit.onboarding.OnboardingContext
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.ui.shouldColorNavigationBar
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.addNavigationButtonsFlags
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.getNavigationButtonsFlags
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.lazy
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import kotlinx.coroutines.*

public class OnboardingContainerLayout :
    NavigationFragment(), OnboardingPageLayout.Delegate, RedirectableLayout {

    private lateinit var onboardingContext: OnboardingContext

    private val metricsService: AggregateMetricsService by App.lazy()
    private val layoutDataProvider: LayoutDataProvider by App.lazy()
    private val logging: Logging by App.lazy()

    override val onboardingController: OnboardingController by lazy {
        App.resolve(onboardingContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!this::onboardingContext.isInitialized)
            restoreData()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //If we already have fragments in, we don't display anything, we let the Fragment recreate itself and its content
        if (childFragmentManager.fragments.size == 0) {
            try {
                onboardingController.getLayoutToDisplay()?.let {
                    display(it)
                }
            } catch (e: Throwable) {
                parentFragmentManager.popBackStack()
                logging.e("Failed to create any onboarding view", throwable = e)
            }
        }
    }

    private fun restoreData() {
        onboardingContext = run {
            val hashCode = arguments?.getInt(ONBOARDING_CONTEXT_ARG_KEY) ?: return@run null
            layoutDataProvider.getLayoutData(hashCode) {
                KiboSerializable.decodeFromString<OnboardingContext>(it)
            }
        } ?: throw IllegalStateException("Onboarding container layout cannot work without onboarding context")
    }

    public fun display(page: Layout) {
        if (isAdded) {
            (page as? OnboardingPageLayout)?.onboardingScreenViewEvent?.let { metricsService.track(it) }
            page.addNavigationButtonsFlags(getNavigationButtonsFlags())
            page.shouldColorNavigationBar = shouldColorNavigationBar
            replace(page, tag = if (onboardingContext.isAppOnboarding) APP_ONBOARDING_TAG else null)
        }
    }

    override fun pageDidComplete(pageId: String, persistAsCompleted: Boolean) {
        onboardingController.pageDidComplete(this, pageId, persistAsCompleted)
    }

    public sealed class OnboardingControllerException : Throwable() {
        public data class NoPageNoFeatureException(val context: OnboardingContext) :
            OnboardingControllerException() {
            override val message: String
                get() = "[${this::class.java.superclass?.simpleName}] Couldn't retrieve Page nor Feature $context."
        }
    }

    public companion object {
        internal const val ONBOARDING_CONTEXT_ARG_KEY = "onboardingContext"
        internal const val APP_ONBOARDING_TAG = "appOnboardingFragment"

        public fun newInstance(onboardingContext: OnboardingContext): OnboardingContainerLayout =
            OnboardingContainerLayout().apply {
                this.onboardingContext = onboardingContext
                val contextHashCode = onboardingContext.hashCode()
                CoroutineScope(Dispatchers.IO).launch {
                    layoutDataProvider.addLayoutData(contextHashCode, onboardingContext)
                }

                if (arguments == null) {
                    arguments = bundleOf()
                }

                arguments?.putInt(ONBOARDING_CONTEXT_ARG_KEY, contextHashCode)
            }
    }

    override val redirectionHash: RedirectionHash by lazy {
        onboardingContext.redirectionHash ?: RedirectionHash(OnboardingFeatureInitializer.key)
    }
}

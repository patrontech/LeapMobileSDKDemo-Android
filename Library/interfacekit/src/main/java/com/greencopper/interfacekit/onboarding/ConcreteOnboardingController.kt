package com.greencopper.interfacekit.onboarding

import com.greencopper.core.conditions.authorized
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.common.interfaceKit
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.navigation.route.RouteController
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageInfo
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageKey
import com.greencopper.interfacekit.onboarding.pages.ui.OnboardingPageLayout
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout.OnboardingControllerException
import com.greencopper.interfacekit.ui.views.navigationcontrols.handlers.closePresentedLayout
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.LazyResolver
import com.greencopper.toolkit.di.resolver.ResolveException
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.logging.Logging
import com.greencopper.toolkit.logging.e
import java.time.Instant

internal class ConcreteOnboardingController(
    private val routeController: RouteController,
    private val featureResolver: FeatureResolver,
    private val conditionChecker: ConditionChecker,
    private val lazyLocalStorage: LazyResolver<LocalStorage>,
    private val onboardingContext: OnboardingContext,
    private val logging: Logging,
) : OnboardingController {

    private var onboardingInitializers = mutableMapOf<OnboardingPageKey, OnboardingPageInitializer>()

    override val onboardingSequence = OnboardingSequence(
        onboardingContext.pages
            .filter {
                try {
                    getInitializer(it).showInSequence()
                } catch (t: Throwable) {
                    logging.e("Failed to resolve onboarding initializer", throwable = t)
                    false
                }
            }
            .authorized(conditionChecker)
    )

    override fun getLayoutToDisplay(): Layout {
        return nextPage()?.let {
            it as Layout
        } ?: onboardingContext.feature?.let {
            featureResolver.resolve(it)
        } ?: throw OnboardingControllerException.NoPageNoFeatureException(onboardingContext)
    }

    private fun nextPage(currentPageId: String? = null): OnboardingPageLayout? {
        val nextPages = onboardingContext.pages.after(currentPageId)
            .authorized(conditionChecker)
        if (nextPages.isEmpty()) {
            return null
        }

        val nextPageInfo = nextPages.first()
        return try {
            resolveOnboardingPage(nextPageInfo)
        } catch (throwable: Throwable) {
            logging.e(
                "An error occurred in nextPage() of ${javaClass.simpleName}: $throwable",
                throwable = throwable
            )
            onboardingSequence.pages -= nextPageInfo
            nextPage(nextPageInfo.id)
        }
    }

    override fun pageDidComplete(
        onboardingContainerLayout: OnboardingContainerLayout,
        pageId: String,
        persistAsCompleted: Boolean
    ) {
        if (persistAsCompleted) {
            val onboardingStorage = lazyLocalStorage.resolve().project.interfaceKit.onboarding
            onboardingStorage.completedPages.value += pageId

            val lastCompletions = onboardingStorage.lastOnboardingPageCompletions.value.toMutableMap()
            lastCompletions[pageId] = Instant.now().epochSecond
            onboardingStorage.lastOnboardingPageCompletions.value = lastCompletions
        }

        nextPage(pageId)?.let {
            onboardingContainerLayout.display(it as Layout)
        } ?: onboardingContext.feature?.let {
            displayFeature(onboardingContainerLayout)
        } ?: run {
            // This should only be called during app-onboarding, when there is no feature to redirect too.
            closeOnboarding(onboardingContainerLayout)
        }
    }

    private fun displayFeature(onboardingContainerLayout: OnboardingContainerLayout) {
        onboardingContext.feature?.let {
            routeController.replace(onboardingContainerLayout, it)
        }
    }

    override fun closeOnboarding(onboardingContainerLayout: OnboardingContainerLayout) {
        onboardingContainerLayout.closePresentedLayout()
    }

    private fun resolveOnboardingPage(pageInfo: OnboardingPageInfo): OnboardingPageLayout =
        getInitializer(pageInfo).resolve(pageInfo.params, pageInfo.id)

    @Throws(ResolveException::class)
    private fun getInitializer(pageInfo: OnboardingPageInfo): OnboardingPageInitializer {
        return onboardingInitializers[pageInfo.key] ?: App.resolve<OnboardingPageInitializer>(tag = pageInfo.key).also {
            onboardingInitializers[pageInfo.key] = it
        }
    }
}

internal fun List<OnboardingPageInfo>.after(pageId: String?): List<OnboardingPageInfo> {
    if (pageId == null || isEmpty()) {
        return this
    }

    val currentIndex = indexOfLast { it.id == pageId }

    if (currentIndex == -1) {
        return emptyList()
    }

    return ArrayList(subList(currentIndex, lastIndex))
        .apply { add(this@after.last()) }
        .drop(1)
}

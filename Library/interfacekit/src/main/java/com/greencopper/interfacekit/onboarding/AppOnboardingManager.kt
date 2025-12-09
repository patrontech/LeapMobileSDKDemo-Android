package com.greencopper.interfacekit.onboarding

import androidx.fragment.app.FragmentManager
import com.greencopper.core.conditions.authorized
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.interfacekit.findFragmentByTagInStack
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigurationHolder
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import com.greencopper.interfacekit.present
import com.greencopper.interfacekit.rootview.RootLayoutHolder

internal interface AppOnboardingManager {
    fun checkAppOnboarding(fragmentManager: FragmentManager, rootContainerId: Int)
}

internal class ConcreteAppOnboardingManager(
    private val onboardingConfigHolder: OnboardingConfigurationHolder,
    private val conditionChecker: ConditionChecker,
) : AppOnboardingManager {

    override fun checkAppOnboarding(fragmentManager: FragmentManager, rootContainerId: Int) {
        RootLayoutHolder.rootLayoutHolder.value ?: return //if no root layout has been set, there's nothing to present over
        val onboarding = onboardingConfigHolder.currentConfiguration.value ?: return
        if (fragmentManager.findFragmentByTagInStack(OnboardingContainerLayout.APP_ONBOARDING_TAG) != null) return

        val onboardingPages = onboarding.pages.authorized(conditionChecker)
        if (onboardingPages.isNotEmpty()) {
            val argument = OnboardingContext(
                redirectionHash = null,
                pages = onboardingPages,
                feature = null,
                isAppOnboarding = true,
            )

            val layout = OnboardingContainerLayout.newInstance(argument)
            fragmentManager.present(rootContainerId, layout, OnboardingContainerLayout.APP_ONBOARDING_TAG)
        }
    }
}

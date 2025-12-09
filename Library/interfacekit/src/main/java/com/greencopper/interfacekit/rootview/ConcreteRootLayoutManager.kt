package com.greencopper.interfacekit.rootview

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import com.greencopper.core.conditions.authorized
import com.greencopper.core.conditions.conditionchecker.ConditionChecker
import com.greencopper.core.content.manager.CurrentProjectTagProvider
import com.greencopper.interfacekit.navigation.NavigationFragment
import com.greencopper.interfacekit.navigation.feature.FeatureResolver
import com.greencopper.interfacekit.navigation.layout.Layout
import com.greencopper.interfacekit.onboarding.OnboardingContext
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigurationHolder
import com.greencopper.interfacekit.onboarding.ui.OnboardingContainerLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

internal class ConcreteRootLayoutManager(
    private val featureResolver: FeatureResolver,
    private val onboardingConfigHolder: OnboardingConfigurationHolder,
    private val rootLayoutHolder: RootLayoutHolder,
    private val rootViewConfigurationHolder: RootViewConfigurationHolder,
    private val currentProjectTagProvider: CurrentProjectTagProvider,
    private val conditionChecker: ConditionChecker,
    private val defaultScope: CoroutineScope,
) : RootLayoutManager {

    private var fragmentManager: FragmentManager? = null

    init {
        defaultScope.launch {
            rootViewConfigurationHolder.flow.filterNotNull()
                .zip(currentProjectTagProvider.currentProjectFlow.filterNotNull()) { rootViewConfig, _ ->
                    rootViewConfig
                }
                .collectLatest {
                    updateRootLayout()
                }
        }
    }

    override fun setupRootLayout(
        fragmentManager: FragmentManager,
        alreadySetup: Boolean
    ): Flow<Layout> {
        this.fragmentManager = fragmentManager
        if (!alreadySetup) {
            defaultScope.launch {
                updateRootLayout()
            }
        }
        return RootLayoutHolder.rootLayoutHolder.filterNotNull()
    }

    override suspend fun updateRootLayout() {
        val rootViewConfig = rootViewConfigurationHolder.value
        val currentFragmentManager = fragmentManager

        if (rootViewConfig != null && currentFragmentManager != null) {
            val onboardingPages = onboardingConfigHolder.currentConfiguration.value?.pages.orEmpty()
            val shouldShowOnboarding = onboardingPages.authorized(conditionChecker).isNotEmpty()
            val layout = if (shouldShowOnboarding) {
                val featureInfo = rootViewConfig.feature
                val redirectionHash = featureResolver.resolveInitializer(featureInfo).redirectionHashFor(featureInfo.params)
                val argument = OnboardingContext(
                    redirectionHash = redirectionHash,
                    // Since some pages may effect the conditions of following pages, we need to pass the entire list
                    // to the onboarding context
                    pages = onboardingPages,
                    feature = featureInfo,
                    isAppOnboarding = true,
                )
                OnboardingContainerLayout.newInstance(argument)
            } else {
                featureResolver.resolve(rootViewConfig.feature)
            }

            withContext(Dispatchers.Main) {
                val rootLayoutContainer = NavigationFragment()
                var onAttachListener: FragmentOnAttachListener? = null
                onAttachListener = FragmentOnAttachListener { fragmentManager, fragment ->
                    if (fragment === rootLayoutContainer) {
                        onAttachListener?.let {
                            fragmentManager.removeFragmentOnAttachListener(it)
                        }
                        val tag = OnboardingContainerLayout.APP_ONBOARDING_TAG.takeIf { shouldShowOnboarding }
                        rootLayoutContainer.replace(layout, tag)
                    }
                }
                currentFragmentManager.addFragmentOnAttachListener(onAttachListener)
                rootLayoutHolder.setRootLayout(rootLayoutContainer)
            }
        }
    }
}

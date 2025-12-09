package com.greencopper.interfacekit.interests

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.core.localization.service.LocalizationService
import com.greencopper.core.localstorage.LocalStorage
import com.greencopper.interfacekit.bindViewModel
import com.greencopper.interfacekit.interests.recipe.InterestsConfigurationHolder
import com.greencopper.interfacekit.interests.recipe.InterestsRecipe
import com.greencopper.interfacekit.interests.recipe.InterestsRecipeOverride
import com.greencopper.interfacekit.interests.viewmodel.InterestsAnalyticsReducer
import com.greencopper.interfacekit.interests.viewmodel.InterestsReducer
import com.greencopper.interfacekit.interests.viewmodel.InterestsState
import com.greencopper.interfacekit.interests.viewmodel.InterestsViewModel
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.utils.StoreCoroutineProvider
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.*
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve
import com.toggl.komposable.extensions.combine
import com.toggl.komposable.extensions.createStore

internal class InterestsAssembly : Assembly {

    override fun registerBindings(registrar: Registrar) {
        with(registrar) {
            bindRecipe(auto(::InterestsRecipe))
            bindRecipeOverride(auto(::InterestsRecipeOverride))
            bindSingleton { InterestsConfigurationHolder() }
            bindFeature(InterestsPickerInitializer.key, auto(::InterestsPickerInitializer))
            bindProvider<OnboardingPageInitializer>(tag = InterestsOnboardingInitializer.key,
                auto(::InterestsOnboardingInitializer))
            bindCondition(IsAnInterestCondition.key) { IsAnInterestCondition(lazyResolver())}
            bindCondition(NoInterestCondition.key) { NoInterestCondition(lazyResolver()) }

            bindViewModel { params ->
                val initialState: InterestsState = params[0]
                val layoutData: InterestsLayoutData = params[1]

                val storeCoroutineProvider = resolve<StoreCoroutineProvider>()
                val configHolder = resolve<InterestsConfigurationHolder>()
                val localStorage = resolve<LocalStorage>()
                val localizationService = resolve<LocalizationService>()
                InterestsViewModel(
                    viewBuilder = resolve(),
                    store = createStore(
                        initialState = initialState,
                        reducer = combine(
                            InterestsReducer(
                                localizationService = localizationService,
                                localStorage = localStorage,
                                configHolder = configHolder,
                                remoteStateDispatcher = resolve(),
                                json = resolve(),
                            ),
                            InterestsAnalyticsReducer(
                                metricsService = resolve(),
                                localizationService = localizationService,
                                configHolder = configHolder,
                                localStorage = localStorage,
                                layoutData = layoutData,
                            )
                        ),
                        storeScopeProvider = storeCoroutineProvider.storeScopeProvider,
                        dispatcherProvider = storeCoroutineProvider.dispatcherProvider,
                    ),
                )
            }
        }
    }
}

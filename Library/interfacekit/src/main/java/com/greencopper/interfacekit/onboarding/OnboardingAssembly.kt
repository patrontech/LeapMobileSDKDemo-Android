package com.greencopper.interfacekit.onboarding

import com.greencopper.core.conditions.conditionchecker.bindCondition
import com.greencopper.interfacekit.navigation.feature.bindFeature
import com.greencopper.interfacekit.onboarding.ads.AdOnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.initializers.OnboardingFeatureInitializer
import com.greencopper.interfacekit.onboarding.initializers.OnboardingPageInitializer
import com.greencopper.interfacekit.onboarding.maincard.MainActionCardPageInitializer
import com.greencopper.interfacekit.onboarding.pages.ConcreteOnboardingPageActionHandler
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageActionHandler
import com.greencopper.interfacekit.onboarding.pages.OnboardingPageCompletionCondition
import com.greencopper.interfacekit.onboarding.pages.SinceLastPageCompletionCondition
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigurationHolder
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigRecipe
import com.greencopper.interfacekit.onboarding.recipe.OnboardingConfigRecipeOverride
import com.greencopper.toolkit.di.assembly.Assembly
import com.greencopper.toolkit.di.binding.Registrar
import com.greencopper.toolkit.di.binding.auto
import com.greencopper.toolkit.di.binding.bindProvider
import com.greencopper.toolkit.di.binding.bindRecipe
import com.greencopper.toolkit.di.binding.bindRecipeOverride
import com.greencopper.toolkit.di.binding.bindSingleton
import com.greencopper.toolkit.di.resolver.lazyResolver
import com.greencopper.toolkit.di.resolver.resolve

internal class OnboardingAssembly : Assembly {
    override fun registerBindings(registrar: Registrar) {
        registrar.apply {
            bindSingleton { OnboardingConfigurationHolder() }
            bindRecipe(auto(::OnboardingConfigRecipe))
            bindRecipeOverride(auto(::OnboardingConfigRecipeOverride))
            bindSingleton<AppOnboardingManager> {
                ConcreteAppOnboardingManager(
                    resolve(),
                    resolve(),
                )
            }

            bindProvider<OnboardingPageActionHandler> {
                ConcreteOnboardingPageActionHandler(
                    routeController = lazyResolver(),
                    commandExecutor = lazyResolver(),
                    conditionChecker = lazyResolver(),
                    metricsService = lazyResolver(),
                    locationService = lazyResolver(),
                    bluetoothService = lazyResolver(),
                    notificationPermissionService = lazyResolver(),
                    buildConfigProvider = lazyResolver(),
                    remoteStateDispatcher = lazyResolver(),
                )
            }

            bindCondition(OnboardingPageCompletionCondition.key) { OnboardingPageCompletionCondition(lazyResolver()) }
            bindCondition(SinceLastPageCompletionCondition.key) { SinceLastPageCompletionCondition(lazyResolver()) }

            bindFeature(OnboardingFeatureInitializer.key, auto(::OnboardingFeatureInitializer))

            bindProvider<OnboardingPageInitializer>(tag = MainActionCardPageInitializer.key) {
                MainActionCardPageInitializer()
            }
            bindProvider<OnboardingPageInitializer>(tag = AdOnboardingPageInitializer.key) {
                AdOnboardingPageInitializer(resolve())
            }

            bindProvider<OnboardingController> { params ->
                ConcreteOnboardingController(
                    routeController = resolve(),
                    featureResolver = resolve(),
                    conditionChecker = resolve(),
                    onboardingContext = params[0],
                    lazyLocalStorage = lazyResolver(),
                    logging = resolve(),
                )
            }
        }
    }
}

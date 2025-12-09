package com.greencopper.interfacekit.onboarding.recipe

import com.greencopper.core.content.recipe.ConfigurationHolderRecipe
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.core.data.KiboSerializable
import com.greencopper.core.data.writeToPath
import java.io.File

internal open class OnboardingConfigRecipe(
    onboardingConfigHolder: OnboardingConfigurationHolder,
) : ConfigurationHolderRecipe<OnboardingConfiguration, OnboardingConfigurationHolder>(
    onboardingConfigHolder,
    KiboSerializable.Companion::decodeFromString,
    OnboardingConfiguration::writeToPath,
) {
    override val key: ContentRecipeKey = ContentRecipeKey("InterfaceKit.Onboarding", 1)
    override val componentPath: String = "interfaceKit/onboarding"
}

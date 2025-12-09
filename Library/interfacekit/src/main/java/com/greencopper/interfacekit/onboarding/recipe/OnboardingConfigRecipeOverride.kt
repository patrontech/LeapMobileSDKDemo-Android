package com.greencopper.interfacekit.onboarding.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class OnboardingConfigRecipeOverride(
    private val context: Context,
    configurationHolder: OnboardingConfigurationHolder,
): OnboardingConfigRecipe(configurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

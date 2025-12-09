package com.greencopper.thuzi.account.registration.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class RegistrationRecipeOverride(
    private val context: Context,
    registrationConfigurationHolder: RegistrationConfigurationHolder,
): RegistrationRecipe(registrationConfigurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

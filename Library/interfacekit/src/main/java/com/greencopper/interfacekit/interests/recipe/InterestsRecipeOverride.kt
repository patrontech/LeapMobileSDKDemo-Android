package com.greencopper.interfacekit.interests.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class InterestsRecipeOverride(
    private val context: Context,
    interestsConfigHolder: InterestsConfigurationHolder,
) : InterestsRecipe(interestsConfigHolder), RecipeOverride {

    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

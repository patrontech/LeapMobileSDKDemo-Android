package com.greencopper.core.location.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.core.location.LocationConfigurationHolder
import java.io.File

internal class LocationRecipeOverride(
    private val context: Context,
    configurationHolder: LocationConfigurationHolder,
): LocationRecipe(configurationHolder), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

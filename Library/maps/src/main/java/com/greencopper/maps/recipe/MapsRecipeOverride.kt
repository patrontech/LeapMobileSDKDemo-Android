package com.greencopper.maps.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import java.io.File

internal class MapsRecipeOverride(
    private val context: Context,
    mapsRepository: MapsRepository,
): MapsRecipe(mapsRepository), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

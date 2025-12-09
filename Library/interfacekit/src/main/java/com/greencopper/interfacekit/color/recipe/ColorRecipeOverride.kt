package com.greencopper.interfacekit.color.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.interfacekit.color.repository.ColorRepository
import java.io.File

internal class ColorRecipeOverride(
    private val context: Context,
    colorRepository: ColorRepository,
) : ColorRecipe(colorRepository), RecipeOverride {
    override val componentPathOverride = componentPath
    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

package com.greencopper.interfacekit.editorial.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.interfacekit.editorial.repository.EditorialPageRepository
import java.io.File

internal class EditorialPageRecipeOverride(
    private val context: Context,
    repository: EditorialPageRepository,
): EditorialPageRecipe(repository), RecipeOverride {
    override val componentPathOverride = componentPath

    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))

}

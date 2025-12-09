package com.greencopper.core.localization.recipe

import android.content.Context
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.core.localization.translation.TranslationRepository
import kotlinx.serialization.json.Json
import java.io.File

internal class LocalizationRecipeOverride(
    private val context: Context,
    jsonParser: Json,
    localStorageTranslationRepository: TranslationRepository,
): LocalizationRecipe(jsonParser, localStorageTranslationRepository), RecipeOverride {
    override val componentPathOverride = componentPath

    override suspend fun tryToApply(contentDirectory: File) =
        super.tryToApply(overrideFolder(context))
}

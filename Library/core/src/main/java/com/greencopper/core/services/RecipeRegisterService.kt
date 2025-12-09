package com.greencopper.core.services

import android.content.Context
import com.greencopper.core.content.manager.ContentManager
import com.greencopper.core.content.manager.register
import com.greencopper.core.content.recipe.ContentRecipe
import com.greencopper.core.content.recipe.RecipeOverride
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolveAll
import com.greencopper.toolkit.versionprovider.BuildConfigProvider

public interface RecipeRegisterService {
    public fun register()
}

internal class ConcreteRecipeRegisterService(
    private val context: Context,
    private val contentManager: ContentManager,
    private val buildConfigProvider: BuildConfigProvider,
): RecipeRegisterService {
    override fun register() {
        val contentRecipes = App.resolveAll<ContentRecipe>(allowSubclasses = true, tag = "recipe")
        val isDebug = buildConfigProvider.isDebug
        val resultRecipes = if(isDebug) {
            val overrideRecipes = App.resolveAll<ContentRecipe>(allowSubclasses = true, tag = "recipeOverride")
                .filter {
                    (it as? RecipeOverride)?.overrideConfigExists(context) == true
                }
            contentRecipes.map { recipe ->
                val overrideRecipe = overrideRecipes.firstOrNull { recipe::class.isInstance(it) }
                overrideRecipe ?: recipe
            }
        } else {
            contentRecipes
        }
        contentManager.register(resultRecipes)
    }
}
package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipeInfo
import com.greencopper.core.content.recipe.ContentRecipeKey
import com.greencopper.toolkit.App
import com.greencopper.toolkit.di.resolver.resolve
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
internal data class ContentConfiguration(val recipes: List<Recipe>) {

    @Serializable
    data class Recipe(val key: ContentRecipeInfo, val enabled: Boolean)

    companion object {
        fun fromDirectory(contentDirectory: File): ContentConfiguration {
            val configFile = File(contentDirectory, "config.json")
            val configuration = App.resolve<Json>().decodeFromString(serializer(), configFile.readText())
            val recipes = configuration.recipes.plus(ContentConfigurationOverride.recipesToAdd.map {
                Recipe(it.key, it.enabled)
            })
            return configuration.copy(recipes = recipes)
        }
    }
}

public object ContentConfigurationOverride {
    public data class Recipe(val key: ContentRecipeInfo, val enabled: Boolean)

    public val recipesToAdd: MutableList<Recipe> = mutableListOf()
}

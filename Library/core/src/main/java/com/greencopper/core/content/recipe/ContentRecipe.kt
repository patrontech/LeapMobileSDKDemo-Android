package com.greencopper.core.content.recipe

import java.io.File

public interface ContentRecipe {
    public val key: ContentRecipeKey
    public val componentPath: String

    /**
     * Processes data from [unarchivedDirectory] to [contentDirectory]
     * This function throws if the process fails
     */
    public suspend fun process(unarchivedDirectory: File, contentDirectory: File)

    /** Updates the component's data using [contentDirectory] */
    public suspend fun apply(contentDirectory: File)

    public fun File.config(): File = File(this, "config.json")
}

public fun Collection<ContentRecipe>.keys(): Set<ContentRecipeKey> = this.map { it.key }.toSet()

public fun Collection<ContentRecipe>.intersectionRegardlessImplementation(infoSet: Set<ContentRecipeInfo>): Set<ContentRecipe> {
    return filter { infoSet.contains(it.key.info) }.toSet()
}

public fun Collection<ContentRecipe>.intersection(keys: Set<ContentRecipeKey>): Set<ContentRecipe> {
    return filter { recipe ->
        keys.contains(recipe.key)
    }.toSet()
}

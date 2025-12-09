package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipe
import java.io.File

internal interface ContentProcessor {

    /** Unzip [content] into proper directory */
    suspend fun open(content: Content): File

    /** Process all the [ContentRecipe] on [content] */
    suspend fun process(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content

    /** Apply all the [ContentRecipe] on [content] */
    suspend fun apply(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content

    /** Clean all files related to [content] */
    suspend fun clean(content: Content)
}

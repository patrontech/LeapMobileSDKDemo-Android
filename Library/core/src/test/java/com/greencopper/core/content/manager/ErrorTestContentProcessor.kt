package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipe
import java.io.File

internal class ErrorTestContentProcessor : ContentProcessor {

    override suspend fun open(content: Content): File {
        throw IllegalArgumentException()
    }

    override suspend fun process(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content {
        throw IllegalStateException()
    }

    override suspend fun apply(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content {
        throw IllegalAccessException()
    }

    override suspend fun clean(content: Content) {
        throw NoSuchElementException()
    }
}

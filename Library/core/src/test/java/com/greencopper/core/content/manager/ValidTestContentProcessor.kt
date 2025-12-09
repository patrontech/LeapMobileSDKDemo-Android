package com.greencopper.core.content.manager

import com.greencopper.core.content.recipe.ContentRecipe
import java.io.File

internal class ValidTestContentProcessor : ContentProcessor {
    var openCalled = 0
        private set

    var processCalled = 0
        private set

    var applyCalled = 0
        private set

    var cleanCalled = 0
        private set

    override suspend fun open(content: Content): File {
        openCalled++
        return content.archive.file
    }

    override suspend fun process(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content {
        processCalled++
        return content
    }

    override suspend fun apply(
        content: Content,
        recipes: Set<ContentRecipe>
    ): Content {
        applyCalled++
        return content
    }

    override suspend fun clean(content: Content) {
        cleanCalled++
    }

    fun reset() {
        openCalled = 0
        processCalled = 0
        applyCalled = 0
        cleanCalled = 0
    }
}

package com.greencopper.core.content.recipe

import java.io.File

internal class TestContentRecipe(
    private val failRun: Boolean = false,
    private val failApply: Boolean = false,
) : TryContentRecipe, RecipeOverride {
    override val componentPathOverride = ""
    override val key: ContentRecipeKey = ContentRecipeKey("Test.BasicRecipe", 1)
    override val componentPath: String = ""

    override suspend fun tryToProcess(unarchivedDirectory: File, contentDirectory: File) {
        require(unarchivedDirectory.exists()) { "Origin directory doesn't exist" }
        if (failRun) {
            throw IllegalArgumentException("Failed to process")
        }
        File(contentDirectory, "config.json").writeText("""{ "config": "true" } """)
    }

    override suspend fun tryToApply(contentDirectory: File) {
        if (failApply) {
            throw IllegalArgumentException("Failed to apply")
        }
        println("Done applying!")
    }
}